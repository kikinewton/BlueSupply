package com.logistics.supply.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ConditionalOnProperty(value = "app.scheduling.enable", havingValue = "true", matchIfMissing = true)
public class ScheduledDatabaseBackup {

    private final String dbHost;
    private final int dbPort;
    private final String dbName;
    private final String dbUsername;
    private final String dbPassword;
    private final int retentionDays;
    private final int minBackupsToKeep;
    private final String pgDumpPath;

    public ScheduledDatabaseBackup(
            @Value("${spring.datasource.url}") String datasourceUrl,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password,
            @Value("${cron.backup.retentionDays:30}") int retentionDays,
            @Value("${cron.backup.minBackupsToKeep:5}") int minBackupsToKeep,
            @Value("${cron.backup.pgDumpPath:pg_dump}") String pgDumpPath) {

        URI uri = URI.create(datasourceUrl.replaceFirst("^jdbc:", ""));
        this.dbHost = uri.getHost();
        this.dbPort = uri.getPort() == -1 ? 5432 : uri.getPort();
        this.dbName = uri.getPath().replaceFirst("^/", "");
        this.dbUsername = username;
        this.dbPassword = password;
        this.retentionDays = retentionDays;
        this.minBackupsToKeep = minBackupsToKeep;
        this.pgDumpPath = pgDumpPath;
    }

    @Scheduled(cron = "${cron.backup.expression:0 0 0 * * ?}")
    public void runDatabaseBackup() {
        log.info("Running scheduled database backup");
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

        String command = "%s -h %s -p %d -U %s -d %s -F p > %s"
                .formatted(pgDumpPath, dbHost, dbPort, dbUsername, dbName, tempFilePath);

        log.info("Run database backup at time: {}", LocalDateTime.now());

        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", command);
            processBuilder.environment().put("PGPASSWORD", dbPassword);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            boolean finished = process.waitFor(30, TimeUnit.MINUTES);
            Path path = Path.of(tempFilePath);
            if (!finished) {
                process.destroyForcibly();
                log.error("Database backup timed out after 30 minutes, process killed");
                Files.deleteIfExists(path);
                return;
            }

            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.exitValue();

            if (exitCode == 0) {
                Files.move(path, Path.of(backupFilePath), StandardCopyOption.ATOMIC_MOVE);
                log.info("Database backup completed successfully. Backup file: {}", backupFilePath);
                deleteOldBackups(backupPath);
            } else {
                Files.deleteIfExists(path);
                log.error("Database backup failed (exit code {}). pg_dump output: {}", exitCode, output);
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
