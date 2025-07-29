package com.manga.artisttracker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GalleryInfo {
    @JsonProperty("date")
    private String date;
    private String artistName;
    private String id;
    @JsonProperty("title")
    private String title;
    @JsonProperty("galleryurl")
    private String galleryurl;

    @JsonSetter("artists")
    public void setArtists(List<Map<String, String>> artists) {
        if (artists != null && !artists.isEmpty()) {
            this.artistName = artists.stream()
                    .map(a -> a.get("artist"))
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));
        }
    }
}
