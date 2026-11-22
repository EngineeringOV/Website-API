package io.github.engineeringov.website.api.minecraft.web;

import io.github.engineeringov.website.api.minecraft.ConditionalOnMinecraftAuth;
import io.github.engineeringov.website.api.minecraft.MinecraftAuthConfig;
import io.github.engineeringov.website.api.minecraft.service.MinecraftAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Joining a server (Mojang's sessionserver protocol), plus name lookups the game server makes
 * for /whitelist, /op, /ban...
 */
@RestController
@ConditionalOnMinecraftAuth
@RequestMapping(value = MinecraftAuthConfig.PATH, produces = APPLICATION_JSON_VALUE)
public class MinecraftSessionEndpoint {

    private final MinecraftAuthService auth;

    public MinecraftSessionEndpoint(MinecraftAuthService auth) {
        this.auth = auth;
    }

    /** The game client, before connecting: "I am joining server X". */
    @PostMapping(value = "/session/minecraft/join", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> join(@RequestBody Requests.Join body, HttpServletRequest request) {
        auth.join(body.accessToken(), body.selectedProfile(), body.serverId(), Requests.ip(request));
        return ResponseEntity.noContent().build();
    }

    /** The game server, when a player connects: who is it? 204 refuses them. */
    @GetMapping("/session/minecraft/hasJoined")
    public ResponseEntity<Map<String, Object>> hasJoined(@RequestParam String username, @RequestParam String serverId,
                                                         @RequestParam(required = false) String ip) {
        return of(auth.hasJoined(username, serverId, ip));
    }

    @GetMapping("/session/minecraft/profile/{id}")
    public ResponseEntity<Map<String, Object>> profile(@PathVariable String id,
                                                       @RequestParam(defaultValue = "true") boolean unsigned) {
        return of(auth.profile(id, unsigned));
    }

    @GetMapping("/account/users/profiles/minecraft/{name}")
    public ResponseEntity<Map<String, Object>> byName(@PathVariable String name) {
        return of(auth.byName(name));
    }

    @PostMapping(value = "/account/profiles/minecraft", consumes = APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> byNames(@RequestBody List<String> names) {
        return auth.byNames(names);
    }

    private static ResponseEntity<Map<String, Object>> of(java.util.Optional<Map<String, Object>> m) {
        return m.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }
}
