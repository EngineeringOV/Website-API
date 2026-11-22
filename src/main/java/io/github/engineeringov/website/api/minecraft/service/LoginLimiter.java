package io.github.engineeringov.website.api.minecraft.service;

import io.github.engineeringov.website.api.minecraft.ConditionalOnMinecraftAuth;
import io.github.engineeringov.website.api.minecraft.MinecraftAuthProperties;
import io.github.engineeringov.website.api.minecraft.MinecraftNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Failed logins per IP and per account name within a sliding window. */
@Component
@ConditionalOnMinecraftAuth
public class LoginLimiter {
    private final Map<String, Deque<Instant>> failures = new ConcurrentHashMap<>();
    private final MinecraftAuthProperties props;
    private final Clock clock;

    @Autowired
    public LoginLimiter(MinecraftAuthProperties props) {
        this(props, Clock.systemUTC());
    }

    LoginLimiter(MinecraftAuthProperties props, Clock clock) {
        this.props = props;
        this.clock = clock;
    }

    public boolean blocked(String ip, String username) {
        return count("ip:" + ip) >= props.getLoginAttempts() * 4
                || count("user:" + MinecraftNames.key(username)) >= props.getLoginAttempts();
    }

    public void failed(String ip, String username) {
        add("ip:" + ip);
        add("user:" + MinecraftNames.key(username));
    }

    public void succeeded(String username) {
        failures.remove("user:" + MinecraftNames.key(username));
    }

    private int count(String key) {
        Deque<Instant> d = failures.get(key);
        if (d == null) return 0;
        synchronized (d) {
            prune(d);
            return d.size();
        }
    }

    private void add(String key) {
        Deque<Instant> d = failures.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (d) {
            prune(d);
            d.addLast(clock.instant());
        }
        if (failures.size() > 10_000) failures.entrySet().removeIf(e -> {
            synchronized (e.getValue()) {
                prune(e.getValue());
                return e.getValue().isEmpty();
            }
        });
    }

    private void prune(Deque<Instant> d) {
        Instant cutoff = clock.instant().minus(props.getLoginWindow());
        while (!d.isEmpty() && d.peekFirst().isBefore(cutoff)) d.pollFirst();
    }
}
