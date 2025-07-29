package com.manga.artisttracker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileInfo {
    private int hasavif;
    private String hash;
    private int height;
    private int width;
    private String name;

    /**
     * Determine if this file is a GIF based on the name extension
     */
    public boolean isGif() {
        return name != null && name.toLowerCase().endsWith(".gif");
    }
}

