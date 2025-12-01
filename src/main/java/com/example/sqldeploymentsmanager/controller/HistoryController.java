package com.example.sqldeploymentsmanager.controller;

import com.example.sqldeploymentsmanager.service.HistoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public String showHistoryPage(Model model) {
        model.addAttribute("history", historyService.getHistory());
        return "history";
    }

    @PostMapping("/clear")
    public String clearHistory(Model model) {
        historyService.clearHistory();
        model.addAttribute("message", "🧹 History cleared successfully.");
        model.addAttribute("history", historyService.getHistory());
        return "history";
    }
}
