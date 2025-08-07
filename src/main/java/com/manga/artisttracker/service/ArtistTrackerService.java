package com.manga.artisttracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manga.artisttracker.dto.FileInfo;
import com.manga.artisttracker.dto.GalleryInfo;
import com.manga.artisttracker.dto.NewWorkResponse;
import com.manga.artisttracker.entity.ArtistTracker;
import com.manga.artisttracker.repository.ArtistTrackerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.net.URI;

@Service
@Slf4j
public class ArtistTrackerService {
    private final ArtistTrackerRepository artistRepository;
    private final ArtistUpdateService artistUpdateService;
    private final EnhancedNozomiFileService nozomiService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String GALLERY_BASE_URL = "https://ltn.gold-usergeneratedcontent.net/galleries/";
    private static final String HITOMI_BASE_URL = "https://hitomi.la";
    private static final String LTN_URL = "https://ltn.gold-usergeneratedcontent.net";
    private static final String CDN_DOMAIN = "gold-usergeneratedcontent.net";
    private static final String serviceName = "hitomi";

    public ArtistTrackerService(ArtistTrackerRepository artistRepository,
                                EnhancedNozomiFileService nozomiService,
                                ArtistUpdateService artistUpdateService) {
        this.artistRepository = artistRepository;
        this.nozomiService = nozomiService;
        this.artistUpdateService = artistUpdateService;
    }

    private Long scriptLastRetrieval = null;
    private int subdomainOffsetDefault = 0;
    private Map<Integer, Integer> subdomainOffsetMap = new HashMap<>();
    private String commonImageId = "";

    public List<NewWorkResponse> checkForNewWorks(String artistName) throws IOException {
        List<NewWorkResponse> newWorks = new ArrayList<>();

        List<Integer> currentPostIds = getCurrentPostIds(artistName);
        if (currentPostIds.isEmpty()) {
            return newWorks;
        }

        String latestId = String.valueOf(currentPostIds.getFirst());
        Optional<ArtistTracker> existingArtist = artistRepository.findByServiceNameAndArtistName(serviceName, artistName);

        if (existingArtist.isPresent()) {
            processExistingArtist(existingArtist.get(), latestId, currentPostIds, newWorks);
        }

        return newWorks;
    }

    private List<Integer> getCurrentPostIds(String artistName) throws IOException {
        String nozomiUrl = "https://ltn.gold-usergeneratedcontent.net/artist/" + artistName + "-all.nozomi";
        return nozomiService.parsePostIds(nozomiUrl);
    }

    private void processExistingArtist(ArtistTracker artist, String latestId,
                                       List<Integer> currentPostIds, List<NewWorkResponse> newWorks) {
        String previousLatestId = artist.getLatestId();
        if (latestId.equals(previousLatestId)) {
            return;
        }
        LocalDateTime lastUpdated = artist.getLastUpdated();
        GalleryInfo latestGalleryInfo = fetchLatestGalleryInfo(latestId);
        assert latestGalleryInfo != null;
        LocalDateTime galleryDate = parseGalleryDate(latestGalleryInfo.getDate());
        if (!galleryDate.isAfter(lastUpdated)){
            return;
        }
        List<Integer> newIds = getNewIds(currentPostIds, Integer.parseInt(previousLatestId));

        processNewWorks(newIds, newWorks);
        artistUpdateService.updateArtistRecord(artist, latestId, latestGalleryInfo, this::parseGalleryDate);
    }

    private GalleryInfo fetchLatestGalleryInfo(String latestId) {
        try {
            return getGalleryInfo(latestId);
        } catch (Exception e) {
            log.error("Error getting latest gallery info for {}: {}", latestId, e.getMessage());
            return null;
        }
    }

    private void processNewWorks(List<Integer> newIds, List<NewWorkResponse> newWorks) {
        for (Integer newId : newIds) {
            try {
                processNewWork(newId, newWorks);
            } catch (Exception e) {
                log.error("Error processing gallery {}: {}", newId, e.getMessage());
            }
        }
    }

    private void processNewWork(Integer newId, List<NewWorkResponse> newWorks) throws IOException {
        GalleryInfo galleryInfo = getGalleryInfo(String.valueOf(newId));
        if (galleryInfo != null) {
            String firstImageUrl = generateFirstImageUrl(String.valueOf(newId));
            newWorks.add(new NewWorkResponse(
                    galleryInfo.getTitle(),
                    galleryInfo.getArtistName(),
                    HITOMI_BASE_URL + galleryInfo.getGalleryurl(),
                    firstImageUrl,
                    String.valueOf(newId),
                    galleryInfo.getDate()
            ));
        }
    }

    private String generateFirstImageUrl(String galleryId) {
        try {
            FileInfo firstFile = getFirstFileInfo(galleryId);
            if (firstFile == null) {
                return null;
            }
            return generateFullImageUrl(firstFile.getHash(), firstFile.isGif());
        } catch (Exception e) {
            log.error("Error generating first image URL for {}: {}", galleryId, e.getMessage());
            return null;
        }
    }

    private String generateFullImageUrl(String hash, boolean isGif) throws IOException {
        refreshScript();

        int imageId = imageIdFromHash(hash);
        int subDomainOffset = getSubdomainOffset(imageId);

        String type = isGif ? "webp" : "avif";
        String subDomain = isGif ? "w" + (subDomainOffset + 1) : "a" + (subDomainOffset + 1);
        return "https://" + subDomain + "." + CDN_DOMAIN + "/" + commonImageId + "/" + imageId + "/" + hash + "." + type;
    }

    private FileInfo getFirstFileInfo(String galleryId) throws IOException {
        String idJsUrl = LTN_URL + "/galleries/" + galleryId + ".js";

        try {
            URL url = URI.create(idJsUrl).toURL();
            Scanner scanner = new Scanner(url.openStream());
            StringBuilder content = new StringBuilder();
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine());
            }
            scanner.close();

            String jsContent = content.toString();
            int startIndex = jsContent.indexOf("var galleryinfo = ");
            if (startIndex == -1) {
                throw new IOException("Could not find 'var galleryinfo = ' in " + idJsUrl);
            }

            startIndex += "var galleryinfo = ".length();
            int endIndex = findJsonEnd(jsContent, startIndex);

            if (endIndex > startIndex) {
                String jsonContent = jsContent.substring(startIndex, endIndex);
                JsonNode rootNode = objectMapper.readTree(jsonContent);
                JsonNode filesNode = rootNode.get("files");

                if (filesNode != null && filesNode.isArray() && filesNode.size() > 0) {
                    JsonNode firstFileNode = filesNode.get(0);
                    return objectMapper.treeToValue(firstFileNode, FileInfo.class);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching first file info for {}: {}", galleryId, e.getMessage());
            throw new IOException("Failed to fetch first file info", e);
        }

        return null;
    }

    private int findJsonEnd(String jsContent, int startIndex) {
        int braceCount = 0;
        for (int i = startIndex; i < jsContent.length(); i++) {
            char c = jsContent.charAt(i);
            if (c == '{') braceCount++;
            else if (c == '}') braceCount--;

            if (braceCount == 0 && c == '}') {
                return i + 1;
            }
        }
        return -1;
    }

    private synchronized void refreshScript() throws IOException {
        if (!shouldRefreshScript()) {
            return;
        }

        String ggScript = fetchGgScript();
        parseGgScriptValues(ggScript);
        scriptLastRetrieval = System.currentTimeMillis();
    }

    private boolean shouldRefreshScript() {
        long currentTime = System.currentTimeMillis();
        return scriptLastRetrieval == null || (scriptLastRetrieval + 60000) < currentTime;
    }

    private String fetchGgScript() throws IOException {
        String ggScriptUrl = LTN_URL + "/gg.js?_=" + System.currentTimeMillis();
        try {
            URL url = URI.create(ggScriptUrl).toURL();
            return readUrlContent(url);
        } catch (Exception e) {
            log.error("Error refreshing gg.js script: {}", e.getMessage());
            throw new IOException("Failed to refresh gg.js script", e);
        }
    }

    private void parseGgScriptValues(String ggScript) {
        parseSubdomainOffsetDefault(ggScript);
        int o = parseOffsetValue(ggScript);
        parseCaseMappings(ggScript, o);
        parseCommonImageId(ggScript);
    }

    private void parseSubdomainOffsetDefault(String ggScript) {
        Pattern pattern = Pattern.compile("var o = (\\d+)");
        Matcher matcher = pattern.matcher(ggScript);
        if (matcher.find()) {
            subdomainOffsetDefault = Integer.parseInt(matcher.group(1));
        }
    }

    private int parseOffsetValue(String ggScript) {
        Pattern pattern = Pattern.compile("o = (\\d+); break;");
        Matcher matcher = pattern.matcher(ggScript);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : subdomainOffsetDefault;
    }

    private void parseCaseMappings(String ggScript, int o) {
        subdomainOffsetMap.clear();
        Pattern pattern = Pattern.compile("case (\\d+):");
        Matcher matcher = pattern.matcher(ggScript);
        while (matcher.find()) {
            int caseValue = Integer.parseInt(matcher.group(1));
            subdomainOffsetMap.put(caseValue, o);
        }
    }

    private void parseCommonImageId(String ggScript) {
        Pattern pattern = Pattern.compile("b:\\s*'([^']+)'");
        Matcher matcher = pattern.matcher(ggScript);
        if (matcher.find()) {
            commonImageId = matcher.group(1).replaceAll("/$", "");
        } else {
            pattern = Pattern.compile("b:\\s*\"([^\"]+)\"");
            matcher = pattern.matcher(ggScript);
            if (matcher.find()) {
                commonImageId = matcher.group(1).replaceAll("/$", "");
            }
        }
    }

    private int getSubdomainOffset(int imageId) {
        return subdomainOffsetMap.getOrDefault(imageId, subdomainOffsetDefault);
    }

    private int imageIdFromHash(String hash) {
        if (hash.length() < 3) {
            return 0;
        }

        String last3 = hash.substring(hash.length() - 3);
        String last2 = last3.substring(0, 2);
        String last1 = last3.substring(2, 3);

        return Integer.parseInt(last1 + last2, 16);
    }

    private List<Integer> getNewIds(List<Integer> currentIds, int previousLatestId) {
        List<Integer> newIds = new ArrayList<>();
        for (Integer id : currentIds) {
            if (!id.equals(previousLatestId)) {
                newIds.add(id);
            } else {
                break;
            }
        }
        return newIds;
    }

    @Transactional
    public ArtistTracker addArtist(String artistName) throws IOException {
        artistName = URLEncoder.encode(artistName, StandardCharsets.UTF_8).replace("+", "%20");;

        Optional<ArtistTracker> existing = artistRepository.findByServiceNameAndArtistName(serviceName, artistName);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Artist already tracked: " + artistName);
        }

        String nozomiUrl = "https://ltn.gold-usergeneratedcontent.net/artist/" + artistName + "-all.nozomi";
        List<Integer> postIds = nozomiService.parsePostIds(nozomiUrl);

        if (postIds.isEmpty()) {
            throw new IllegalArgumentException("No works found for artist: " + artistName);
        }

        String latestId = String.valueOf(postIds.getFirst());

        LocalDateTime galleryDate;
        try {
            GalleryInfo galleryInfo = getGalleryInfo(latestId);
            if (galleryInfo != null && galleryInfo.getDate() != null) {
                galleryDate = parseGalleryDate(galleryInfo.getDate());
            } else {
                throw new RuntimeException("Gallery info or date is null");
            }
        } catch (Exception e) {
            log.error("Error getting gallery date for {}: {}", latestId, e.getMessage());
            galleryDate = LocalDateTime.now();
        }

        ArtistTracker newArtist = new ArtistTracker(serviceName, artistName, latestId, galleryDate);
        return artistRepository.save(newArtist);
    }

    private LocalDateTime parseGalleryDate(String dateString) {
        try {
            // Normalize space to 'T'
            if (dateString.contains(" ") && !dateString.contains("T")) {
                dateString = dateString.replace(" ", "T");
            }

            if (dateString.matches(".*[+-]\\d{2}$")) {
                dateString += ":00"; // turns -05 into -05:00
            } else if (dateString.matches(".*[+-]\\d{2}\\d{2}$")) {
                // e.g., -0530 → -05:30
                dateString = dateString.replaceAll("([+-]\\d{2})(\\d{2})$", "$1:$2");
            }

            // Parse using OffsetDateTime
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateString);
            return offsetDateTime.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
        } catch (Exception e) {
            log.error("Failed to parse date '{}': {}", dateString, e.getMessage());
            throw new RuntimeException("Invalid date format: " + dateString, e);
        }
    }


    private GalleryInfo getGalleryInfo(String galleryId) throws IOException {
        String galleryUrl = GALLERY_BASE_URL + galleryId + ".js";
        try {
            URL url = URI.create(galleryUrl).toURL();
            String jsContent = readUrlContent(url);

            int startIndex = findGalleryInfoStart(jsContent);
            int endIndex = findGalleryInfoEnd(jsContent, startIndex);

            String jsonContent = jsContent.substring(startIndex, endIndex);
            return objectMapper.readValue(jsonContent, GalleryInfo.class);

        } catch (Exception e) {
            log.error("Error fetching gallery info for ID {}: {}", galleryId, e.getMessage());
            throw new IOException("Failed to fetch gallery info", e);
        }
    }

    private String readUrlContent(URL url) throws IOException {
        try (Scanner scanner = new Scanner(url.openStream())) {
            StringBuilder content = new StringBuilder();
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine());
            }
            return content.toString();
        }
    }

    private int findGalleryInfoStart(String jsContent) throws IOException {
        int startIndex = jsContent.indexOf("var galleryinfo = ");
        if (startIndex == -1) {
            throw new IOException("Could not find 'var galleryinfo = ' in response");
        }
        return startIndex + "var galleryinfo = ".length();
    }

    private int findGalleryInfoEnd(String jsContent, int startIndex) throws IOException {
        int endIndex = jsContent.indexOf(";", startIndex);
        if (endIndex == -1) {
            endIndex = findJsonEndByBraces(jsContent, startIndex);
        }

        if (endIndex <= startIndex) {
            throw new IOException("Could not determine end of JSON content");
        }
        return endIndex;
    }

    private int findJsonEndByBraces(String jsContent, int startIndex) {
        int braceCount = 0;
        for (int i = startIndex; i < jsContent.length(); i++) {
            char c = jsContent.charAt(i);
            if (c == '{') braceCount++;
            else if (c == '}') braceCount--;

            if (braceCount == 0 && c == '}') {
                return i + 1;
            }
        }
        return -1;
    }


    public List<ArtistTracker> getAllArtists() {
        return artistRepository.findAll();
    }

    @Transactional
    public void deleteArtist(String artistName) {
        artistRepository.deleteByServiceNameAndArtistName(serviceName, artistName);
    }

    public List<NewWorkResponse> refreshAllArtists() {
        List<NewWorkResponse> allNewWorks = new ArrayList<>();
        List<ArtistTracker> allArtists = artistRepository.findAll();

        for (ArtistTracker artist : allArtists) {
            try {
                List<NewWorkResponse> newWorks = checkForNewWorks(artist.getArtistName());
                allNewWorks.addAll(newWorks);
            } catch (Exception e) {
                log.error("Error checking artist {}: {}", artist.getArtistName(), e.getMessage());
            }
        }

        return allNewWorks;
    }
}