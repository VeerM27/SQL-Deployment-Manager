package com.example.sqldeploymentsmanager.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatabaseBackupService {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseBackupService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void backupCurrentSchema() {
        String schema = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        if (schema == null) throw new IllegalStateException("Cannot determine current schema.");

        String backupSchema = schema + "_backup";

        jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS `" + backupSchema + "`");

        List<String> tables = jdbcTemplate.queryForList("SHOW TABLES", String.class);

        for (String table : tables) {
            String source = "`" + schema + "`.`" + table + "`";
            String target = "`" + backupSchema + "`.`" + table + "`";

            jdbcTemplate.execute("DROP TABLE IF EXISTS " + target);
            jdbcTemplate.execute("CREATE TABLE " + target + " LIKE " + source);
            jdbcTemplate.execute("INSERT INTO " + target + " SELECT * FROM " + source);
        }
    }
}
