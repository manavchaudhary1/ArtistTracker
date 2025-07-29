package com.manga.artisttracker.service;


import com.manga.artisttracker.dto.GalleryInfo;
import com.manga.artisttracker.entity.ArtistTracker;
import com.manga.artisttracker.repository.ArtistTrackerRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.function.Function;

@Service
@Transactional
@Slf4j
public class ArtistUpdateService {

    private final ArtistTrackerRepository artistRepository;

    public ArtistUpdateService(ArtistTrackerRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    public void updateArtistRecord(ArtistTracker artist, String latestId, GalleryInfo latestGalleryInfo,
                                   Function<String, LocalDateTime> dateParser) {
        try {
            LocalDateTime galleryDate = determineGalleryDate(latestGalleryInfo, dateParser);
            artist.setLatestId(latestId);
            artist.setLastUpdated(galleryDate);
            artistRepository.save(artist);
        } catch (Exception e) {
            log.error("Error updating artist record for ID {}: {}", latestId, e.getMessage());
            artist.setLatestId(latestId);
            artist.setLastUpdated(LocalDateTime.now());
            artistRepository.save(artist);
        }
    }

    private LocalDateTime determineGalleryDate(GalleryInfo latestGalleryInfo,
                                               Function<String, LocalDateTime> dateParser) {
        if (latestGalleryInfo != null && latestGalleryInfo.getDate() != null) {
            return dateParser.apply(latestGalleryInfo.getDate());
        }
        return LocalDateTime.now();
    }
}

