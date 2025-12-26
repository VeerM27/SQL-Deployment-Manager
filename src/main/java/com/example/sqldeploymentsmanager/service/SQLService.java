package com.example.sqldeploymentsmanager.service;

import com.example.sqldeploymentsmanager.model.SQLScript;
import com.example.sqldeploymentsmanager.repository.SQLScriptRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Service for managing SQL scripts.
 */
@Service
public class SQLService {

    private final SQLScriptRepository repo;

    public SQLService(SQLScriptRepository repo) {
        this.repo = repo;
    }

    /**
     * Saves an uploaded SQL file to the database.
     * Uses UTF-8 encoding to prevent character corruption.
     *
     * @param file The uploaded SQL file
     * @return The saved SQLScript entity
     * @throws IOException if file reading fails
     */
    public SQLScript saveSQLFile(MultipartFile file) throws IOException {
        SQLScript script = new SQLScript();
        script.setFilename(file.getOriginalFilename());
        script.setContent(new String(file.getBytes(), StandardCharsets.UTF_8));
        return repo.save(script);
    }

    public SQLScript saveSQLText(String sqlText) {
        SQLScript script = new SQLScript();
        script.setFilename("Pasted SQL - " + System.currentTimeMillis());
        script.setContent(sqlText);
        return repo.save(script);  // ✅ fixed reference
    }

    public List<SQLScript> getAllScripts() {
        return repo.findAll();
    }
}
