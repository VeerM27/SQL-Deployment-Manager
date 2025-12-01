package com.example.sqldeploymentsmanager.service;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SQLAnalysisService {

    public List<String> smartAnalyze(String sqlText) {
        List<String> feedback = new ArrayList<>();
        
        if (sqlText == null || sqlText.isBlank()) {
            feedback.add("⚠️ No SQL provided.");
            return feedback;
        }

        String trimmedSQL = sqlText.trim();
        String upperSQL = trimmedSQL.toUpperCase();

        try {
            // Parse SQL using JSQLParser
            Statement statement = CCJSqlParserUtil.parse(trimmedSQL);
            
            // Analyze based on statement type
            feedback.addAll(analyzeStatement(statement, upperSQL));
            
        } catch (JSQLParserException e) {
            // If parsing fails, fall back to basic analysis
            feedback.addAll(basicAnalysis(upperSQL));
            feedback.add(0, "⚠️ Note: Using basic analysis (SQL syntax may have limitations).");
        } catch (Exception e) {
            feedback.add("🟥 Analysis Error: " + e.getMessage());
            return feedback;
        }

        // Add security checks
        feedback.addAll(checkSecurityIssues(upperSQL));
        
        // Add performance suggestions
        feedback.addAll(checkPerformanceIssues(upperSQL));

        // Add summary
        if (feedback.isEmpty()) {
            feedback.add("✅ SQL appears safe and efficient.");
        } else {
            feedback.add("✅ Analysis completed with " + feedback.size() + " observations.");
        }

        return feedback;
    }

    private List<String> analyzeStatement(Statement statement, String upperSQL) {
        List<String> feedback = new ArrayList<>();
        
        if (statement instanceof Select) {
            feedback.addAll(analyzeSelect((Select) statement, upperSQL));
        } else if (statement instanceof Delete) {
            feedback.addAll(analyzeDelete((Delete) statement, upperSQL));
        } else if (statement instanceof Update) {
            feedback.addAll(analyzeUpdate((Update) statement, upperSQL));
        } else if (statement instanceof Insert) {
            feedback.addAll(analyzeInsert((Insert) statement, upperSQL));
        } else if (statement instanceof Alter) {
            feedback.add("⚠️ Schema Change: ALTER statement detected.");
            feedback.add("ℹ️ Best Practice: Test schema changes in development first.");
        } else if (statement instanceof Drop) {
            feedback.add("🟥 Dangerous: DROP statement detected — potential data loss.");
            feedback.add("✅ Recommendation: Consider backup before execution.");
        } else if (statement instanceof Truncate) {
            feedback.add("🟥 Dangerous: TRUNCATE clears entire table contents.");
            feedback.add("✅ Alternative: Use DELETE with WHERE for selective removal.");
        } else if (statement instanceof CreateTable) {
            feedback.add("ℹ️ Schema Creation: CREATE TABLE statement detected.");
            feedback.add("✅ Recommendation: Consider adding indexes and constraints.");
        }
        
        return feedback;
    }

    private List<String> analyzeSelect(Select select, String upperSQL) {
        List<String> feedback = new ArrayList<>();
        
        feedback.add("🔍 SELECT statement analysis:");
        
        // Check for SELECT *
        if (upperSQL.contains("SELECT *") && !upperSQL.contains("COUNT(*)")) {
            feedback.add("   ⚠️ Performance: Avoid SELECT * — specify needed columns.");
        }
        
        // Check for missing WHERE in potentially large operations
        if (!upperSQL.contains(" WHERE ") && upperSQL.contains(" FROM ")) {
            feedback.add("   ℹ️ Info: No WHERE clause - may return large result set.");
        }
        
        // Check for ORDER BY without LIMIT
        if (upperSQL.contains(" ORDER BY ") && !upperSQL.contains(" LIMIT ")) {
            feedback.add("   ℹ️ Performance: Consider adding LIMIT to ORDER BY queries.");
        }
        
        // Check for multiple JOINs
        long joinCount = upperSQL.split(" JOIN ", -1).length - 1;
        if (joinCount > 3) {
            feedback.add("   ⚠️ Complexity: Multiple JOINs (" + joinCount + ") may impact performance.");
        }
        
        // Check for subqueries
        if (upperSQL.contains("(SELECT") || upperSQL.contains(" SELECT ")) {
            long subqueryCount = upperSQL.split("\\(SELECT", -1).length - 1;
            if (subqueryCount > 2) {
                feedback.add("   ⚠️ Complexity: Multiple subqueries may impact performance.");
            }
        }
        
        // Check for DISTINCT without need
        if (upperSQL.contains(" DISTINCT ") && !upperSQL.contains(" GROUP BY ")) {
            feedback.add("   ℹ️ Consider: DISTINCT can be expensive on large tables.");
        }
        
        return feedback;
    }

    private List<String> analyzeDelete(Delete delete, String upperSQL) {
        List<String> feedback = new ArrayList<>();
        
        feedback.add("🗑️ DELETE statement analysis:");
        
        // Check for missing WHERE clause
        if (delete.getWhere() == null) {
            feedback.add("   🟥 Critical: DELETE without WHERE will remove ALL rows from table.");
        } else {
            feedback.add("   ⚠️ Caution: DELETE operation - ensure proper backup.");
        }
        
        // Check for LIMIT (good practice for safety)
        if (!upperSQL.contains(" LIMIT ")) {
            feedback.add("   ✅ Recommendation: Add LIMIT clause for safety during testing.");
        }
        
        return feedback;
    }

    private List<String> analyzeUpdate(Update update, String upperSQL) {
        List<String> feedback = new ArrayList<>();
        
        feedback.add("✏️ UPDATE statement analysis:");
        
        // Check for missing WHERE clause
        if (update.getWhere() == null) {
            feedback.add("   🟥 Critical: UPDATE without WHERE will modify ALL rows in table.");
        } else {
            feedback.add("   ⚠️ Caution: UPDATE operation - verify WHERE clause accuracy.");
        }
        
        // Check if updating primary key or unique columns
        if (upperSQL.contains(" ID = ") || upperSQL.contains("_ID = ") || 
            upperSQL.contains(" PRIMARY") || upperSQL.contains(" UNIQUE")) {
            feedback.add("   ⚠️ Warning: Updating identifier columns may break relationships.");
        }
        
        // Check for LIMIT
        if (!upperSQL.contains(" LIMIT ")) {
            feedback.add("   ✅ Recommendation: Add LIMIT clause for safety during testing.");
        }
        
        return feedback;
    }

    private List<String> analyzeInsert(Insert insert, String upperSQL) {
        List<String> feedback = new ArrayList<>();
        
        feedback.add("📥 INSERT statement analysis:");
        
        // Check for explicit column list
        if (insert.getColumns() == null || insert.getColumns().isEmpty()) {
            feedback.add("   ⚠️ Best Practice: Specify column names in INSERT statements.");
        }
        
        // Check for bulk inserts
        if (upperSQL.contains("VALUES (") && upperSQL.split("VALUES \\(").length > 2) {
            feedback.add("   ℹ️ Info: Multiple value sets detected - consider batch size limits.");
        }
        
        // Check for INSERT without column list
        if (upperSQL.contains("INSERT INTO") && !upperSQL.contains("(") && upperSQL.contains("VALUES")) {
            feedback.add("   ⚠️ Risk: Implicit column mapping - specify columns explicitly.");
        }
        
        return feedback;
    }

    private List<String> basicAnalysis(String upperSQL) {
        List<String> feedback = new ArrayList<>();
        
        // Fallback analysis when parsing fails
        feedback.add("🔍 Basic statement analysis:");
        
        // Safety checks
        if (upperSQL.contains("DROP")) {
            feedback.add("   🟥 Dangerous: DROP statement detected.");
        }
        if (upperSQL.contains("TRUNCATE")) {
            feedback.add("   🟥 Dangerous: TRUNCATE statement detected.");
        }
        if (upperSQL.contains("DELETE") && !upperSQL.contains("WHERE")) {
            feedback.add("   ⚠️ Risk: DELETE without WHERE clause.");
        }
        if (upperSQL.contains("UPDATE") && !upperSQL.contains("WHERE")) {
            feedback.add("   ⚠️ Risk: UPDATE without WHERE clause.");
        }
        
        return feedback;
    }

    private List<String> checkSecurityIssues(String upperSQL) {
        List<String> feedback = new ArrayList<>();
        
        // SQL Injection patterns
        if (upperSQL.contains(" OR 1=1") || upperSQL.contains("' OR '") || 
            upperSQL.contains("' OR '1'='1") || upperSQL.contains(" UNION ") ||
            upperSQL.contains("; DROP") || upperSQL.contains(" EXEC ") ||
            upperSQL.contains(" EXECUTE ") || upperSQL.contains(" XP_")) {
            feedback.add("🛡️ Security: Possible SQL injection pattern detected.");
        }
        
        // Dynamic SQL
        if (upperSQL.contains("EXEC(") || upperSQL.contains("EXECUTE(") || 
            upperSQL.contains("SP_") || upperSQL.contains("XP_")) {
            feedback.add("⚠️ Security: Dynamic SQL execution detected.");
        }
        
        return feedback;
    }

    private List<String> checkPerformanceIssues(String upperSQL) {
        List<String> feedback = new ArrayList<>();
        
        // LIKE with leading wildcard
        if (upperSQL.contains(" LIKE '%") || upperSQL.contains(" LIKE \"%")) {
            feedback.add("🐌 Performance: Leading wildcard in LIKE prevents index usage.");
        }
        
        // Functions on indexed columns
        if (upperSQL.matches(".*WHERE\\s+[A-Z]+\\s*\\([^)]+\\)\\s*[=<>].*")) {
            feedback.add("🐌 Performance: Functions in WHERE clause may prevent index usage.");
        }
        
        // Implicit type conversion
        if (upperSQL.matches(".*WHERE\\s+[a-zA-Z_]+\\s*=\\s*'[0-9]+'.*") ||
            upperSQL.matches(".*WHERE\\s+[a-zA-Z_]+\\s*=\\s*[0-9]+\\..*")) {
            feedback.add("ℹ️ Performance: Possible implicit type conversion in WHERE clause.");
        }
        
        // Missing indexes suggestion
        if ((upperSQL.contains(" WHERE ") || upperSQL.contains(" JOIN ")) && 
            !upperSQL.contains("INDEX") && !upperSQL.contains("PRIMARY") && 
            !upperSQL.contains("UNIQUE")) {
            feedback.add("💡 Optimization: Consider indexing columns used in WHERE/JOIN clauses.");
        }
        
        return feedback;
    }
}