package com.manga.artisttracker.config;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Service
public class H2BackupService {

    private final DataSource dataSource;

    public H2BackupService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void exportDatabase(String backupFilePath) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.execute("BACKUP TO '" + backupFilePath.replace("\\", "/") + "'");
        }
    }
}
