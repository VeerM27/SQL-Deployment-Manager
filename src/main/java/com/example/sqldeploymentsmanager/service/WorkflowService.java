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
    private final List<String> workflowHistory = new ArrayList<>();

    private final HistoryService historyService;
    private final DatabaseExecutorService executor;
    private final DatabaseBackupService backupService;

    public WorkflowService(
            HistoryService historyService,
            DatabaseExecutorService executor,
            DatabaseBackupService backupService
    ) {
        this.historyService = historyService;
        this.executor = executor;
        this.backupService = backupService;
    }

    public enum WorkflowStatus {
        DRAFT, VALIDATED, BACKUP_CREATED, APPROVAL_PENDING, APPROVED, COMPLETED
    }

    public void setLastSQL(String sql) {
        this.lastSQL = sql;
        this.currentStatus = WorkflowStatus.DRAFT;
        this.workflowHistory.clear();
        add("Workflow initialized");
        historyService.logAction("Init Workflow", "Workflow", "DRAFT", "SQL updated");
    }

    public String validateSQL() {
        if (lastSQL == null || lastSQL.isBlank()) {
            add("Validation failed: No SQL");
            historyService.logAction("Validate", "Workflow", "FAILED", "No SQL");
            return "⚠️ No SQL to validate.";
        }

        try {
            executor.validateSyntax(lastSQL);
            currentStatus = WorkflowStatus.VALIDATED;
            add("Validation successful");
            historyService.logAction("Validate", "Workflow", "SUCCESS", "SQL OK");
            return "SQL validated successfully.";
        } catch (Exception ex) {
            currentStatus = WorkflowStatus.DRAFT;
            add("Validation failed: " + ex.getMessage());
            historyService.logAction("Validate", "Workflow", "FAILED", ex.getMessage());
            return "❌ SQL validation failed: " + ex.getMessage();
        }
    }

    public String backupDatabase() {
        if (currentStatus != WorkflowStatus.VALIDATED) {
            add("Backup failed: Not validated");
            historyService.logAction("Backup", "Workflow", "FAILED", "Not validated");
            return "⚠️ Must validate SQL before backup.";
        }

        try {
            backupService.backupCurrentSchema();
            currentStatus = WorkflowStatus.BACKUP_CREATED;
            add("Backup completed");
            historyService.logAction("Backup", "Workflow", "SUCCESS", "Schema backed up");
            return "Backup completed successfully.";
        } catch (Exception ex) {
            add("Backup failed: " + ex.getMessage());
            historyService.logAction("Backup", "Workflow", "FAILED", ex.getMessage());
            return "❌ Backup failed: " + ex.getMessage();
        }
    }

    public String approveDeployment() {
        if (currentStatus == WorkflowStatus.BACKUP_CREATED) {
            currentStatus = WorkflowStatus.APPROVAL_PENDING;
            add("Approval requested");
            historyService.logAction("Approve", "Workflow", "PENDING", "Approval requested");
            return "Approval requested.";
        }

        if (currentStatus == WorkflowStatus.APPROVAL_PENDING) {
            currentStatus = WorkflowStatus.APPROVED;
            add("Deployment approved");
            historyService.logAction("Approve", "Workflow", "APPROVED", "Approved");
            return "Deployment approved.";
        }

        historyService.logAction("Approve", "Workflow", "FAILED", "Invalid stage");
        return "⚠️ You cannot approve at this stage.";
    }

    public String deployToDatabase() {
        if (currentStatus != WorkflowStatus.APPROVED) {
            historyService.logAction("Deploy", "Workflow", "FAILED", "Not approved");
            return "⚠️ Deployment must be approved first.";
        }

        try {
            executor.executeSql(lastSQL);
            currentStatus = WorkflowStatus.COMPLETED;
            add("Deployment completed");
            historyService.logAction("Deploy", "Workflow", "SUCCESS", "Deployment executed");
            return "Deployment executed successfully.";
        } catch (Exception ex) {
            add("Deployment failed: " + ex.getMessage());
            historyService.logAction("Deploy", "Workflow", "FAILED", ex.getMessage());
            return "❌ Deployment failed: " + ex.getMessage();
        }
    }

    public void reset() {
        currentStatus = WorkflowStatus.DRAFT;
        workflowHistory.clear();
        add("Workflow reset");
        historyService.logAction("Reset", "Workflow", "DRAFT", "Workflow reset");
    }

    private void add(String msg) {
        String ts = new SimpleDateFormat("HH:mm:ss").format(new Date());
        workflowHistory.add(ts + " - " + msg);
    }

    public WorkflowStatus getCurrentStatus() { return currentStatus; }
    public List<String> getWorkflowHistory() { return workflowHistory; }
    public String getLastSQL() { return lastSQL; }
}
