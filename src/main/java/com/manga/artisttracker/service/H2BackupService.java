package com.manga.artisttracker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Service
@Slf4j
public class H2BackupService {

    private final DataSource dataSource;

    public H2BackupService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void exportDatabase(String backupPath) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            String sql = "BACKUP TO '" + backupPath + "'";
            statement.execute(sql);
        } catch (SQLException e) {
            log.error("Failed to export database to {}: {}", backupPath, e.getMessage());
            throw e;
        }
    }
}