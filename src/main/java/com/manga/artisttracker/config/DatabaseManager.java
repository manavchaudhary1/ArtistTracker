package com.manga.artisttracker.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
@Slf4j
public class DatabaseManager {

    private static final String BACKUP_FILE = "src/main/resources/db-backup.zip";

    private final H2BackupService backupService;

    public DatabaseManager(H2BackupService backupService) {
        this.backupService = backupService;
    }

    @EventListener(ContextClosedEvent.class)
    public void onApplicationShutdown() {
        try {
            backupService.exportDatabase(BACKUP_FILE);
            log.info("Database backup created successfully at {}", BACKUP_FILE);
        } catch (SQLException e) {
            log.error("Failed to create database backup", e);
        }
    }
}

