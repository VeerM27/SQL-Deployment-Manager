package com.example.sqldeploymentsmanager.controller;

import com.example.sqldeploymentsmanager.service.HistoryService;
import com.example.sqldeploymentsmanager.service.SchemaComparisonService;
import com.example.sqldeploymentsmanager.service.SelectQueryService;
import com.example.sqldeploymentsmanager.service.WorkflowService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
public class SchemaComparisonController {

    private final SchemaComparisonService comparisonService;
    private final SelectQueryService selectQueryService;
    private final WorkflowService workflowService;
    private final HistoryService historyService;

    public SchemaComparisonController(SchemaComparisonService comparisonService,
                                      SelectQueryService selectQueryService,
                                      WorkflowService workflowService,
                                      HistoryService historyService) {
        this.comparisonService = comparisonService;
        this.selectQueryService = selectQueryService;
        this.workflowService = workflowService;
        this.historyService = historyService;
    }

    @GetMapping("/comparison")
    public String showComparisonPage(HttpSession session, Model model) {
        String sqlText = (String) session.getAttribute("lastSQL");

        if (sqlText == null || sqlText.isBlank()) {
            model.addAttribute("warning", "⚠️ No SQL query found. Please upload or enter SQL in the Development page first.");
            // Even if no SQL, still show last deployment impact if present
            model.addAttribute("schemaImpact", workflowService.getLastSchemaImpact());
            model.addAttribute("rowImpactSummary", workflowService.getLastRowImpactSummary());
            return "comparison";
        }

        model.addAttribute("sqlScript", sqlText);

        String upper = sqlText.trim().toUpperCase();
        boolean isSelect = upper.startsWith("SELECT");

        if (isSelect) {
            try {
                List<Map<String, Object>> rows = selectQueryService.executeSelect(sqlText);
                model.addAttribute("selectResults", rows);
                model.addAttribute("isSelect", true);

                historyService.logAction("SELECT Preview", "Schema Comparison", "SUCCESS",
                        "Previewed query results");
            } catch (Exception ex) {
                model.addAttribute("warning", "Failed to execute SELECT: " + ex.getMessage());
                historyService.logAction("SELECT Preview", "Schema Comparison", "FAILED",
                        ex.getMessage());
            }
        } else {
            List<String> results = comparisonService.compareWithDatabase(sqlText);
            model.addAttribute("results", results);

            historyService.logAction("View Comparison", "Schema Comparison", "VIEW",
                    "Displayed schema comparison results");
        }

        // Always attach last deployment impact, if any
        model.addAttribute("schemaImpact", workflowService.getLastSchemaImpact());
        model.addAttribute("rowImpactSummary", workflowService.getLastRowImpactSummary());

        return "comparison";
    }

    @PostMapping("/comparison")
    public String runComparison(HttpSession session, RedirectAttributes redirectAttributes) {
        String sqlText = (String) session.getAttribute("lastSQL");

        if (sqlText == null || sqlText.isBlank()) {
            redirectAttributes.addFlashAttribute("warning", "⚠️ No SQL query found. Please upload or enter SQL in the Development page first.");
            historyService.logAction("Run Comparison", "Schema Comparison", "FAILED", "No SQL found to compare");
            return "redirect:/development";
        }

        List<String> results = comparisonService.compareWithDatabase(sqlText);
        String resultSummary = results.size() + " comparison results generated";
        historyService.logAction("Run Comparison", "Schema Comparison", "SUCCESS", resultSummary);

        redirectAttributes.addFlashAttribute("success", "✅ Schema comparison completed successfully.");
        return "redirect:/comparison";
    }
}
