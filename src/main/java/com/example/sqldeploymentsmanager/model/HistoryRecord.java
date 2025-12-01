package com.example.sqldeploymentsmanager.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HistoryRecord {

    private String action;
    private String source;
    private String status;
    private String details;
    private String timestamp;

    public HistoryRecord(String action, String source, String status, String details) {
        this.action = action;
        this.source = source;
        this.status = status;
        this.details = details;
        this.timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Getters and Setters
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
