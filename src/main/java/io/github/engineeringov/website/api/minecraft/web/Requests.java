package io.github.engineeringov.website.api.minecraft.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.servlet.http.HttpServletRequest;

/** Request bodies of the Minecraft endpoints (Mojang's field names). */
final class Requests {
    private Requests() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Authenticate(String username, String password, String clientToken, Boolean requestUser) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Tokens(String accessToken, String clientToken) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ChooseName(String accessToken, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Join(String accessToken, String selectedProfile, String serverId) {
    }

    /** nginx passes the player's address in X-Real-IP. */
    static String ip(HttpServletRequest request) {
        String real = request.getHeader("X-Real-IP");
        return real != null && !real.isBlank() ? real.trim() : request.getRemoteAddr();
    }
}
