package com.example.sqldeploymentsmanager.service;

import com.example.sqldeploymentsmanager.model.HistoryRecord;
import com.example.sqldeploymentsmanager.repository.HistoryRecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for managing history records.
 * Now persists to database instead of in-memory storage.
 */
@Service
public class HistoryService {

    private final HistoryRecordRepository historyRepository;

    public HistoryService(HistoryRecordRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    /**
     * Logs an action to the history database.
     *
     * @param action Action performed
     * @param source Source of the action
     * @param status Status of the action
     * @param details Additional details
     */
    public void logAction(String action, String source, String status, String details) {
        HistoryRecord record = new HistoryRecord(action, source, status, details);
        historyRepository.save(record);
    }

    /**
     * Retrieves all history records ordered by timestamp (newest first).
     *
     * @return List of history records
     */
    public List<HistoryRecord> getHistory() {
        return historyRepository.findAllByOrderByTimestampDesc();
    }

    /**
     * Clears all history records from the database.
     * Use with caution - this is permanent!
     */
    public void clearHistory() {
        historyRepository.deleteAll();
    }
}
