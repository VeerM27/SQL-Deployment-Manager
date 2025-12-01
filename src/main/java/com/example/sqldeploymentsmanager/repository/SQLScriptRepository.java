package com.example.sqldeploymentsmanager.repository;

import com.example.sqldeploymentsmanager.model.SQLScript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SQLScriptRepository extends JpaRepository<SQLScript, Long> {
}
