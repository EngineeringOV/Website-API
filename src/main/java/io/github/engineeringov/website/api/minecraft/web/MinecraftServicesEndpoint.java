package io.github.engineeringov.website.api.minecraft.web;

import io.github.engineeringov.website.api.minecraft.ConditionalOnMinecraftAuth;
import io.github.engineeringov.website.api.minecraft.MinecraftAuthConfig;
import io.github.engineeringov.website.api.minecraft.service.MinecraftAuthService;
import io.github.engineeringov.website.api.minecraft.service.MojangApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * The "services" Minecraft asks about at start. Answers that keep multiplayer and chat on;
 * the rest (reports, friends, telemetry, chat signing) is politely declined.
 */
@RestController
@ConditionalOnMinecraftAuth
@RequestMapping(value = MinecraftAuthConfig.PATH + "/services", produces = APPLICATION_JSON_VALUE)
public class MinecraftServicesEndpoint {

    private static final Map<String, Object> ENABLED = Map.of("enabled", true);
    private static final Map<String, Object> DISABLED = Map.of("enabled", false);

    private final MojangApi mojang;
    private final MinecraftAuthService auth;

    public MinecraftServicesEndpoint(MojangApi mojang, MinecraftAuthService auth) {
        this.mojang = mojang;
        this.auth = auth;
    }

    /** Mojang's keys: real players' skins are signed by Mojang and verified with them. */
    @GetMapping("/publickeys")
    public ResponseEntity<String> publicKeys() {
        return mojang.publicKeys().map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
    }

    @GetMapping("/player/attributes")
    public Map<String, Object> attributes() {
        return Map.of(
                "privileges", Map.of(
                        "onlineChat", ENABLED,
                        "multiplayerServer", ENABLED,
                        "multiplayerRealms", DISABLED,
                        "telemetry", DISABLED,
                        "optionalTelemetry", DISABLED),
                "profanityFilterPreferences", Map.of("profanityFilterOn", false),
                "banStatus", Map.of("bannedScopes", Map.of()));
    }

    /** Minecraft 1.16's version of the above. */
    @GetMapping("/privileges")
    public Map<String, Object> privileges() {
        return Map.of("privileges", Map.of(
                "onlineChat", ENABLED,
                "multiplayerServer", ENABLED,
                "multiplayerRealms", DISABLED,
                "telemetry", DISABLED));
    }

    @GetMapping("/privacy/blocklist")
    public Map<String, Object> blocklist() {
        return Map.of("blockedProfiles", List.of());
    }

    /** Name lookups of Minecraft 1.20.3+ (services.host, then profiles.host from 1.21.9). */
    @GetMapping("/minecraft/profile/lookup/name/{name}")
    public ResponseEntity<Map<String, Object>> lookupName(@PathVariable String name) {
        return auth.byName(name).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/minecraft/profile/lookup/bulk/byname", consumes = APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> lookupNames(@RequestBody List<String> names) {
        return auth.byNames(names);
    }

    /** Telemetry and presence: accepted and dropped. */
    @PostMapping({"/events", "/presence"})
    public ResponseEntity<Void> ignored() {
        return ResponseEntity.noContent().build();
    }
}
