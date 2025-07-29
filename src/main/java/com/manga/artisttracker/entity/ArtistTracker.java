package com.manga.artisttracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "artist_tracker")
public class ArtistTracker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String serviceName;

    @Column(nullable = false)
    private String artistName;

    @Column(nullable = false)
    private String latestId;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    public ArtistTracker(String serviceName, String artistName, String latestId, LocalDateTime galleryDate) {
        this.serviceName = serviceName;
        this.artistName = artistName;
        this.latestId = latestId;
        this.lastUpdated = galleryDate;
    }
}

