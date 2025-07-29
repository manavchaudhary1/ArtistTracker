package com.manga.artisttracker.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


@Slf4j
@Component
public class DatabaseInitializer {

    private static final String BACKUP_FILE = "classpath:db-backup.sql";

    private final DataSource dataSource;
    private final ResourceLoader resourceLoader;

    public DatabaseInitializer(DataSource dataSource, ResourceLoader resourceLoader) {
        this.dataSource = dataSource;
        this.resourceLoader = resourceLoader;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationStart() {
        try {
            Resource resource = resourceLoader.getResource("classpath:db-backup.zip");
            if (resource.exists()) {
                restoreDatabase(resource.getInputStream(), Paths.get("data"));
                log.info("Database restored from zip backup");
            } else {
                log.info("No backup file found, starting with fresh database");
            }
        } catch (Exception e) {
            log.error("Failed to restore database from backup", e);
        }
    }


    private void restoreDatabase(InputStream zipStream, Path outputDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(zipStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path filePath = outputDir.resolve(entry.getName());
                Files.createDirectories(filePath.getParent());
                try (OutputStream os = Files.newOutputStream(filePath)) {
                    zis.transferTo(os);
                }
                zis.closeEntry();
            }
        }
    }
}


