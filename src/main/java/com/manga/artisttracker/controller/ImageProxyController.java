package com.manga.artisttracker.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;


@RestController
@RequestMapping("/api/image-proxy")
@Slf4j
public class ImageProxyController {

    private final RestTemplate restTemplate;

    public ImageProxyController() {
        this.restTemplate = new RestTemplate();
    }

    @GetMapping
    public ResponseEntity<byte[]> proxyImage(@RequestParam String url) {
        try {
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Referer", "https://hitomi.la/");
            headers.set("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Exchange
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    byte[].class
            );

            // Get content type from the response
            MediaType contentType = response.getHeaders().getContentType();

            // Fallback if null
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM;
            }

            // Prepare response headers
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(contentType);
            responseHeaders.setCacheControl(CacheControl.maxAge(Duration.ofHours(24)));

            return new ResponseEntity<>(response.getBody(), responseHeaders, HttpStatus.OK);

        } catch (Exception e) {
            // Log the error to find out what's going on
            log.error("Image proxying failed for URL: " + url, e);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

