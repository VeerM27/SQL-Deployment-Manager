package com.example.sqldeploymentsmanager.service;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@SessionScope
public class WorkflowService {

    private String lastSQL;
    private WorkflowStatus currentStatus = WorkflowStatus.DRAFT;
    private List<String> workflowHistory = new ArrayList<>();
    private final HistoryService historyService;

    // Add constructor with HistoryService
    public WorkflowService(HistoryService historyService) {
        this.historyService = historyService;
    }

    // ALL YOUR EXISTING CODE STAYS EXACTLY THE SAME...
    // DON'T CHANGE ANY OF YOUR EXISTING METHODS

    public enum WorkflowStatus {
        DRAFT, VALIDATED, BACKUP_CREATED, APPROVAL_PENDING, APPROVED, COMPLETED
    }

    public void setLastSQL(String sql) {
        this.lastSQL = sql;
        this.currentStatus = WorkflowStatus.DRAFT;
        this.workflowHistory.clear();
        addToHistory("Workflow initialized with SQL script");
        // ADD THIS ONE LINE:
        historyService.logAction("Workflow Initialized", "Workflow", currentStatus.toString(), "Workflow started");
    }

    public String validateSQL() {
        System.out.println("WorkflowService.validateSQL() called");
        
        if (lastSQL == null || lastSQL.isBlank()) {
            currentStatus = WorkflowStatus.DRAFT;
            addToHistory("Validation failed: No SQL found");
            // ADD THIS ONE LINE:
            historyService.logAction("Validate SQL", "Workflow", "FAILED", "No SQL found to validate");
            return "⚠️ No SQL found to validate.";
        }
        
        currentStatus = WorkflowStatus.VALIDATED;
        String result = "✅ SQL validation passed successfully!";
        addToHistory(result);
        // ADD THIS ONE LINE:
        historyService.logAction("Validate SQL", "Workflow", "SUCCESS", "SQL validation passed");
        return result;
    }

    public String backupDatabase() {
        System.out.println("WorkflowService.backupDatabase() called");
        
        if (currentStatus != WorkflowStatus.VALIDATED) {
            addToHistory("Backup failed: Please validate SQL first");
            // ADD THIS ONE LINE:
            historyService.logAction("Backup Database", "Workflow", "FAILED", "Cannot backup - SQL not validated");
            return "⚠️ Please validate SQL first before backup.";
        }
        
        currentStatus = WorkflowStatus.BACKUP_CREATED;
        String result = "✅ Backup simulation completed successfully.";
        addToHistory(result);
        // ADD THIS ONE LINE:
        historyService.logAction("Backup Database", "Workflow", "SUCCESS", "Backup simulation completed");
        return result;
    }

    public String approveDeployment() {
        System.out.println("WorkflowService.approveDeployment() called");
        
        if (currentStatus == WorkflowStatus.BACKUP_CREATED) {
            currentStatus = WorkflowStatus.APPROVAL_PENDING;
            addToHistory("Approval requested");
            // ADD THIS ONE LINE:
            historyService.logAction("Request Approval", "Workflow", "PENDING", "Approval requested");
            return "✅ Approval requested. Ready for final approval.";
        } else if (currentStatus == WorkflowStatus.APPROVAL_PENDING) {
            currentStatus = WorkflowStatus.APPROVED;
            addToHistory("Deployment approved");
            // ADD THIS ONE LINE:
            historyService.logAction("Approve Deployment", "Workflow", "APPROVED", "Deployment approved");
            return "✅ Deployment approved. Ready to execute.";
        } else {
            // ADD THIS ONE LINE:
            historyService.logAction("Approve Deployment", "Workflow", "FAILED", "Cannot approve at current stage");
            return "⚠️ Cannot approve at current workflow stage.";
        }
    }

    public String deployToDatabase() {
        System.out.println("WorkflowService.deployToDatabase() called");
        
        if (currentStatus != WorkflowStatus.APPROVED) {
            // ADD THIS ONE LINE:
            historyService.logAction("Deploy to Database", "Workflow", "FAILED", "Deployment not approved");
            return "⚠️ Deployment must be approved first.";
        }
        
        currentStatus = WorkflowStatus.COMPLETED;
        String result = "🚀 Deployment simulation successful!";
        addToHistory(result);
        // ADD THIS ONE LINE:
        historyService.logAction("Deploy to Database", "Workflow", "SUCCESS", "Deployment simulation completed");
        return result;
    }

    // KEEP ALL YOUR EXISTING METHODS EXACTLY AS THEY ARE
    private void addToHistory(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        workflowHistory.add(timestamp + " - " + message);
        System.out.println("History added: " + message);
    }

    public WorkflowStatus getCurrentStatus() {
        return currentStatus;
    }

    public List<String> getWorkflowHistory() {
        return workflowHistory;
    }

    public String getLastSQL() {
        return lastSQL;
    }
}