package com.example.sqldeploymentsmanager.service;

import com.example.sqldeploymentsmanager.model.HistoryRecord;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class HistoryService {

    private final List<HistoryRecord> history = new ArrayList<>();

    public void logAction(String action, String source, String status, String details) {
        history.add(new HistoryRecord(action, source, status, details));
    }

    public List<HistoryRecord> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public void clearHistory() {
        history.clear();
    }
}
