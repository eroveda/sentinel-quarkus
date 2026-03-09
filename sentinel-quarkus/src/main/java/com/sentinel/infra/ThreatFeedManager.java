package com.sentinel.infra;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.sentinel.service.FastSecurityFilter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ThreatFeedManager {
    private static final Logger LOG = Logger.getLogger(ThreatFeedManager.class);
    private static final String CACHE_FILE = "threats.cache";

    // Standard Java HTTP Client
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Inject
    FastSecurityFilter fastFilter;

    @ConfigProperty(name = "sentinel.feeds.config.path", defaultValue = "config/feeds.txt")
    String feedsConfigPath;

    @ConfigProperty(name = "sentinel.test-mode")
    boolean testMode;

    // Safety Whitelist - Add or remove trusted domains as needed
    private static final List<String> ALLOWED_DOMAINS = List.of(
        "raw.githubusercontent.com", 
        "api.moltbook.com",
        "gist.githubusercontent.com"
    );

    @ConfigProperty(name = "sentinel.wallet.address")
    String walletAddress;

    /**
     * Punto de entrada principal para la sincronización.
     * Si testMode es true, ignora internet y usa el archivo local.
     */
    public void syncAll() {
        if (testMode) {
            LOG.infof("MODE: LOCAL TEST. Target Wallet: %s", walletAddress);
            loadLocalTestPatterns();
        } else {
            LOG.info("MODE: PRODUCTION. Starting global threat feed synchronization...");
            Set<String> patterns = downloadNewPatterns();

            if (!patterns.isEmpty()) {
                saveToLocalCache(patterns);
                fastFilter.rebuildTrie(patterns);
                LOG.info("Synchronization successful. Node is up to date.");
            } else {
                LOG.warn("Could not fetch online feeds. Falling back to local cache...");
                loadFromCache();
            }
        }
    }

    /**
     * Carga patrones desde src/main/resources/config/local_threats.txt
     */
    private void loadLocalTestPatterns() {
        try (var is = Thread.currentThread().getContextClassLoader().getResourceAsStream("config/local_threats.txt");
             var reader = new BufferedReader(new InputStreamReader(is))) {
            
            if (is == null) {
                LOG.error("Test file 'config/local_threats.txt' not found in resources!");
                return;
            }

            Set<String> patterns = new HashSet<>();
            parsePatterns(reader.lines().collect(Collectors.joining("\n")), patterns);

            fastFilter.rebuildTrie(patterns);
            LOG.infof("Successfully loaded %d patterns into CPU-Trie (Local Mode).", patterns.size());
            
        } catch (Exception e) {
            LOG.error("Error loading local test patterns: " + e.getMessage());
        }
    }

    private Set<String> downloadNewPatterns() {
        Set<String> aggregatedPatterns = new HashSet<>();
        List<String> urls = loadUrlsFromConfig();

        if (urls.isEmpty()) {
            return aggregatedPatterns;
        }

        for (String url : urls) {
            if (!isSafeUrl(url)) {
                LOG.warnf("Skipping unsafe URL: %s", url);
                continue;
            }

            try {
                LOG.infof("Downloading patterns from: %s", url);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    if (response.body().length() < 2 * 1024 * 1024) { // 2MB Limit
                        parsePatterns(response.body(), aggregatedPatterns);
                    } else {
                        LOG.errorf("Feed content exceeds safety limit (2MB): %s", url);
                    }
                }
            } catch (Exception e) {
                LOG.errorf("Network error on %s: %s", url, e.getMessage());
            }
        }
        return aggregatedPatterns;
    }

    private boolean isSafeUrl(String urlString) {
        try {
            URI uri = new URI(urlString);
            return "https".equals(uri.getScheme()) && ALLOWED_DOMAINS.contains(uri.getHost());
        } catch (Exception e) {
            return false;
        }
    }

    private void parsePatterns(String content, Set<String> targetSet) {
        content.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .filter(line -> line.length() < 256) // Avoid Trie bloat from giant strings
                .map(String::toLowerCase)
                .forEach(targetSet::add);
    }

    private void saveToLocalCache(Set<String> patterns) {
        try {
            Files.write(Path.of(CACHE_FILE), patterns, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            LOG.info("Local threat cache updated on disk.");
        } catch (Exception e) {
            LOG.error("Disk I/O error while saving cache: " + e.getMessage());
        }
    }

    private void loadFromCache() {
        try {
            Path path = Path.of(CACHE_FILE);
            if (Files.exists(path)) {
                List<String> patterns = Files.readAllLines(path);
                fastFilter.rebuildTrie(patterns);
                LOG.infof("Successfully loaded %d patterns from local cache.", patterns.size());
            } else {
                LOG.error("Local cache file not found. Node operating without pattern-matching.");
            }
        } catch (Exception e) {
            LOG.error("Disk I/O error while loading cache: " + e.getMessage());
        }
    }

    private List<String> loadUrlsFromConfig() {
        try (var inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(feedsConfigPath)) {
            if (inputStream == null) {
                LOG.errorf("Configuration file [%s] not found in resources.", feedsConfigPath);
                return List.of();
            }
            try (var reader = new BufferedReader(new InputStreamReader(inputStream))) {
                return reader.lines()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            LOG.error("Failed to load feed configuration: " + e.getMessage());
            return List.of();
        }
    }
}