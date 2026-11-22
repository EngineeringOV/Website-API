package io.github.engineeringov.website.api.minecraft;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** {@code minecraft.auth.*} settings. */
@Data
@ConfigurationProperties(prefix = "minecraft.auth")
public class MinecraftAuthProperties {
    /** Master switch; everything Minecraft is off unless true. */
    private boolean enabled = false;
    /** Public URL of the Minecraft endpoints, as players' launchers and the game server reach them. */
    private String publicUrl;
    /** Shown by the launcher. */
    private String name = "Minecraft";
    /** host[:port] the launcher adds to the in-game server list; empty = the auth server's host. */
    private String gameServer;
    /** Let real (Mojang) accounts join too. */
    private boolean mojangFallback = true;
    /**
     * A real account whose name belongs to one of ours. Minecraft always uses the name the
     * player's client sent, so only a server plugin can rename:
     * REFUSE (default, works everywhere) or SUFFIX (record Name(2)... for a renaming plugin).
     */
    private NameClash nameClash = NameClash.REFUSE;

    public enum NameClash { REFUSE, SUFFIX }
    /** Access token lifetime, extended on every refresh. */
    private Duration tokenLifetime = Duration.ofDays(30);
    /** Failed logins allowed per IP and account name within {@link #loginWindow}. */
    private int loginAttempts = 5;
    private Duration loginWindow = Duration.ofMinutes(5);
    /** How long a join stays valid between the client's join and the server's check. */
    private Duration joinLifetime = Duration.ofSeconds(30);

    /** Mojang endpoints, overridable for tests. */
    private String mojangSessionUrl = "https://sessionserver.mojang.com";
    private String mojangApiUrl = "https://api.mojang.com";
    private String mojangServicesUrl = "https://api.minecraftservices.com";
}
