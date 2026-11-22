package io.github.engineeringov.website.api.minecraft.web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The service directory Minecraft 26+ reads at start (same layout as
 * discovery.minecraftservices.com), with every endpoint pointing here. Textures stay on
 * Mojang's host, the only one older versions accept.
 */
final class Discovery {
    private Discovery() {
    }

    static Map<String, Object> document(String base) {
        String services = base + "/services";
        String session = base + "/session/minecraft";
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("authentication", service(
                "getPublicKeys", uri(services + "/publickeys"),
                "loginXbox", uri(services + "/authentication/login_with_xbox")));
        d.put("session", service(
                "getProfileById", uri(session + "/profile/{profileId}"),
                "verify", uri(session + "/hasJoined"),
                "join", uri(session + "/join")));
        d.put("player", service(
                "updatePresence", uri(services + "/presence"),
                "sendReport", uri(services + "/player/report"),
                "getAttributes", uri(services + "/player/attributes"),
                "getFriends", uri(services + "/friends"),
                "getCertificates", uri(services + "/player/certificates"),
                "updateAttributes", uri(services + "/player/attributes"),
                "updateFriends", uri(services + "/friends"),
                "getBlocklist", uri(services + "/privacy/blocklist")));
        d.put("profiles", service(
                "getManyByName", uri(base + "/account/profiles/minecraft"),
                "getByName", uri(base + "/account/users/profiles/minecraft/{name}"),
                "getTexture", Map.of("validUris", List.of(
                        "https://textures.minecraft.net/texture/{textureId}",
                        "http://textures.minecraft.net/texture/{textureId}"))));
        d.put("telemetry", service("sendEvents", uri(services + "/events")));
        d.put("signaling", service(
                "getConfiguration", uri(services + "/signaling/configuration"),
                "connect", uri("{signalingUri}/ws/v1.0/messaging/connect/java")));

        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("environment", "custom");
        doc.put("product", "minecraft");
        doc.put("discovery", d);
        return doc;
    }

    private static Map<String, Object> uri(String u) {
        return Map.of("uri", u);
    }

    private static Map<String, Object> service(Object... nameValue) {
        Map<String, Object> endpoints = new LinkedHashMap<>();
        for (int i = 0; i < nameValue.length; i += 2) endpoints.put((String) nameValue[i], nameValue[i + 1]);
        return Map.of("endpoints", endpoints);
    }
}
