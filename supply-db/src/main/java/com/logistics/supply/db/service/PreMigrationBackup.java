package com.logistics.supply.db.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@ConditionalOnProperty(value = "app.scheduling.enable", havingValue = "true", matchIfMissing = true)
@Component
public class PreMigrationBackup {

    private static final Logger log = LoggerFactory.getLogger(PreMigrationBackup.class);

    private final String databaseName;
    private final int retentionDays;
    private final int minBackupsToKeep;
    private final String pgDumpPath;

    public PreMigrationBackup(
            @Value("${db.service.databaseName}") String databaseName,
            @Value("${cron.backup.retentionDays:30}") int retentionDays,
            @Value("${cron.backup.minBackupsToKeep:5}") int minBackupsToKeep,
            @Value("${cron.backup.pgDumpPath:pg_dump}") String pgDumpPath) {

        this.databaseName = databaseName;
        this.retentionDays = retentionDays;
        this.minBackupsToKeep = minBackupsToKeep;
        this.pgDumpPath = pgDumpPath;
    }

    public void run() {
        log.info("Running pre-migration database backup");
        performDatabaseBackup();
    }

    private void performDatabaseBackup() {

        String backupPath = "%s%sBSupplyDbBackup".formatted(System.getProperty("user.home"), File.separator);

        File backupDir = new File(backupPath);
        if (!backupDir.exists() && !backupDir.mkdirs()) {
            log.error("Failed to create backup directory: {}", backupPath);
            return;
        }

        String backupFileName = "%s_%d.sql".formatted("supply_db_backup", System.currentTimeMillis());
        String backupFilePath = "%s/%s".formatted(backupPath, backupFileName);
        String tempFilePath = backupFilePath + ".tmp";

        String command = "%s --dbname=%s -F p > %s".formatted(pgDumpPath, databaseName, tempFilePath);

        log.info("Run database backup at time: {}", LocalDateTime.now());

        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", command);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            boolean finished = process.waitFor(30, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                log.error("Database backup timed out after 30 minutes, process killed");
                Files.deleteIfExists(Path.of(tempFilePath));
                return;
            }

            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.exitValue();

            if (exitCode == 0) {
                Files.move(Path.of(tempFilePath), Path.of(backupFilePath), StandardCopyOption.ATOMIC_MOVE);
                log.info("Database backup completed successfully. Backup file: {}", backupFilePath);
                deleteOldBackups(backupPath);
            } else {
                Files.deleteIfExists(Path.of(tempFilePath));
                log.error("Database backup failed (exit code {}). pg_dump output: {}", exitCode, output);
                File partial = new File(backupFilePath);
                if (partial.exists() && !partial.delete()) {
                    log.warn("Could not delete partial backup file: {}", backupFilePath);
                }
            }
        } catch (IOException | InterruptedException e) {
            log.error("Database backup failed with exception", e);
            Thread.currentThread().interrupt();
        }
    }

    private void deleteOldBackups(String backupPath) {
        File dir = new File(backupPath);
        File[] backups = dir.listFiles((d, name) -> name.endsWith(".sql"));
        if (backups == null || backups.length <= minBackupsToKeep) return;

        long cutoff = System.currentTimeMillis() - ChronoUnit.DAYS.getDuration().toMillis() * retentionDays;

        java.util.Arrays.sort(backups, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));

        int kept = 0;
        for (File backup : backups) {
            String name = backup.getName();
            int lastUnderscore = name.lastIndexOf('_');
            int dot = name.lastIndexOf('.');
            if (lastUnderscore < 0 || dot < 0 || lastUnderscore >= dot) continue;

            try {
                long fileTimestamp = Long.parseLong(name.substring(lastUnderscore + 1, dot));
                if (kept < minBackupsToKeep || fileTimestamp >= cutoff) {
                    kept++;
                    continue;
                }
                boolean deleted = backup.delete();
                if (deleted) {
                    log.info("Deleted old backup: {}", name);
                } else {
                    log.warn("Failed to delete old backup: {}", name);
                }
            } catch (NumberFormatException e) {
                log.warn("Skipping backup file with unrecognised name format: {}", name);
            }
        }
    }
}
