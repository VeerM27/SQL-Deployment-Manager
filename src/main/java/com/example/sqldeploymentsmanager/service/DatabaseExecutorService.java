package com.example.sqldeploymentsmanager.service;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class DatabaseExecutorService {

    private final JdbcTemplate jdbcTemplate;

    // Whitelist of allowed SQL keywords
    private static final Set<String> ALLOWED_KEYWORDS = new HashSet<>(Arrays.asList(
        "SELECT", "INSERT", "UPDATE", "DELETE", "CREATE", "ALTER", "DROP", "TRUNCATE", "WITH"
    ));

    // Patterns that might indicate SQL injection attempts
    private static final Pattern[] SUSPICIOUS_PATTERNS = {
        Pattern.compile(".*;\\s*DROP\\s+", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*;\\s*DELETE\\s+", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*;\\s*UPDATE\\s+", Pattern.CASE_INSENSITIVE),
        Pattern.compile("'\\s*OR\\s+'", Pattern.CASE_INSENSITIVE),
        Pattern.compile("'\\s*OR\\s+1\\s*=\\s*1", Pattern.CASE_INSENSITIVE),
        Pattern.compile("UNION\\s+SELECT", Pattern.CASE_INSENSITIVE),
        Pattern.compile("EXEC\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("EXECUTE\\s*\\(", Pattern.CASE_INSENSITIVE)
    };

    public DatabaseExecutorService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Validates SQL syntax using JSQLParser.
     * Performs basic security checks to prevent SQL injection.
     *
     * @param sql The SQL statement to validate
     * @throws IllegalArgumentException if SQL is invalid or potentially dangerous
     */
    public void validateSyntax(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("SQL is empty.");
        }

        String trimmed = sql.trim();
        String upper = trimmed.toUpperCase();

        // Check if SQL starts with allowed keyword
        boolean startsWithAllowed = false;
        for (String keyword : ALLOWED_KEYWORDS) {
            if (upper.startsWith(keyword + " ") || upper.startsWith(keyword + "\n") || upper.startsWith(keyword + "\t")) {
                startsWithAllowed = true;
                break;
            }
        }

        if (!startsWithAllowed) {
            throw new IllegalArgumentException("SQL must start with a valid SQL keyword: " + ALLOWED_KEYWORDS);
        }

        // Check for suspicious patterns
        for (Pattern pattern : SUSPICIOUS_PATTERNS) {
            if (pattern.matcher(sql).find()) {
                throw new IllegalArgumentException("SQL contains potentially dangerous pattern. Please review your query.");
            }
        }

        // Try to parse with JSQLParser for syntax validation
        try {
            Statement statement = CCJSqlParserUtil.parse(trimmed);
            // Successfully parsed - SQL syntax is valid
        } catch (JSQLParserException e) {
            // For complex queries that JSQLParser can't handle, try EXPLAIN as fallback
            if (upper.startsWith("SELECT")) {
                try {
                    jdbcTemplate.execute("EXPLAIN " + sql);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("SQL syntax validation failed: " + ex.getMessage());
                }
            }
            // For non-SELECT, we'll allow it if it passed basic checks
        }
    }

    /**
     * Executes SQL script with validation.
     * Each statement is validated before execution.
     *
     * @param sqlScript The SQL script to execute (may contain multiple statements)
     * @throws IllegalArgumentException if SQL is invalid
     */
    public void executeSql(String sqlScript) {
        if (sqlScript == null || sqlScript.isBlank()) {
            throw new IllegalArgumentException("SQL script is empty.");
        }

        String[] parts = sqlScript.split(";");
        for (String statement : parts) {
            String cleaned = statement.trim();
            if (!cleaned.isEmpty()) {
                // Validate each statement before execution
                validateSyntax(cleaned);
                jdbcTemplate.execute(cleaned);
            }
        }
    }
}
