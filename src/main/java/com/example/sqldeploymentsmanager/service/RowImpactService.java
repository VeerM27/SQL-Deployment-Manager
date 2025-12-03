package com.example.sqldeploymentsmanager.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class RowImpactService {

    private final JdbcTemplate jdbcTemplate;

    public RowImpactService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public RowImpactSummary executeWithImpact(String sqlScript) {
        RowImpactSummary summary = new RowImpactSummary();

        if (sqlScript == null || sqlScript.isBlank()) {
            return summary;
        }

        String[] statements = sqlScript.split(";");
        for (String raw : statements) {
            String stmt = raw.trim();
            if (stmt.isEmpty()) continue;

            String upper = stmt.toUpperCase(Locale.ROOT).trim();

            try {
                if (upper.startsWith("INSERT")) {
                    int count = jdbcTemplate.update(stmt);
                    summary.insertCount += count;
                    summary.executedStatements.add(stmt);
                } else if (upper.startsWith("UPDATE")) {
                    int count = jdbcTemplate.update(stmt);
                    summary.updateCount += count;
                    summary.executedStatements.add(stmt);
                } else if (upper.startsWith("DELETE")) {
                    int count = jdbcTemplate.update(stmt);
                    summary.deleteCount += count;
                    summary.executedStatements.add(stmt);
                } else {
                    // DDL or other statements
                    jdbcTemplate.execute(stmt);
                    summary.executedStatements.add(stmt);
                }
            } catch (Exception ex) {
                // bubble up; WorkflowService will catch and report
                throw ex;
            }
        }

        return summary;
    }

    public static class RowImpactSummary {
        private int insertCount;
        private int updateCount;
        private int deleteCount;
        private final List<String> executedStatements = new ArrayList<>();

        public int getInsertCount() { return insertCount; }
        public int getUpdateCount() { return updateCount; }
        public int getDeleteCount() { return deleteCount; }
        public List<String> getExecutedStatements() { return executedStatements; }
    }
}
