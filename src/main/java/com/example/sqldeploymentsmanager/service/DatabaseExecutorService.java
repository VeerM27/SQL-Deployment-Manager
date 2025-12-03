package com.example.sqldeploymentsmanager.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseExecutorService {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseExecutorService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void validateSyntax(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("SQL is empty.");
        }

        String trimmed = sql.trim().toUpperCase();
        if (trimmed.startsWith("SELECT")) {
            jdbcTemplate.execute("EXPLAIN " + sql);
        }
        // non-select = no strict validation
    }

    public void executeSql(String sqlScript) {
        if (sqlScript == null || sqlScript.isBlank()) {
            throw new IllegalArgumentException("SQL script is empty.");
        }

        String[] parts = sqlScript.split(";");
        for (String statement : parts) {
            String cleaned = statement.trim();
            if (!cleaned.isEmpty()) {
                jdbcTemplate.execute(cleaned);
            }
        }
    }
}
