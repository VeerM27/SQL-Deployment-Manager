package com.example.sqldeploymentsmanager.controller;

import com.example.sqldeploymentsmanager.service.HistoryService;
import com.example.sqldeploymentsmanager.service.SQLAnalysisService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SQLAnalysisController {

    private final SQLAnalysisService sqlAnalysisService;
    private final HistoryService historyService;

    public SQLAnalysisController(SQLAnalysisService sqlAnalysisService, HistoryService historyService) {
        this.sqlAnalysisService = sqlAnalysisService;
        this.historyService = historyService;
    }

    @GetMapping("/analysis")
    public String showAnalysisPage(HttpSession session, Model model) {
        String sqlText = (String) session.getAttribute("lastSQL");

        model.addAttribute("sqlText", sqlText);

        if (sqlText != null && !sqlText.isBlank()) {
            // Store the analysis results in BOTH attributes for template compatibility
            var analysisResults = sqlAnalysisService.smartAnalyze(sqlText);
            model.addAttribute("analysisResults", analysisResults);
            model.addAttribute("feedback", analysisResults);
            
            // Log the analysis view
            historyService.logAction("View Analysis", "SQL Analysis", "VIEW", "Displayed SQL analysis results");
        }
        return "analysis";
    }

    @PostMapping("/analysis")
    public String runAnalysis(HttpSession session) {
        String sqlText = (String) session.getAttribute("lastSQL");
        if (sqlText == null || sqlText.isBlank()) {
            historyService.logAction("Run Analysis", "SQL Analysis", "FAILED", "No SQL found to analyze");
            return "redirect:/development";
        }
        
        // Run analysis and log it
        var analysisResults = sqlAnalysisService.smartAnalyze(sqlText);
        String resultSummary = "Analysis completed - " + analysisResults.size() + " findings";
        historyService.logAction("Run Analysis", "SQL Analysis", "SUCCESS", resultSummary);
        
        return "redirect:/analysis";
    }
}