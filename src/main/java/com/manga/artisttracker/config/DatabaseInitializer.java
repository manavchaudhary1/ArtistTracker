package com.manga.artisttracker.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
            Resource resource = resourceLoader.getResource(BACKUP_FILE);
            if (resource.exists()) {
                restoreDatabase(resource.getFile().getAbsolutePath());
                log.info("Database restored from backup");
                log.info("H2 DB URL: {}", dataSource.getConnection().getMetaData().getURL());
            } else {
                log.info("No backup file found, starting with fresh database");
            }
        } catch (SQLException | IOException e) {
            log.error("Failed to restore database from backup", e);
        }
    }

    private void restoreDatabase(String zipPath) throws IOException {
        File targetDir = new File("data");
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File outFile = new File(targetDir, entry.getName());
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    zis.transferTo(fos);
                }
            }
        }
    }
}


