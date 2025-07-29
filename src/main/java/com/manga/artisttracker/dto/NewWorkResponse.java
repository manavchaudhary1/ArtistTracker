// NewWorkResponse.java
package com.manga.artisttracker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NewWorkResponse {
    private String title;
    private String artist;
    private String galleryUrl;
    private String firstImageUrl;
    private String id;
    private String date;
}
