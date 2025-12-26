package com.example.sqldeploymentsmanager.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BackupService {

    private final String backupDir =
            Paths.get(System.getProperty("user.home"), "sql-deployment-backups").toString();

    @Value("${DB_NAME:sqldeploymentdb}")
    private String databaseName;

    @Value("${DB_USER}")
    private String dbUser;

    @Value("${DB_PASSWORD}")
    private String dbPassword;

    /**
     * Creates a full database backup using mysqldump.
     * Uses MySQL configuration file for secure credential handling.
     * Waits for backup process to complete before returning.
     *
     * @return Success message with backup path or error message
     */
    public String createFullBackup() {
        if (dbUser == null || dbPassword == null) {
            return "Database credentials not set. Please configure DB_USER and DB_PASSWORD environment variables.";
        }

        if (!isMysqlDumpAvailable()) {
            return "mysqldump not detected. Backup skipped. Please install MySQL client tools.";
        }

        Path configFile = null;
        try {
            // Create backup directory
            File backupDirectory = new File(backupDir);
            if (!backupDirectory.exists()) {
                backupDirectory.mkdirs();
            }

            // Create temporary MySQL config file for secure password handling
            configFile = createMySQLConfigFile();

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String backupFile = backupDir + "/" + databaseName + "_" + timestamp + ".sql";

            // Use --defaults-extra-file to avoid password in command line
            ProcessBuilder pb = new ProcessBuilder(
                    "mysqldump",
                    "--defaults-extra-file=" + configFile.toString(),
                    databaseName
            );

            pb.redirectOutput(new File(backupFile));
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // Wait for backup to complete
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                return "Backup failed with exit code: " + exitCode;
            }

            return backupFile;

        } catch (IOException e) {
            return "Backup failed: " + e.getMessage();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Backup interrupted: " + e.getMessage();
        } finally {
            // Clean up temporary config file
            if (configFile != null) {
                try {
                    Files.deleteIfExists(configFile);
                } catch (IOException e) {
                    // Log but don't fail the backup
                    System.err.println("Failed to delete temporary config file: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Creates a temporary MySQL configuration file with credentials.
     * This is more secure than passing password as command-line argument.
     *
     * @return Path to the temporary config file
     * @throws IOException if file creation fails
     */
    private Path createMySQLConfigFile() throws IOException {
        Path configFile = Files.createTempFile("mysql-backup-", ".cnf");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile.toFile()))) {
            writer.write("[client]\n");
            writer.write("user=" + dbUser + "\n");
            writer.write("password=" + dbPassword + "\n");
        }

        // Set file permissions to be readable only by owner (Unix-like systems)
        try {
            configFile.toFile().setReadable(false, false);
            configFile.toFile().setReadable(true, true);
            configFile.toFile().setWritable(false, false);
            configFile.toFile().setWritable(true, true);
        } catch (Exception e) {
            // Permissions setting may fail on Windows, but that's acceptable
        }

        return configFile;
    }

    /**
     * Checks if mysqldump is available on the system.
     *
     * @return true if mysqldump is available, false otherwise
     */
    private boolean isMysqlDumpAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("mysqldump", "--version");
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
}
