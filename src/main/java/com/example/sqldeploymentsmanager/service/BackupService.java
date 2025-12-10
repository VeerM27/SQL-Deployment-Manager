package com.example.sqldeploymentsmanager.service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BackupService {

    // You can also move these to application.properties if you want
    private static final String SCHEMA_NAME = "college";
    private static final String BACKUP_DIR =
            System.getProperty("user.home") + "/sql-deployment-backups";

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    public String createFullBackup() {

        try {
            // Ensure backup directory exists
            File dir = new File(BACKUP_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Timestamped filename
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String backupFileName = "college_" + timestamp + ".sql";
            String fullPath = BACKUP_DIR + "/" + backupFileName;

            // Extract MySQL host from JDBC URL (basic safe parse)
            // Example: jdbc:mysql://localhost:3306/college
            String host = dbUrl.split("//")[1].split(":")[0];

            String command = String.format(
                    "mysqldump -h %s -u%s -p%s %s > %s",
                    host,
                    dbUser,
                    dbPassword,
                    SCHEMA_NAME,
                    fullPath
            );

            Process process = Runtime.getRuntime().exec(new String[]{
                    "bash", "-c", command
            });

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("mysqldump failed with exit code " + exitCode);
            }

            return fullPath;

        } catch (Exception ex) {
            throw new RuntimeException("Backup failed: " + ex.getMessage(), ex);
        }
    }
}
