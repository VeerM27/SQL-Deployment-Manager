package com.example.sqldeploymentsmanager.controller;

import com.example.sqldeploymentsmanager.service.HistoryService;
import com.example.sqldeploymentsmanager.service.SQLAnalysisService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class DevelopmentController {

    private final SQLAnalysisService sqlAnalysisService;
    private final HistoryService historyService;

    public DevelopmentController(SQLAnalysisService sqlAnalysisService, HistoryService historyService) {
        this.sqlAnalysisService = sqlAnalysisService;
        this.historyService = historyService;
    }

    @GetMapping("/development")
    public String showDevelopmentPage() {
        return "development";
    }

    @PostMapping("/upload-sql")
    public String uploadSQL(@RequestParam(value = "sqlText", required = false) String sqlText,
                           @RequestParam(value = "file", required = false) MultipartFile file,
                           HttpSession session,
                           Model model) {
        
        try {
            String finalSQL = "";
            String actionType = "";
            
            // Handle file upload
            if (file != null && !file.isEmpty()) {
                // Basic file validation
                String fileName = file.getOriginalFilename();
                if (fileName == null || !fileName.toLowerCase().endsWith(".sql")) {
                    model.addAttribute("error", "❌ Please upload a .sql file.");
                    return "development";
                }
                finalSQL = new String(file.getBytes());
                actionType = "File Upload";
            } 
            // Handle text input
            else if (sqlText != null && !sqlText.trim().isEmpty()) {
                finalSQL = sqlText.trim();
                actionType = "Text Input";
            }
            // No input provided
            else {
                model.addAttribute("error", "❌ Please provide SQL either by uploading a file or pasting text.");
                return "development";
            }
            
            // Basic SQL validation
            if (!isValidSQL(finalSQL)) {
                model.addAttribute("error", "❌ Invalid SQL. Please provide valid SQL syntax.");
                historyService.logAction("SQL Upload", "Development", "FAILED", "Invalid SQL syntax");
                return "development";
            }
            
            // SQL is valid, store in session
            session.setAttribute("lastSQL", finalSQL);
            model.addAttribute("success", "✅ SQL saved successfully!");
            model.addAttribute("fileContent", finalSQL);
            model.addAttribute("fileName", file != null ? file.getOriginalFilename() : "Current SQL Script");
            
            // Log successful SQL upload
            String details = actionType + " - " + finalSQL.length() + " characters";
            historyService.logAction("SQL Upload", "Development", "SUCCESS", details);
            
        } catch (Exception e) {
            model.addAttribute("error", "❌ Error: " + e.getMessage());
            historyService.logAction("SQL Upload", "Development", "ERROR", e.getMessage());
        }
        
        return "development";
    }

    private boolean isValidSQL(String sql) {
        if (sql == null || sql.trim().isEmpty()) return false;
        
        String upperSQL = sql.toUpperCase().trim();
        
        // Must start with SQL keyword
        String[] validStarts = {"SELECT", "INSERT", "UPDATE", "DELETE", "CREATE", "ALTER", "DROP", "WITH"};
        for (String start : validStarts) {
            if (upperSQL.startsWith(start + " ") || upperSQL.startsWith(start + "\n") || upperSQL.startsWith(start + "\t")) {
                return true;
            }
        }
        
        return false;
    }
}