package com.logistics.supply.db.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class DatabaseBackupScheduler {

    private static final Logger log =   LoggerFactory.getLogger(DatabaseBackupScheduler.class);

    @Value("${db.service.databaseName}")
    private String databaseName;
    @Value("${db.service.username}")
    private String username;

    @Scheduled(cron = "0 0 0 * * ?")
    public void runDatabaseBackup() {
        performDatabaseBackup();
    }

    private void performDatabaseBackup() {

        String backupPath = "%s%sBSupplyDbBackup".formatted(System.getProperty("user.home"), File.separator);

        String backupFileName = "%s_%d.sql".formatted("supply_db_backup", System.currentTimeMillis());

        String backupFilePath = "%s/%s".formatted(backupPath, backupFileName);

        String command = "pg_dump --dbname=%s -F p > %s"
                .formatted(databaseName, backupFilePath);

        log.info("Run database backup: {}", command);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", command);

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("Database backup completed successfully. Backup file: {}", backupFilePath);
            } else {
                log.info("Error occurred while performing the database backup.");
            }
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }
    }
}
