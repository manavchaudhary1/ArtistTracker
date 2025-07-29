// ArtistTrackerController.java
package com.manga.artisttracker.controller;

import com.manga.artisttracker.dto.NewWorkResponse;
import com.manga.artisttracker.entity.ArtistTracker;
import com.manga.artisttracker.service.ArtistTrackerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/artists")
public class ArtistTrackerController {

    private final ArtistTrackerService artistService;

    public ArtistTrackerController(ArtistTrackerService artistService) {
        this.artistService = artistService;
    }

    /**
     * Check for new works by a specific artist
     */
    @GetMapping("/{artistName}/check")
    public ResponseEntity<List<NewWorkResponse>> checkArtist(@PathVariable String artistName) {
        try {
            List<NewWorkResponse> newWorks = artistService.checkForNewWorks(artistName);
            return ResponseEntity.ok(newWorks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all tracked artists
     */
    @GetMapping("/all")
    public ResponseEntity<List<ArtistTracker>> getAllArtists() {
        List<ArtistTracker> artists = artistService.getAllArtists();
        return ResponseEntity.ok(artists);
    }

    /**
     * Add new artist to tracking
     */
    @PostMapping("/{artistName}")
    public ResponseEntity<ArtistTracker> addArtist(@PathVariable String artistName) {
        try {
            ArtistTracker artist = artistService.addArtist(artistName);
            return ResponseEntity.ok(artist);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete artist from tracking
     */
    @DeleteMapping("/{artistName}")
    public ResponseEntity<Void> deleteArtist(@PathVariable String artistName) {
        try {
            artistService.deleteArtist(artistName);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Refresh all artists - check for new works for all tracked artists
     */
    @PostMapping("/refresh-all")
    public ResponseEntity<List<NewWorkResponse>> refreshAllArtists() {
        try {
            List<NewWorkResponse> allNewWorks = artistService.refreshAllArtists();
            return ResponseEntity.ok(allNewWorks);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
