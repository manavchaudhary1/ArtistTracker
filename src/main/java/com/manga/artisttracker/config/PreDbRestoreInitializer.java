package com.manga.artisttracker.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
public class PreDbRestoreInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            log.info("Starting restoring DB");
            Path dataDir = Paths.get("data");
            Path dbFile = dataDir.resolve("artisttracker.mv.db");
            if (Files.notExists(dbFile)) {
                try (InputStream is = getClass().getClassLoader().getResourceAsStream("db-backup.zip")) {
                    if (is != null) {
                        ZipInputStream zis = new ZipInputStream(is);
                        ZipEntry entry;
                        while ((entry = zis.getNextEntry()) != null) {
                            Path outputPath = dataDir.resolve(entry.getName());
                            Files.createDirectories(outputPath.getParent());
                            getCopy(zis, outputPath);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore DB early", e);
        }
    }

    private static void getCopy(ZipInputStream zis, Path outputPath) throws IOException {
        Files.copy(zis, outputPath, StandardCopyOption.REPLACE_EXISTING);
    }
}

