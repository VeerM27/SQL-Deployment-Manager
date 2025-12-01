package com.example.sqldeploymentsmanager.controller;

import com.example.sqldeploymentsmanager.service.HistoryService;
import com.example.sqldeploymentsmanager.service.WorkflowService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/workflow")
public class WorkflowController {

    private final WorkflowService workflowService;
    private final HistoryService historyService;

    public WorkflowController(WorkflowService workflowService, HistoryService historyService) {
        this.workflowService = workflowService;
        this.historyService = historyService;
    }

    @GetMapping
    public String showWorkflowPage(HttpSession session, Model model) {
        String lastSQL = (String) session.getAttribute("lastSQL");
        System.out.println("GET /workflow - lastSQL: " + (lastSQL != null ? "exists" : "null"));
        
        if (lastSQL == null || lastSQL.isBlank()) {
            model.addAttribute("hasSQL", false);
        } else {
            workflowService.setLastSQL(lastSQL);
            model.addAttribute("hasSQL", true);
            model.addAttribute("currentStatus", workflowService.getCurrentStatus());
            model.addAttribute("workflowHistory", workflowService.getWorkflowHistory());
            model.addAttribute("sqlScript", workflowService.getLastSQL());
        }
        return "workflow";
    }

    @PostMapping("/validate")
    public String validate(HttpSession session, Model model) {
        System.out.println("POST /workflow/validate");
        
        String lastSQL = (String) session.getAttribute("lastSQL");
        if (lastSQL != null) {
            workflowService.setLastSQL(lastSQL);
        }
        
        String msg = workflowService.validateSQL();
        System.out.println("Validation result: " + msg);
        
        model.addAttribute("message", msg);
        // REMOVED DUPLICATE: historyService.logAction("Validate", "Workflow", workflowService.getCurrentStatus().toString(), msg);
        
        addWorkflowAttributes(model);
        return "workflow";
    }

    @PostMapping("/backup")
    public String backup(HttpSession session, Model model) {
        System.out.println("POST /workflow/backup");
        
        String msg = workflowService.backupDatabase();
        System.out.println("Backup result: " + msg);
        
        model.addAttribute("message", msg);
        // REMOVED DUPLICATE: historyService.logAction("Backup", "Workflow", workflowService.getCurrentStatus().toString(), msg);
        
        addWorkflowAttributes(model);
        return "workflow";
    }

    @PostMapping("/approve")
    public String approve(HttpSession session, Model model) {
        System.out.println("POST /workflow/approve");
        
        String msg = workflowService.approveDeployment();
        System.out.println("Approve result: " + msg);
        
        model.addAttribute("message", msg);
        // REMOVED DUPLICATE: historyService.logAction("Approval", "Workflow", workflowService.getCurrentStatus().toString(), msg);
        
        addWorkflowAttributes(model);
        return "workflow";
    }

    @PostMapping("/deploy")
    public String deploy(HttpSession session, Model model) {
        System.out.println("POST /workflow/deploy");
        
        String msg = workflowService.deployToDatabase();
        System.out.println("Deploy result: " + msg);
        
        model.addAttribute("message", msg);
        // REMOVED DUPLICATE: historyService.logAction("Deploy", "Workflow", workflowService.getCurrentStatus().toString(), msg);
        
        addWorkflowAttributes(model);
        return "workflow";
    }

    @PostMapping("/reset")
    public String resetWorkflow(HttpSession session, Model model) {
        System.out.println("POST /workflow/reset");
        
        String lastSQL = (String) session.getAttribute("lastSQL");
        if (lastSQL != null) {
            workflowService.setLastSQL(lastSQL);
        }
        
        // Keep this one - reset is only logged here, not in WorkflowService
        historyService.logAction("Reset", "Workflow", workflowService.getCurrentStatus().toString(), "Workflow has been reset");
        
        model.addAttribute("message", "Workflow has been reset");
        addWorkflowAttributes(model);
        return "workflow";
    }

    private void addWorkflowAttributes(Model model) {
        model.addAttribute("hasSQL", true);
        model.addAttribute("currentStatus", workflowService.getCurrentStatus());
        model.addAttribute("workflowHistory", workflowService.getWorkflowHistory());
        model.addAttribute("sqlScript", workflowService.getLastSQL());
    }
}