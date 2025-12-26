package com.example.sqldeploymentsmanager.repository;

import com.example.sqldeploymentsmanager.model.HistoryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for persisting and retrieving history records.
 */
@Repository
public interface HistoryRecordRepository extends JpaRepository<HistoryRecord, Long> {
    
    /**
     * Find all history records ordered by timestamp descending (newest first).
     * @return List of history records
     */
    List<HistoryRecord> findAllByOrderByTimestampDesc();
}

