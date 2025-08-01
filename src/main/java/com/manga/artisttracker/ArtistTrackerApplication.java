package com.manga.artisttracker;

import com.manga.artisttracker.config.PreDbRestoreInitializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class ArtistTrackerApplication {

    public static void main(String[] args) {
        SpringApplication app =  new SpringApplication(ArtistTrackerApplication.class);
        app.addInitializers(new PreDbRestoreInitializer());
        app.run(args);
        log.info("ArtistTrackerApplication started");
        log.info("Visit http://localhost:8080/dashboard ");
    }

}
