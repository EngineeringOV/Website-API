package io.github.engineeringov.website.api.minecraft.web;

import io.github.engineeringov.website.api.minecraft.ConditionalOnMinecraftAuth;
import io.github.engineeringov.website.api.minecraft.MinecraftAuthConfig;
import io.github.engineeringov.website.api.minecraft.MinecraftAuthProperties;
import io.github.engineeringov.website.api.minecraft.service.MinecraftAuthService;
import io.github.engineeringov.website.api.minecraft.service.MinecraftAuthService.ProfileRef;
import io.github.engineeringov.website.api.minecraft.service.YggdrasilException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/** What the launcher talks to: server info and logins (Mojang's authserver protocol). */
@RestController
@ConditionalOnMinecraftAuth
@RequestMapping(value = MinecraftAuthConfig.PATH, produces = APPLICATION_JSON_VALUE)
public class MinecraftAuthEndpoint {

    private final MinecraftAuthService auth;
    private final MinecraftAuthProperties props;

    public MinecraftAuthEndpoint(MinecraftAuthService auth, MinecraftAuthProperties props) {
        this.auth = auth;
        this.props = props;
    }

    /** The launcher reads this when an account is added: name, and the address for the server list. */
    @GetMapping({"", "/"})
    public Map<String, Object> info() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", props.getName());
        m.put("gameServer", gameServer());
        m.put("discovery", props.getPublicUrl() + "/discovery");
        m.put("realAccounts", props.isMojangFallback());
        return m;
    }

    /** Where Minecraft 26+ finds every service (-Dminecraft.api.discovery.host). */
    @GetMapping("/discovery")
    public Map<String, Object> discovery() {
        return Discovery.document(props.getPublicUrl());
    }

    @PostMapping(value = "/authserver/authenticate", consumes = APPLICATION_JSON_VALUE)
    public Map<String, Object> authenticate(@RequestBody Requests.Authenticate body, HttpServletRequest request) {
        MinecraftAuthService.Session s = auth.authenticate(body.username(), body.password(), body.clientToken(), Requests.ip(request));
        Map<String, Object> m = session(s);
        if (Boolean.TRUE.equals(body.requestUser())) m.put("user", Map.of("id", String.valueOf(s.accountId()), "properties", List.of()));
        return m;
    }

    @PostMapping(value = "/authserver/refresh", consumes = APPLICATION_JSON_VALUE)
    public Map<String, Object> refresh(@RequestBody Requests.Tokens body) {
        return session(auth.refresh(body.accessToken(), body.clientToken()));
    }

    @PostMapping(value = "/authserver/validate", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> validate(@RequestBody Requests.Tokens body) {
        if (!auth.validate(body.accessToken(), body.clientToken())) throw YggdrasilException.forbidden("Invalid token.");
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/authserver/invalidate", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> invalidate(@RequestBody Requests.Tokens body) {
        auth.invalidate(body.accessToken());
        return ResponseEntity.noContent().build();
    }

    /** For accounts whose login name can't be a Minecraft name: pick one, once. */
    @PostMapping(value = "/profile/name", consumes = APPLICATION_JSON_VALUE)
    public Map<String, Object> chooseName(@RequestBody Requests.ChooseName body) {
        return profile(auth.chooseName(body.accessToken(), body.name()));
    }

    private Map<String, Object> session(MinecraftAuthService.Session s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("accessToken", s.accessToken());
        m.put("clientToken", s.clientToken());
        m.put("availableProfiles", s.selectedProfile() == null ? List.of() : List.of(profile(s.selectedProfile())));
        if (s.selectedProfile() != null) m.put("selectedProfile", profile(s.selectedProfile()));
        return m;
    }

    private static Map<String, Object> profile(ProfileRef p) {
        return Map.of("id", p.id(), "name", p.name());
    }

    /** Configured address, else this server's host on the default port. */
    private String gameServer() {
        if (props.getGameServer() != null && !props.getGameServer().isBlank()) return props.getGameServer().trim();
        String host = URI.create(props.getPublicUrl()).getHost();
        return host == null ? null : host;
    }
}
