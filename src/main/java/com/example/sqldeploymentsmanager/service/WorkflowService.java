package com.example.sqldeploymentsmanager.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

@Service
@SessionScope
public class WorkflowService {

    private String lastSQL;
    private WorkflowStatus currentStatus = WorkflowStatus.DRAFT;
    private final List<String> workflowHistory = new ArrayList<>();

    private final HistoryService historyService;
    private final RowImpactService rowImpactService;
    private final SchemaImpactService schemaImpactService;
    private final BackupService backupService;   // ✅ FIX 1: FIELD ADDED

    // Impact captured from last deployment
    private List<String> lastSchemaImpact;
    private String lastRowImpactSummary;

    // ✅ FIX 2: CONSTRUCTOR UPDATED TO INCLUDE BackupService
    public WorkflowService(HistoryService historyService,
                           RowImpactService rowImpactService,
                           SchemaImpactService schemaImpactService,
                           BackupService backupService) {
        this.historyService = historyService;
        this.rowImpactService = rowImpactService;
        this.schemaImpactService = schemaImpactService;
        this.backupService = backupService;   // ✅ FIX 3: ASSIGNMENT ADDED
    }

    public enum WorkflowStatus {
        DRAFT, VALIDATED, BACKUP_CREATED, APPROVAL_PENDING, APPROVED, COMPLETED
    }

    public void setLastSQL(String sql) {
        this.lastSQL = sql;
        this.currentStatus = WorkflowStatus.DRAFT;
        this.workflowHistory.clear();
        this.lastSchemaImpact = null;
        this.lastRowImpactSummary = null;

        addToHistory("Workflow initialized with SQL script");
        historyService.logAction("Workflow Initialized", "Workflow",
                currentStatus.toString(), "Workflow started");
    }

    public String validateSQL() {
        System.out.println("WorkflowService.validateSQL() called");

        if (lastSQL == null || lastSQL.isBlank()) {
            currentStatus = WorkflowStatus.DRAFT;
            addToHistory("Validation failed: No SQL found");
            historyService.logAction("Validate SQL", "Workflow",
                    "FAILED", "No SQL found to validate");
            return "⚠️ No SQL found to validate.";
        }

        currentStatus = WorkflowStatus.VALIDATED;
        String result = "✅ SQL validation passed successfully!";
        addToHistory(result);
        historyService.logAction("Validate SQL", "Workflow",
                "SUCCESS", "SQL validation passed");
        return result;
    }

    // ✅ REAL FULL MYSQL BACKUP
    public String backupDatabase() {
        System.out.println("WorkflowService.backupDatabase() called");

        if (currentStatus != WorkflowStatus.VALIDATED) {
            addToHistory("Backup failed: Please validate SQL first");
            historyService.logAction("Backup Database", "Workflow",
                    "FAILED", "Cannot backup - SQL not validated");
            return "⚠️ Please validate SQL first before backup.";
        }

        try {
            String backupPath = backupService.createFullBackup();
            currentStatus = WorkflowStatus.BACKUP_CREATED;

            String result = "✅ Full database backup created at: " + backupPath;

            addToHistory(result);
            historyService.logAction("Backup Database", "Workflow",
                    "SUCCESS", "Backup created at " + backupPath);

            return result;

        } catch (Exception ex) {
            String msg = "❌ Backup failed: " + ex.getMessage();
            addToHistory(msg);
            historyService.logAction("Backup Database", "Workflow",
                    "FAILED", ex.getMessage());
            return msg;
        }
    }

    public String approveDeployment() {
        System.out.println("WorkflowService.approveDeployment() called");

        if (currentStatus == WorkflowStatus.BACKUP_CREATED) {
            currentStatus = WorkflowStatus.APPROVAL_PENDING;
            addToHistory("Approval requested");
            historyService.logAction("Request Approval", "Workflow",
                    "PENDING", "Approval requested");
            return "✅ Approval requested. Ready for final approval.";
        } else if (currentStatus == WorkflowStatus.APPROVAL_PENDING) {
            currentStatus = WorkflowStatus.APPROVED;
            addToHistory("Deployment approved");
            historyService.logAction("Approve Deployment", "Workflow",
                    "APPROVED", "Deployment approved");
            return "✅ Deployment approved. Ready to execute.";
        } else {
            historyService.logAction("Approve Deployment", "Workflow",
                    "FAILED", "Cannot approve at current stage");
            return "⚠️ Cannot approve at current workflow stage.";
        }
    }

    public String deployToDatabase() {
        System.out.println("WorkflowService.deployToDatabase() called");

        if (currentStatus != WorkflowStatus.APPROVED) {
            historyService.logAction("Deploy to Database", "Workflow",
                    "FAILED", "Deployment not approved");
            return "⚠️ Deployment must be approved first.";
        }

        if (lastSQL == null || lastSQL.isBlank()) {
            historyService.logAction("Deploy to Database", "Workflow",
                    "FAILED", "No SQL script available");
            return "⚠️ No SQL script available to deploy.";
        }

        try {
            SchemaImpactService.SchemaSnapshot before = schemaImpactService.captureSnapshot();
            RowImpactService.RowImpactSummary rowImpact = rowImpactService.executeWithImpact(lastSQL);
            SchemaImpactService.SchemaSnapshot after = schemaImpactService.captureSnapshot();

            this.lastSchemaImpact = schemaImpactService.diff(before, after);
            this.lastRowImpactSummary = String.format(
                    "INSERT: %d, UPDATE: %d, DELETE: %d",
                    rowImpact.getInsertCount(),
                    rowImpact.getUpdateCount(),
                    rowImpact.getDeleteCount()
            );

            currentStatus = WorkflowStatus.COMPLETED;

            String result = "🚀 Deployment executed successfully. " +
                    "Review the Schema Comparison page for schema and row impact.";
            addToHistory(result);
            historyService.logAction("Deploy to Database", "Workflow",
                    "SUCCESS", "Deployment executed with impact captured");

            return result;

        } catch (Exception ex) {
            String msg = "❌ Deployment failed: " + ex.getMessage();
            addToHistory(msg);
            historyService.logAction("Deploy to Database", "Workflow",
                    "FAILED", ex.getMessage());
            return msg;
        }
    }

    public void reset() {
        this.currentStatus = WorkflowStatus.DRAFT;
        this.workflowHistory.clear();
        this.lastSchemaImpact = null;
        this.lastRowImpactSummary = null;
        addToHistory("Workflow reset");
        historyService.logAction("Reset", "Workflow",
                "DRAFT", "Workflow has been reset");
    }

    private void addToHistory(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
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

    public List<String> getLastSchemaImpact() {
        return lastSchemaImpact;
    }

    public String getLastRowImpactSummary() {
        return lastRowImpactSummary;
    }
}
