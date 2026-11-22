package io.github.engineeringov.website.api.minecraft.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.engineeringov.website.api.minecraft.ConditionalOnMinecraftAuth;
import io.github.engineeringov.website.api.minecraft.MinecraftAuthProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Mojang's public endpoints, for real accounts. Profiles come back as plain JSON maps. */
@Log4j2
@Component
@ConditionalOnMinecraftAuth
public class MojangApi {
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final Duration KEYS_TTL = Duration.ofHours(1);
    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {
    };
    private static final TypeReference<List<Map<String, Object>>> LIST = new TypeReference<>() {
    };

    private final MinecraftAuthProperties props;
    private final ObjectMapper json;
    private final HttpClient http;
    private volatile String publicKeys;
    private volatile Instant publicKeysAt = Instant.EPOCH;

    @Autowired
    public MojangApi(MinecraftAuthProperties props, ObjectMapper json) {
        this(props, json, HttpClient.newBuilder().connectTimeout(TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL).build());
    }

    MojangApi(MinecraftAuthProperties props, ObjectMapper json, HttpClient http) {
        this.props = props;
        this.json = json;
        this.http = http;
    }

    /** The real player's profile if Mojang confirms the join, else empty. */
    public Optional<Map<String, Object>> hasJoined(String username, String serverId, String ip) {
        String url = props.getMojangSessionUrl() + "/session/minecraft/hasJoined?username=" + enc(username)
                + "&serverId=" + enc(serverId) + (ip == null ? "" : "&ip=" + enc(ip));
        return get(url).map(this::map);
    }

    public Optional<Map<String, Object>> profile(String id, boolean unsigned) {
        return get(props.getMojangSessionUrl() + "/session/minecraft/profile/" + enc(id)
                + "?unsigned=" + unsigned).map(this::map);
    }

    /** {id, name} of a real account by name. */
    public Optional<Map<String, Object>> byName(String name) {
        return get(props.getMojangApiUrl() + "/users/profiles/minecraft/" + enc(name)).map(this::map);
    }

    public List<Map<String, Object>> byNames(List<String> names) {
        if (names.isEmpty()) return List.of();
        try {
            HttpResponse<String> r = http.send(HttpRequest.newBuilder(URI.create(props.getMojangApiUrl() + "/profiles/minecraft"))
                    .timeout(TIMEOUT).header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(names))).build(),
                    HttpResponse.BodyHandlers.ofString());
            return r.statusCode() == 200 ? json.readValue(r.body(), LIST) : List.of();
        } catch (IOException e) {
            log.warn("Mojang bulk lookup failed: {}", e.toString());
            return List.of();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return List.of();
        }
    }

    /** Mojang's signing keys (raw JSON), cached for an hour; the stale copy is kept when Mojang is down. */
    public Optional<String> publicKeys() {
        if (publicKeys == null || Instant.now().isAfter(publicKeysAt.plus(KEYS_TTL))) {
            get(props.getMojangServicesUrl() + "/publickeys").ifPresent(body -> {
                publicKeys = body;
                publicKeysAt = Instant.now();
            });
        }
        return Optional.ofNullable(publicKeys);
    }

    /** Body of a 200, empty on anything else (204 = unknown/not joined). */
    private Optional<String> get(String url) {
        try {
            HttpResponse<String> r = http.send(HttpRequest.newBuilder(URI.create(url)).timeout(TIMEOUT).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            if (r.statusCode() == 200 && !r.body().isBlank()) return Optional.of(r.body());
            if (r.statusCode() >= 500 || r.statusCode() == 429) log.warn("Mojang answered {} for {}", r.statusCode(), stripQuery(url));
            return Optional.empty();
        } catch (IOException e) {
            log.warn("Mojang unreachable ({}): {}", stripQuery(url), e.toString());
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    private Map<String, Object> map(String body) {
        try {
            return new LinkedHashMap<>(json.readValue(body, MAP));
        } catch (IOException e) {
            throw new IllegalStateException("Unreadable answer from Mojang", e);
        }
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String stripQuery(String url) {
        int q = url.indexOf('?');
        return q < 0 ? url : url.substring(0, q);
    }
}
