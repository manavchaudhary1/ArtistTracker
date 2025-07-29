package com.manga.artisttracker;

import com.manga.artisttracker.config.PreDbRestoreInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ArtistTrackerApplication {

    public static void main(String[] args) {
        SpringApplication app =  new SpringApplication(ArtistTrackerApplication.class);
        app.addInitializers(new PreDbRestoreInitializer());
        app.run(args);
    }

}
