package com.manga.artisttracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ArtistTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArtistTrackerApplication.class, args);
    }

}
