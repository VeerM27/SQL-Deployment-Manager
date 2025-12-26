package com.example.sqldeploymentsmanager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for capturing and comparing database schema snapshots.
 */
@Service
public class SchemaImpactService {

    @Value("${DB_NAME:sqldeploymentdb}")
    private String schemaName;

    private final JdbcTemplate jdbcTemplate;

    public SchemaImpactService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public SchemaSnapshot captureSnapshot() {

        String sql =
                "SELECT table_name, column_name, column_type, is_nullable, " +
                "COALESCE(column_default, '') AS column_default " +
                "FROM information_schema.columns " +
                "WHERE table_schema = ? " +
                "ORDER BY table_name, ordinal_position";

        List<ColumnInfo> columns = jdbcTemplate.query(sql, (rs, rowNum) -> {
            ColumnInfo c = new ColumnInfo();
            c.tableName = rs.getString("table_name");
            c.columnName = rs.getString("column_name");
            c.columnType = rs.getString("column_type");
            c.isNullable = rs.getString("is_nullable");
            c.columnDefault = rs.getString("column_default");
            return c;
        }, schemaName);

        Map<String, ColumnInfo> byKey = new HashMap<>();
        Set<String> tables = new HashSet<>();

        for (ColumnInfo c : columns) {
            String key = c.tableName + "." + c.columnName;
            byKey.put(key, c);
            tables.add(c.tableName);
        }

        return new SchemaSnapshot(byKey, tables);
    }

    public List<String> diff(SchemaSnapshot before, SchemaSnapshot after) {

        List<String> impact = new ArrayList<>();

        // New tables
        Set<String> newTables = new HashSet<>(after.tables);
        newTables.removeAll(before.tables);

        for (String t : newTables) {
            impact.add("+ Table created: " + t);
        }

        // Dropped tables
        Set<String> droppedTables = new HashSet<>(before.tables);
        droppedTables.removeAll(after.tables);

        for (String t : droppedTables) {
            impact.add("- Table dropped: " + t);
        }

        // Column changes
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(before.columns.keySet());
        allKeys.addAll(after.columns.keySet());

        for (String key : allKeys) {
            ColumnInfo beforeCol = before.columns.get(key);
            ColumnInfo afterCol = after.columns.get(key);

            if (beforeCol == null && afterCol != null) {
                impact.add("+ Column added: " + describeColumn(afterCol));
            }
            else if (beforeCol != null && afterCol == null) {
                impact.add("- Column removed: " + describeColumn(beforeCol));
            }
            else if (beforeCol != null && afterCol != null) {
                if (!beforeCol.signature().equals(afterCol.signature())) {
                    impact.add("~ Column modified: " + beforeCol.tableName + "." + beforeCol.columnName +
                            " (" + beforeCol.signature() + " → " + afterCol.signature() + ")");
                }
            }
        }

        if (impact.isEmpty()) {
            impact.add("No schema changes detected in schema '" + schemaName + "'.");
        }

        return impact;
    }

    private String describeColumn(ColumnInfo c) {
        return c.tableName + "." + c.columnName + " " + c.columnType +
                " NULLABLE=" + c.isNullable +
                (c.columnDefault == null || c.columnDefault.isEmpty() ? "" : (" DEFAULT=" + c.columnDefault));
    }

    // -------------------------------
    // Java 11 compatible POJOs
    // -------------------------------

    public static class ColumnInfo {
        public String tableName;
        public String columnName;
        public String columnType;
        public String isNullable;
        public String columnDefault;

        public String signature() {
            return columnType + "|" + isNullable + "|" + columnDefault;
        }
    }

    public static class SchemaSnapshot {
        public Map<String, ColumnInfo> columns;
        public Set<String> tables;

        public SchemaSnapshot(Map<String, ColumnInfo> columns, Set<String> tables) {
            this.columns = columns;
            this.tables = tables;
        }
    }
}
