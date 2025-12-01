package com.example.sqldeploymentsmanager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.sql.*;
import java.util.*;

@Service
public class SchemaComparisonService {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    public List<String> compareWithDatabase(String sqlText) {
        List<String> results = new ArrayList<>();

        String normalized = sqlText.toUpperCase(Locale.ROOT)
                .replaceAll("--.*?\\n", "")
                .replaceAll("/\\*.*?\\*/", "")
                .replaceAll("\\s+", " ")
                .trim();

        String[] statements = normalized.split(";");

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            DatabaseMetaData metaData = conn.getMetaData();

            for (String stmt : statements) {
                stmt = stmt.trim();
                if (stmt.isEmpty()) continue;

                if (stmt.matches(".*CREATE\\s+TABLE.*")) {
                    String tableName = extractTableName(stmt, "CREATE TABLE");
                    if (tableExists(metaData, tableName)) {
                        results.add("⚠️ Table `" + tableName + "` already exists.");
                    } else {
                        results.add("✅ Table `" + tableName + "` will be created.");
                    }
                } else if (stmt.matches(".*ALTER\\s+TABLE.*")) {
                    String tableName = extractTableName(stmt, "ALTER TABLE");
                    if (tableExists(metaData, tableName)) {
                        results.add("✅ Table `" + tableName + "` exists — checking columns...");
                        results.addAll(analyzeColumnDifferences(metaData, tableName, stmt));
                    } else {
                        results.add("⚠️ ALTER TABLE failed — table `" + tableName + "` not found.");
                    }
                } else if (stmt.matches(".*INSERT\\s+INTO.*")) {
                    String tableName = extractTableName(stmt, "INSERT INTO");
                    if (tableExists(metaData, tableName)) {
                        results.add("✅ Data will be inserted into existing table `" + tableName + "`.");
                    } else {
                        results.add("⚠️ INSERT failed — table `" + tableName + "` not found.");
                    }
                } else if (stmt.matches(".*DROP\\s+TABLE.*")) {
                    String tableName = extractTableName(stmt, "DROP TABLE");
                    if (tableExists(metaData, tableName)) {
                        results.add("🟥 Table `" + tableName + "` exists — this will drop it.");
                    } else {
                        results.add("ℹ️ Table `" + tableName + "` does not exist — DROP ignored.");
                    }
                } else if (stmt.matches(".*SELECT\\s+.*FROM.*")) {
                    String tableName = extractTableName(stmt, "FROM");
                    if (tableExists(metaData, tableName)) {
                        results.add("ℹ️ SELECT will read data from table `" + tableName + "`.");
                    } else {
                        results.add("⚠️ SELECT references table `" + tableName + "` that doesn’t exist.");
                    }
                } else if (stmt.matches(".*UPDATE\\s+.*SET.*")) {
                    String tableName = extractTableName(stmt, "UPDATE");
                    if (tableExists(metaData, tableName)) {
                        if (stmt.contains("WHERE")) {
                            results.add("🟡 UPDATE will modify matching records in `" + tableName + "`.");
                        } else {
                            results.add("⚠️ UPDATE in `" + tableName + "` has no WHERE — all rows would be affected.");
                        }
                    } else {
                        results.add("⚠️ UPDATE failed — table `" + tableName + "` not found.");
                    }
                } else if (stmt.matches(".*DELETE\\s+FROM.*")) {
                    String tableName = extractTableName(stmt, "DELETE FROM");
                    if (tableExists(metaData, tableName)) {
                        if (stmt.contains("WHERE")) {
                            results.add("🟠 DELETE will remove matching rows from `" + tableName + "`.");
                        } else {
                            results.add("⚠️ DELETE without WHERE — all rows in `" + tableName + "` will be deleted.");
                        }
                    } else {
                        results.add("⚠️ DELETE failed — table `" + tableName + "` not found.");
                    }
                } else {
                    results.add("ℹ️ No schema-level operation detected in: " + summarize(stmt));
                }
            }

        } catch (Exception e) {
            results.add("❌ Error comparing schema: " + e.getMessage());
        }

        return results;
    }

    private boolean tableExists(DatabaseMetaData metaData, String tableName) throws SQLException {
        try (ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private List<String> analyzeColumnDifferences(DatabaseMetaData metaData, String tableName, String sql) throws SQLException {
        List<String> diffs = new ArrayList<>();
        try (ResultSet rs = metaData.getColumns(null, null, tableName, null)) {
            Set<String> existingCols = new HashSet<>();
            while (rs.next()) {
                existingCols.add(rs.getString("COLUMN_NAME").toUpperCase(Locale.ROOT));
            }

            if (sql.contains("ADD COLUMN")) {
                String[] parts = sql.split("ADD COLUMN");
                for (int i = 1; i < parts.length; i++) {
                    String colDef = parts[i].trim().split(",|\\)")[0];
                    String colName = colDef.split("\\s+")[0];
                    if (existingCols.contains(colName)) {
                        diffs.add("⚠️ Column `" + colName + "` already exists in `" + tableName + "`.");
                    } else {
                        diffs.add("✅ Column `" + colName + "` will be added to `" + tableName + "`.");
                    }
                }
            }
        }
        return diffs;
    }

    private String extractTableName(String sql, String keyword) {
        try {
            String[] parts = sql.split(keyword)[1].trim().split("\\s+|\\(|;");
            return parts[0];
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private String summarize(String stmt) {
        return stmt.length() > 80 ? stmt.substring(0, 77) + "..." : stmt;
    }
}
