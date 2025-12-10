package com.example.sqldeploymentsmanager.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Service;

@Service
public class BackupService {

    private final String backupDir =
            Paths.get(System.getProperty("user.home"), "sql-deployment-backups").toString();

    public String createFullBackup() {

        String dbUser = System.getenv("DB_USER");
        String dbPass = System.getenv("DB_PASS");

        if (dbUser == null || dbPass == null) {
            return "Database credentials not set. Please configure DB_USER and DB_PASS environment variables.";
        }

        if (!isMysqlDumpAvailable()) {
            return "mysqldump not detected. Backup skipped. Please install MySQL client tools.";
        }

        try {
            new File(backupDir).mkdirs();

            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String backupFile = backupDir + "/college_" + timestamp + ".sql";

            ProcessBuilder pb = new ProcessBuilder(
                    "mysqldump",
                    "-u" + dbUser,
                    "-p" + dbPass,
                    "college"
            );

            pb.redirectOutput(new File(backupFile));
            pb.start();

            return "Backup created successfully: " + backupFile;

        } catch (IOException e) {
            return "Backup failed: " + e.getMessage();
        }
    }

    private boolean isMysqlDumpAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("mysqldump", "--version");
            pb.start();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
