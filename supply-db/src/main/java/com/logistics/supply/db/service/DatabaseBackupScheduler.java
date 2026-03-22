package com.logistics.supply.db.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class DatabaseBackupScheduler {

    private static final Logger log =   LoggerFactory.getLogger(DatabaseBackupScheduler.class);

    private final String databaseName;
    private final int retentionDays;
    private final int minBackupsToKeep;

    public DatabaseBackupScheduler(
            @Value("${db.service.databaseName}") String databaseName,
            @Value("${cron.backup.retentionDays:30}") int retentionDays,
            @Value("${cron.backup.minBackupsToKeep:5}") int minBackupsToKeep) {

        this.databaseName = databaseName;
        this.retentionDays = retentionDays;
        this.minBackupsToKeep = minBackupsToKeep;
    }

    @Scheduled(cron = "${cron.backup.expression: 0 0 0 * * ?}")
    public void runDatabaseBackup() {
        performDatabaseBackup();
    }

    private void performDatabaseBackup() {

        String backupPath = "%s%sBSupplyDbBackup".formatted(System.getProperty("user.home"), File.separator);

        String backupFileName = "%s_%d.sql".formatted("supply_db_backup", System.currentTimeMillis());

        String backupFilePath = "%s/%s".formatted(backupPath, backupFileName);

        String command = "pg_dump --dbname=%s -F p > %s".formatted(databaseName, backupFilePath);

        log.info("Run database backup: {} at time: {}", command, LocalDateTime.now());

        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", command);

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("Database backup completed successfully. Backup file: {}", backupFilePath);
                deleteOldBackups(backupPath);
            } else {
                log.info("Error occurred while performing the database backup.");
            }
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    private void deleteOldBackups(String backupPath) {
        File dir = new File(backupPath);
        File[] backups = dir.listFiles((d, name) -> name.endsWith(".sql"));
        if (backups == null || backups.length <= minBackupsToKeep) return;

        long cutoff = System.currentTimeMillis() - ChronoUnit.DAYS.getDuration().toMillis() * retentionDays;

        // Sort newest-first so we can honour the minimum retention count
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
