package com.example.sqldeploymentsmanager.controller;

import com.example.sqldeploymentsmanager.service.HistoryService;
import com.example.sqldeploymentsmanager.service.SchemaComparisonService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class SchemaComparisonController {

    private final SchemaComparisonService comparisonService;
    private final HistoryService historyService;

    public SchemaComparisonController(SchemaComparisonService comparisonService, HistoryService historyService) {
        this.comparisonService = comparisonService;
        this.historyService = historyService;
    }

    @GetMapping("/comparison")
    public String showComparisonPage(HttpSession session, Model model) {
        String sqlText = (String) session.getAttribute("lastSQL");

        if (sqlText == null || sqlText.isBlank()) {
            model.addAttribute("warning", "⚠️ No SQL query found. Please upload or enter SQL in the Development page first.");
            return "comparison";
        }

        List<String> results = comparisonService.compareWithDatabase(sqlText);
        model.addAttribute("results", results);
        
        // Log the comparison view
        historyService.logAction("View Comparison", "Schema Comparison", "VIEW", "Displayed schema comparison results");
        
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

        // Run the comparison and log it
        List<String> results = comparisonService.compareWithDatabase(sqlText);
        String resultSummary = results.size() + " comparison results generated";
        historyService.logAction("Run Comparison", "Schema Comparison", "SUCCESS", resultSummary);
        
        redirectAttributes.addFlashAttribute("success", "✅ Schema comparison completed successfully.");
        return "redirect:/comparison";
    }
}