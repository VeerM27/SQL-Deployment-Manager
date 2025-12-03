package com.example.sqldeploymentsmanager.controller;

import com.example.sqldeploymentsmanager.service.HistoryService;
import com.example.sqldeploymentsmanager.service.SchemaComparisonService;
import com.example.sqldeploymentsmanager.service.SelectQueryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class SchemaComparisonController {

    private final SchemaComparisonService comparisonService;
    private final SelectQueryService selectQueryService;
    private final HistoryService historyService;

    public SchemaComparisonController(
            SchemaComparisonService comparisonService,
            SelectQueryService selectQueryService,
            HistoryService historyService
    ) {
        this.comparisonService = comparisonService;
        this.selectQueryService = selectQueryService;
        this.historyService = historyService;
    }

    @GetMapping("/comparison")
    public String compareSchemas(HttpSession session, Model model) {

        String sql = (String) session.getAttribute("lastSQL");

        if (sql == null || sql.isBlank()) {
            model.addAttribute("error", "No SQL script found. Please enter SQL in Development.");
            return "comparison";
        }

        model.addAttribute("sqlScript", sql);

        // ===============================
        // SELECT QUERY PREVIEW MODE
        // ===============================
        if (sql.trim().toUpperCase().startsWith("SELECT")) {
            try {
                List<Map<String, Object>> rows = selectQueryService.executeSelect(sql);

                model.addAttribute("selectResults", rows);
                model.addAttribute("isSelect", true);

                historyService.logAction("SELECT Preview", "Schema Comparison", "SUCCESS",
                        "Previewed query results");

            } catch (Exception ex) {
                model.addAttribute("error", "Failed to execute SELECT: " + ex.getMessage());

                historyService.logAction("SELECT Preview", "Schema Comparison", "FAILED",
                        ex.getMessage());
            }

            return "comparison"; // Do NOT run comparison logic for SELECT
        }

        // ===============================
        // ORIGINAL COMPARISON FUNCTIONALITY
        // ===============================
        List<String> results = comparisonService.compareWithDatabase(sql);
        model.addAttribute("results", results);

        return "comparison";
    }
}
