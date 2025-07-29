package com.manga.artisttracker.config;


import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PreDbRestoreInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
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
                            Files.copy(zis, outputPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore DB early", e);
        }
    }
}

