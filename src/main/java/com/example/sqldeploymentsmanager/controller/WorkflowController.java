package com.example.sqldeploymentsmanager.controller;

import com.example.sqldeploymentsmanager.service.HistoryService;
import com.example.sqldeploymentsmanager.service.WorkflowService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/workflow")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping
    public String showWorkflowPage(HttpSession session, Model model) {
        String sql = (String) session.getAttribute("lastSQL");

        if (sql == null || sql.isBlank()) {
            model.addAttribute("hasSQL", false);
            return "workflow";
        }

        if (workflowService.getLastSQL() == null ||
            !workflowService.getLastSQL().equals(sql)) {
            workflowService.setLastSQL(sql);
        }

        model.addAttribute("hasSQL", true);
        model.addAttribute("currentStatus", workflowService.getCurrentStatus());
        model.addAttribute("workflowHistory", workflowService.getWorkflowHistory());
        return "workflow";
    }

    @PostMapping("/validate")
    public String validate(RedirectAttributes redirect) {
        redirect.addFlashAttribute("message", workflowService.validateSQL());
        return "redirect:/workflow";
    }

    @PostMapping("/backup")
    public String backup(RedirectAttributes redirect) {
        redirect.addFlashAttribute("message", workflowService.backupDatabase());
        return "redirect:/workflow";
    }

    @PostMapping("/approve")
    public String approve(RedirectAttributes redirect) {
        redirect.addFlashAttribute("message", workflowService.approveDeployment());
        return "redirect:/workflow";
    }

    @PostMapping("/deploy")
    public String deploy(RedirectAttributes redirect) {
        redirect.addFlashAttribute("message", workflowService.deployToDatabase());
        return "redirect:/workflow";
    }

    @PostMapping("/reset")
    public String reset(RedirectAttributes redirect) {
        workflowService.reset();
        redirect.addFlashAttribute("message", "Workflow reset.");
        return "redirect:/workflow";
    }
}
