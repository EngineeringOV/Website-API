package io.github.engineeringov.website.api.minecraft.service;

import io.github.engineeringov.website.api.minecraft.ConditionalOnMinecraftAuth;
import io.github.engineeringov.website.api.minecraft.MinecraftAuthProperties;
import io.github.engineeringov.website.api.minecraft.MinecraftNames;
import io.github.engineeringov.website.api.minecraft.service.AzerothAccounts.AcAccount;
import io.github.engineeringov.website.api.minecraft.store.MinecraftStore;
import io.github.engineeringov.website.api.minecraft.store.MinecraftStore.Profile;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Logins for our own accounts, and the join check that lets in both ours and real ones.
 *
 * <p>A joining player is ours when our launcher's client announced the join (with our token),
 * otherwise Mojang is asked. Our accounts own their names: a real account with the same name
 * is refused, or with name-clash=SUFFIX recorded as {@code Name(2)}, {@code (3)}... (ours then
 * {@code Name(1)}) for a server plugin to apply, since Minecraft keeps the name the client sent.</p>
 */
@Log4j2
@Service
@ConditionalOnMinecraftAuth
public class MinecraftAuthService {

    public record ProfileRef(String id, String name) {
    }

    /** selectedProfile is null while the account still has to pick a Minecraft name. */
    public record Session(String accessToken, String clientToken, ProfileRef selectedProfile, int accountId) {
    }

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int BULK_LIMIT = 10;

    private final MinecraftAuthProperties props;
    private final MinecraftStore store;
    private final AzerothAccounts accounts;
    private final MojangApi mojang;
    private final PendingJoins joins;
    private final LoginLimiter limiter;
    private final Clock clock;

    @Autowired
    public MinecraftAuthService(MinecraftAuthProperties props, MinecraftStore store, AzerothAccounts accounts,
                                MojangApi mojang, PendingJoins joins, LoginLimiter limiter) {
        this(props, store, accounts, mojang, joins, limiter, Clock.systemUTC());
    }

    MinecraftAuthService(MinecraftAuthProperties props, MinecraftStore store, AzerothAccounts accounts,
                         MojangApi mojang, PendingJoins joins, LoginLimiter limiter, Clock clock) {
        this.props = props;
        this.store = store;
        this.accounts = accounts;
        this.mojang = mojang;
        this.joins = joins;
        this.limiter = limiter;
        this.clock = clock;
    }

    // --- launcher: tokens -----------------------------------------------------------------

    public Session authenticate(String username, String password, String clientToken, String ip) {
        if (blank(username) || blank(password)) throw YggdrasilException.badRequest("Username and password are required.");
        if (limiter.blocked(ip, username)) throw YggdrasilException.tooMany();
        AcAccount acc = accounts.login(username, password).orElse(null);
        if (acc == null) {
            limiter.failed(ip, username);
            log.info("Minecraft login failed for {} from {}", username, ip);
            throw YggdrasilException.forbidden("Invalid credentials. Invalid username or password.");
        }
        limiter.succeeded(username);
        requireAllowed(acc);
        store.deleteExpiredTokens();
        return issue(acc, blank(clientToken) ? newSecret(16) : clientToken, ensureProfile(acc).orElse(null));
    }

    public Session refresh(String accessToken, String clientToken) {
        MinecraftStore.Token t = validToken(accessToken, clientToken)
                .orElseThrow(() -> YggdrasilException.forbidden("Invalid token."));
        AcAccount acc = accounts.byId(t.accountId())
                .orElseThrow(() -> YggdrasilException.forbidden("Invalid token."));
        requireAllowed(acc);
        store.deleteToken(hash(accessToken));
        return issue(acc, t.clientToken(), store.profileByAccount(acc.id()).orElse(null));
    }

    public boolean validate(String accessToken, String clientToken) {
        return validToken(accessToken, clientToken).isPresent();
    }

    public void invalidate(String accessToken) {
        if (!blank(accessToken)) store.deleteToken(hash(accessToken));
    }

    /** Picks the Minecraft name of an account whose login name can't be one. */
    public ProfileRef chooseName(String accessToken, String name) {
        MinecraftStore.Token t = validToken(accessToken, null)
                .orElseThrow(() -> YggdrasilException.forbidden("Invalid token."));
        if (store.profileByAccount(t.accountId()).isPresent())
            throw YggdrasilException.conflict("This account already has a Minecraft name.");
        if (!MinecraftNames.valid(name))
            throw YggdrasilException.badRequest("Names are 3-16 characters: letters, digits and _.");
        if (!nameFree(name, t.accountId()) || !store.createProfile(new Profile(newId(), t.accountId(), name)))
            throw YggdrasilException.conflict("That name is taken.");
        return ref(store.profileByAccount(t.accountId()).orElseThrow());
    }

    // --- game client and server: joining ---------------------------------------------------

    public void join(String accessToken, String profileId, String serverId, String ip) {
        if (blank(serverId) || blank(profileId)) throw YggdrasilException.badRequest("Missing serverId or profile.");
        MinecraftStore.Token t = validToken(accessToken, null)
                .orElseThrow(() -> YggdrasilException.forbidden("Invalid token."));
        Profile p = store.profileByAccount(t.accountId())
                .filter(x -> x.id().equals(normalizeId(profileId)))
                .orElseThrow(() -> YggdrasilException.forbidden("Invalid token."));
        requireAllowed(accounts.byId(t.accountId()).orElseThrow(() -> YggdrasilException.forbidden("Invalid token.")));
        joins.put(serverId, p.id(), p.name(), ip);
    }

    /** The joining player's profile, or empty to refuse them. */
    public Optional<Map<String, Object>> hasJoined(String username, String serverId, String ip) {
        if (blank(username) || blank(serverId)) return Optional.empty();
        Optional<PendingJoins.Join> ours = joins.take(serverId, username, ip);
        if (ours.isPresent()) return store.profileById(ours.get().profileId()).map(this::profileJson);
        if (!props.isMojangFallback()) return Optional.empty();
        return mojang.hasJoined(username, serverId, ip).flatMap(real -> {
            String name = (String) real.get("name");
            if (name == null || store.profileByName(name).isEmpty()) return Optional.of(real);
            if (props.getNameClash() == MinecraftAuthProperties.NameClash.REFUSE) {
                log.info("Refused real account {}: the name belongs to one of our accounts", name);
                return Optional.empty();
            }
            int n = store.suffixFor(name, (String) real.get("id"));
            real.put("name", MinecraftNames.withSuffix(name, n));
            log.info("Real account {} joined; shown as {} by the server plugin", name, real.get("name"));
            return Optional.of(real);
        });
    }

    // --- lookups (game server: /whitelist, /op, skins...) -----------------------------------

    public Optional<Map<String, Object>> profile(String id, boolean unsigned) {
        String norm = normalizeId(id);
        if (norm == null) return Optional.empty();
        Optional<Profile> ours = store.profileById(norm);
        if (ours.isPresent()) return ours.map(this::profileJson);
        if (!props.isMojangFallback()) return Optional.empty();
        return mojang.profile(norm, unsigned).map(this::renameReal);
    }

    public Optional<Map<String, Object>> byName(String name) {
        if (blank(name) || name.length() > MinecraftNames.MAX) return Optional.empty();
        MinecraftNames.Suffixed s = MinecraftNames.parse(name);
        if (s != null) return bySuffixedName(s);
        Optional<Profile> ours = store.profileByName(name);
        if (ours.isPresent()) return Optional.of(idAndName(ours.get().id(), displayName(ours.get())));
        if (!props.isMojangFallback()) return Optional.empty();
        return mojang.byName(name).map(this::renameReal).map(m -> idAndName((String) m.get("id"), (String) m.get("name")));
    }

    public List<Map<String, Object>> byNames(List<String> names) {
        List<Map<String, Object>> found = new ArrayList<>();
        List<String> real = new ArrayList<>();
        for (String n : names.stream().filter(x -> !blank(x)).distinct().limit(BULK_LIMIT).toList()) {
            if (MinecraftNames.parse(n) != null || store.profileByName(n).isPresent()) byName(n).ifPresent(found::add);
            else real.add(n);
        }
        if (props.isMojangFallback())
            mojang.byNames(real).forEach(m -> found.add(idAndName((String) m.get("id"), (String) renameReal(m).get("name"))));
        return found;
    }

    /**
     * The name a joined player should be shown with, for the server plugin: Name(1) for ours and
     * Name(2)... for real accounts once their names clash, else the name unchanged.
     * Database only, so a join never waits on Mojang.
     */
    public String shownName(String id, String name) {
        String norm = normalizeId(id);
        if (norm == null || blank(name)) throw YggdrasilException.badRequest("id and name are required.");
        Optional<Profile> ours = store.profileById(norm);
        if (ours.isPresent()) return displayName(ours.get());
        return store.clash(name, norm).map(c -> MinecraftNames.withSuffix(name, c.suffix())).orElse(name);
    }

    /** True when the id is one of our accounts (only we hand out those UUIDs; real accounts have Mojang's). */
    public boolean isOwn(String id) {
        String norm = normalizeId(id);
        return norm != null && store.profileById(norm).isPresent();
    }

    // --- helpers ---------------------------------------------------------------------------

    private Optional<Map<String, Object>> bySuffixedName(MinecraftNames.Suffixed s) {
        if (s.n() == 1) {
            return store.profilesByPrefix(s.base()).stream()
                    .filter(p -> s.is(p.name()) && store.clashExists(p.name()))
                    .findFirst().map(p -> idAndName(p.id(), MinecraftNames.withSuffix(p.name(), 1)));
        }
        return store.clashesByPrefix(s.base(), s.n()).stream()
                .filter(c -> s.is(c.name()))
                .findFirst().map(c -> idAndName(c.mojangId(), MinecraftNames.withSuffix(c.name(), c.suffix())));
    }

    /** A real profile from Mojang, renamed if its name clashed with ours when it joined. */
    private Map<String, Object> renameReal(Map<String, Object> real) {
        String name = (String) real.get("name");
        String id = (String) real.get("id");
        if (name == null || id == null) return real;
        store.clash(name, id).ifPresent(c -> real.put("name", MinecraftNames.withSuffix(name, c.suffix())));
        return real;
    }

    /** Ours keep their name until a real player with that name has joined. */
    private String displayName(Profile p) {
        return store.clashExists(p.name()) ? MinecraftNames.withSuffix(p.name(), 1) : p.name();
    }

    /** No skin for our accounts: Minecraft only accepts skins hosted by Mojang. */
    private Map<String, Object> profileJson(Profile p) {
        Map<String, Object> m = idAndName(p.id(), displayName(p));
        m.put("properties", List.of());
        return m;
    }

    private static Map<String, Object> idAndName(String id, String name) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("name", name);
        return m;
    }

    /**
     * The client keeps the plain name (Paper refuses other characters in the login packet);
     * the suffix is applied by hasJoined.
     */
    private ProfileRef ref(Profile p) {
        return new ProfileRef(p.id(), p.name());
    }

    private Optional<Profile> ensureProfile(AcAccount acc) {
        Optional<Profile> p = store.profileByAccount(acc.id());
        if (p.isPresent() || !MinecraftNames.valid(acc.username()) || !nameFree(acc.username(), acc.id())) return p;
        store.createProfile(new Profile(newId(), acc.id(), displayCase(acc.username())));
        return store.profileByAccount(acc.id());
    }

    /** AzerothCore stores login names in capitals: BOB -> Bob. Names are case-insensitive anyway. */
    static String displayCase(String name) {
        if (!name.equals(name.toUpperCase(Locale.ROOT))) return name;
        return name.charAt(0) + name.substring(1).toLowerCase(Locale.ROOT);
    }

    /** Free: no profile uses it, and it isn't another account's login name (they get first pick). */
    private boolean nameFree(String name, int accountId) {
        return store.profileByName(name).isEmpty()
                && accounts.idByUsername(name).map(id -> id == accountId).orElse(true);
    }

    private Session issue(AcAccount acc, String clientToken, Profile profile) {
        String token = newSecret(32);
        store.saveToken(hash(token), clientToken, acc.id(), clock.instant().plus(props.getTokenLifetime()));
        return new Session(token, clientToken, profile == null ? null : ref(profile), acc.id());
    }

    private Optional<MinecraftStore.Token> validToken(String accessToken, String clientToken) {
        if (blank(accessToken)) return Optional.empty();
        Instant now = clock.instant();
        return store.token(hash(accessToken))
                .filter(t -> t.expiresAt().isAfter(now))
                .filter(t -> blank(clientToken) || clientToken.equals(t.clientToken()));
    }

    private void requireAllowed(AcAccount acc) {
        if (acc.locked() || accounts.banned(acc.id())) throw YggdrasilException.forbidden("This account is banned.");
    }

    static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String newSecret(int bytes) {
        byte[] b = new byte[bytes];
        RANDOM.nextBytes(b);
        return HexFormat.of().formatHex(b);
    }

    private static String newId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /** 32 lowercase hex characters, dashes removed; null if it isn't a UUID. */
    static String normalizeId(String id) {
        if (id == null) return null;
        String s = id.replace("-", "").toLowerCase(Locale.ROOT);
        return s.matches("[0-9a-f]{32}") ? s : null;
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
