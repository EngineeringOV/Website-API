package io.github.engineeringov.website.api.minecraft.service;

import io.github.engineeringov.website.api.minecraft.ConditionalOnMinecraftAuth;
import io.github.engineeringov.website.api.minecraft.MinecraftAuthProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Joins announced by our launchers' clients, waiting for the game server's hasJoined check.
 * In memory: a join lives for seconds, and losing them on restart only means "reconnect".
 */
@Component
@ConditionalOnMinecraftAuth
public class PendingJoins {

    public record Join(String profileId, String name, String ip, Instant expiresAt) {
    }

    private final Map<String, Join> byServerId = new ConcurrentHashMap<>();
    private final MinecraftAuthProperties props;
    private final Clock clock;

    @Autowired
    public PendingJoins(MinecraftAuthProperties props) {
        this(props, Clock.systemUTC());
    }

    PendingJoins(MinecraftAuthProperties props, Clock clock) {
        this.props = props;
        this.clock = clock;
    }

    public void put(String serverId, String profileId, String name, String ip) {
        Instant now = clock.instant();
        byServerId.values().removeIf(j -> j.expiresAt().isBefore(now));
        byServerId.put(serverId, new Join(profileId, name, ip, now.plus(props.getJoinLifetime())));
    }

    /**
     * The join for this server id if the name matches (case-insensitive) and it hasn't expired.
     * Consumed on success, so a join can't be replayed.
     */
    public Optional<Join> take(String serverId, String name, String ip) {
        Join j = byServerId.get(serverId);
        if (j == null || !j.name().equalsIgnoreCase(name)) return Optional.empty();
        if (j.expiresAt().isBefore(clock.instant())) {
            byServerId.remove(serverId, j);
            return Optional.empty();
        }
        if (ip != null && j.ip() != null && !ip.equals(j.ip())) return Optional.empty();
        return byServerId.remove(serverId, j) ? Optional.of(j) : Optional.empty();
    }
}
