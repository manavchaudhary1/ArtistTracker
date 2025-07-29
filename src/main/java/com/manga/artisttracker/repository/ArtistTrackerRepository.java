package com.manga.artisttracker.repository;

import com.manga.artisttracker.entity.ArtistTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ArtistTrackerRepository extends JpaRepository<ArtistTracker, Long> {
    Optional<ArtistTracker> findByServiceNameAndArtistName(String serviceName, String artistName);
    List<ArtistTracker> findByServiceName(String serviceName);
    void deleteByServiceNameAndArtistName(String serviceName, String artistName);
}
