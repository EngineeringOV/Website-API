package io.github.engineeringov.website.api.minecraft.web;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiscoveryTest {

    /** Every service and endpoint Mojang's own document has, so Minecraft finds all it looks for. */
    @Test
    @SuppressWarnings("unchecked")
    void sameShapeAsMojangsAndPointsHere() {
        Map<String, Object> doc = Discovery.document("https://example.com/api/minecraft");
        Map<String, Object> d = (Map<String, Object>) doc.get("discovery");
        assertEquals(Set.of("authentication", "session", "player", "profiles", "telemetry", "signaling"), d.keySet());
        Map<String, Object> session = endpoints(d, "session");
        assertEquals(Set.of("getProfileById", "verify", "join"), session.keySet());
        assertEquals("https://example.com/api/minecraft/session/minecraft/hasJoined",
                ((Map<String, Object>) session.get("verify")).get("uri"));
        assertEquals(Set.of("updatePresence", "sendReport", "getAttributes", "getFriends", "getCertificates",
                "updateAttributes", "updateFriends", "getBlocklist"), endpoints(d, "player").keySet());
        assertEquals(Set.of("getPublicKeys", "loginXbox"), endpoints(d, "authentication").keySet());
        assertEquals(Set.of("getManyByName", "getByName", "getTexture"), endpoints(d, "profiles").keySet());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> endpoints(Map<String, Object> d, String service) {
        return (Map<String, Object>) ((Map<String, Object>) d.get(service)).get("endpoints");
    }
}
