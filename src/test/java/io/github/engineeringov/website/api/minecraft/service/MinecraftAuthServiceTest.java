package io.github.engineeringov.website.api.minecraft.service;

import io.github.engineeringov.website.api.minecraft.MinecraftAuthProperties;
import io.github.engineeringov.website.api.minecraft.MinecraftNames;
import io.github.engineeringov.website.api.minecraft.service.AzerothAccounts.AcAccount;
import io.github.engineeringov.website.api.minecraft.store.MinecraftStore;
import io.github.engineeringov.website.api.minecraft.store.MinecraftStore.Clash;
import io.github.engineeringov.website.api.minecraft.store.MinecraftStore.Profile;
import io.github.engineeringov.website.api.minecraft.store.MinecraftStore.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** The rules, against an in-memory store and a fake Mojang. */
class MinecraftAuthServiceTest {

    static final String REAL_BOB = "0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f";

    MinecraftAuthProperties props = new MinecraftAuthProperties();
    FakeStore store = new FakeStore();
    AzerothAccounts accounts = mock(AzerothAccounts.class);
    MojangApi mojang = mock(MojangApi.class);
    Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
    PendingJoins joins = new PendingJoins(props, clock);
    LoginLimiter limiter = new LoginLimiter(props, clock);
    MinecraftAuthService auth;

    @BeforeEach
    void setUp() {
        auth = new MinecraftAuthService(props, store, accounts, mojang, joins, limiter, clock);
        account(1, "Bob", "secret");
        account(2, "john.doe", "pw");
        when(accounts.idByUsername(anyString())).thenReturn(Optional.empty());
        when(accounts.idByUsername(argThat(n -> n.equalsIgnoreCase("bob")))).thenReturn(Optional.of(1));
        when(mojang.hasJoined(anyString(), anyString(), any())).thenReturn(Optional.empty());
        when(mojang.byNames(anyList())).thenReturn(List.of());
    }

    void account(int id, String name, String password) {
        AcAccount a = new AcAccount(id, name, false);
        when(accounts.login(name, password)).thenReturn(Optional.of(a));
        when(accounts.byId(id)).thenReturn(Optional.of(a));
    }

    @Test
    void loginGivesProfileNamedAfterAccount() {
        MinecraftAuthService.Session s = auth.authenticate("Bob", "secret", null, "1.2.3.4");
        assertEquals("Bob", s.selectedProfile().name());
        assertEquals(32, s.selectedProfile().id().length());
        assertEquals(64, s.accessToken().length());
        assertNotNull(s.clientToken());
        assertFalse(store.tokens.containsKey(s.accessToken()), "only the hash is stored");
        // same profile next time
        assertEquals(s.selectedProfile().id(), auth.authenticate("Bob", "secret", "c", "1.2.3.4").selectedProfile().id());
    }

    @Test
    void capitalLoginNamesGetNormalCase() {
        account(7, "CAROL", "pw");
        assertEquals("Carol", auth.authenticate("CAROL", "pw", null, "ip").selectedProfile().name());
        assertEquals("Bob_1", MinecraftAuthService.displayCase("BOB_1"));
        assertEquals("McBob", MinecraftAuthService.displayCase("McBob"));
    }

    @Test
    void invalidAccountNameMustChooseOne() {
        MinecraftAuthService.Session s = auth.authenticate("john.doe", "pw", "c", "1.2.3.4");
        assertNull(s.selectedProfile());
        YggdrasilException bad = assertThrows(YggdrasilException.class, () -> auth.chooseName(s.accessToken(), "j.d"));
        assertEquals(HttpStatus.BAD_REQUEST, bad.getStatus());
        // another account's login name is reserved for it
        YggdrasilException taken = assertThrows(YggdrasilException.class, () -> auth.chooseName(s.accessToken(), "bob"));
        assertEquals(HttpStatus.CONFLICT, taken.getStatus());
        assertEquals("John_Doe", auth.chooseName(s.accessToken(), "John_Doe").name());
        assertEquals("John_Doe", auth.refresh(s.accessToken(), "c").selectedProfile().name());
    }

    @Test
    void wrongPasswordIsRateLimited() {
        for (int i = 0; i < props.getLoginAttempts(); i++)
            assertEquals(HttpStatus.FORBIDDEN, assertThrows(YggdrasilException.class,
                    () -> auth.authenticate("Bob", "nope", null, "1.2.3.4")).getStatus());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, assertThrows(YggdrasilException.class,
                () -> auth.authenticate("Bob", "secret", null, "1.2.3.4")).getStatus());
    }

    @Test
    void bannedAccountRefused() {
        when(accounts.banned(1)).thenReturn(true);
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(YggdrasilException.class,
                () -> auth.authenticate("Bob", "secret", null, "1.2.3.4")).getStatus());
    }

    @Test
    void tokensRefreshValidateInvalidate() {
        MinecraftAuthService.Session s = auth.authenticate("Bob", "secret", "client", "ip");
        assertTrue(auth.validate(s.accessToken(), "client"));
        assertFalse(auth.validate(s.accessToken(), "other-client"));
        MinecraftAuthService.Session r = auth.refresh(s.accessToken(), "client");
        assertFalse(auth.validate(s.accessToken(), null), "old token is replaced");
        assertTrue(auth.validate(r.accessToken(), null));
        auth.invalidate(r.accessToken());
        assertFalse(auth.validate(r.accessToken(), null));
    }

    @Test
    void ourPlayerJoinsWithoutAskingMojang() {
        MinecraftAuthService.Session s = auth.authenticate("Bob", "secret", null, "ip");
        auth.join(s.accessToken(), s.selectedProfile().id(), "server-hash", "ip");
        Map<String, Object> p = auth.hasJoined("Bob", "server-hash", null).orElseThrow();
        assertEquals(s.selectedProfile().id(), p.get("id"));
        assertEquals("Bob", p.get("name"));
        verify(mojang, never()).hasJoined(anyString(), anyString(), any());
        assertTrue(auth.hasJoined("Bob", "server-hash", null).isEmpty(), "a join can't be replayed");
    }

    @Test
    void joinNeedsOwnProfileAndValidToken() {
        MinecraftAuthService.Session s = auth.authenticate("Bob", "secret", null, "ip");
        assertThrows(YggdrasilException.class, () -> auth.join("bad", s.selectedProfile().id(), "h", "ip"));
        assertThrows(YggdrasilException.class, () -> auth.join(s.accessToken(), REAL_BOB, "h", "ip"));
    }

    @Test
    void unknownPlayerFallsBackToMojangOrIsRefused() {
        when(mojang.hasJoined("Alice", "h", null)).thenReturn(Optional.of(real("aaaa0000aaaa0000aaaa0000aaaa0000", "Alice")));
        assertEquals("Alice", auth.hasJoined("Alice", "h", null).orElseThrow().get("name"));
        assertTrue(auth.hasJoined("Nobody", "h", null).isEmpty());
        props.setMojangFallback(false);
        assertTrue(auth.hasJoined("Alice", "h", null).isEmpty());
    }

    @Test
    void realAccountWithOurNameIsRefusedByDefault() {
        auth.authenticate("Bob", "secret", null, "ip");
        when(mojang.hasJoined(eq("Bob"), anyString(), isNull())).thenReturn(Optional.of(real(REAL_BOB, "Bob")));
        assertTrue(auth.hasJoined("Bob", "h1", null).isEmpty());
        assertTrue(store.clashes.isEmpty());
    }

    @Test
    void nameClashGivesSuffixes() {
        props.setNameClash(MinecraftAuthProperties.NameClash.SUFFIX);
        MinecraftAuthService.Session s = auth.authenticate("Bob", "secret", null, "ip");
        String ours = s.selectedProfile().id();
        when(mojang.hasJoined(eq("Bob"), anyString(), isNull())).thenReturn(Optional.of(real(REAL_BOB, "Bob")));
        // someone with a real "Bob" account joins: becomes Bob(2), and ours Bob(1) from now on
        assertEquals("Bob(2)", auth.hasJoined("Bob", "h1", null).orElseThrow().get("name"));
        assertEquals("Bob(2)", auth.hasJoined("Bob", "h2", null).orElseThrow().get("name"), "stable");
        auth.join(s.accessToken(), ours, "h3", "ip");
        assertEquals("Bob(1)", auth.hasJoined("Bob", "h3", null).orElseThrow().get("name"));
        // a different real "Bob" later (Mojang names change hands)
        when(mojang.hasJoined(eq("Bob"), eq("h4"), isNull())).thenReturn(Optional.of(real("1111222233334444555566667777aaaa", "Bob")));
        assertEquals("Bob(3)", auth.hasJoined("Bob", "h4", null).orElseThrow().get("name"));
        // lookups resolve the suffixed names
        assertEquals(ours, auth.byName("Bob(1)").orElseThrow().get("id"));
        assertEquals(REAL_BOB, auth.byName("bob(2)").orElseThrow().get("id"));
        assertEquals(ours, auth.byName("Bob").orElseThrow().get("id"), "plain name = ours");
        assertEquals("Bob(1)", auth.profile(ours, true).orElseThrow().get("name"));
        when(mojang.profile(eq(REAL_BOB), anyBoolean())).thenReturn(Optional.of(real(REAL_BOB, "Bob")));
        assertEquals("Bob(2)", auth.profile(REAL_BOB, true).orElseThrow().get("name"));
        // what the server plugin asks for
        assertEquals("Bob(1)", auth.shownName(ours, "Bob"));
        assertEquals("Bob(2)", auth.shownName(REAL_BOB, "Bob"));
        assertEquals("Steve", auth.shownName("5555aaaa5555aaaa5555aaaa5555aaaa", "Steve"));
    }

    @Test
    void noClashWithoutOurProfile() {
        when(mojang.hasJoined("Steve", "h", null)).thenReturn(Optional.of(real("5555", "Steve")));
        assertEquals("Steve", auth.hasJoined("Steve", "h", null).orElseThrow().get("name"));
        assertTrue(store.clashes.isEmpty());
    }

    static Map<String, Object> real(String id, String name) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("name", name);
        m.put("properties", List.of(Map.of("name", "textures", "value", "x", "signature", "y")));
        return m;
    }

    /** MinecraftStore in memory. */
    static class FakeStore extends MinecraftStore {
        final List<Profile> profiles = new ArrayList<>();
        final Map<String, Token> tokens = new HashMap<>();
        final List<Clash> clashes = new ArrayList<>();

        FakeStore() {
            super(null);
        }

        @Override public Optional<Profile> profileByAccount(int a) { return profiles.stream().filter(p -> p.accountId() == a).findFirst(); }
        @Override public Optional<Profile> profileById(String id) { return profiles.stream().filter(p -> p.id().equals(id)).findFirst(); }
        @Override public Optional<Profile> profileByName(String n) { return profiles.stream().filter(p -> p.name().equalsIgnoreCase(n)).findFirst(); }
        @Override public List<Profile> profilesByPrefix(String pre) { return profiles.stream().filter(p -> MinecraftNames.key(p.name()).startsWith(MinecraftNames.key(pre))).toList(); }
        @Override public boolean createProfile(Profile p) {
            if (profileByName(p.name()).isPresent() || profileByAccount(p.accountId()).isPresent()) return false;
            return profiles.add(p);
        }
        @Override public void saveToken(String h, String c, int a, Instant e) { tokens.put(h, new Token(a, c, e)); }
        @Override public Optional<Token> token(String h) { return Optional.ofNullable(tokens.get(h)); }
        @Override public void deleteToken(String h) { tokens.remove(h); }
        @Override public void deleteExpiredTokens() { }
        @Override public boolean clashExists(String n) { return clashes.stream().anyMatch(c -> c.name().equalsIgnoreCase(n)); }
        @Override public Optional<Clash> clash(String n, String id) { return clashes.stream().filter(c -> c.name().equalsIgnoreCase(n) && c.mojangId().equals(id)).findFirst(); }
        @Override public List<Clash> clashesByPrefix(String pre, int s) { return clashes.stream().filter(c -> c.suffix() == s && MinecraftNames.key(c.name()).startsWith(MinecraftNames.key(pre))).toList(); }
        @Override public int suffixFor(String n, String id) {
            return clash(n, id).map(Clash::suffix).orElseGet(() -> {
                int next = clashes.stream().filter(c -> c.name().equalsIgnoreCase(n)).mapToInt(Clash::suffix).max().orElse(1) + 1;
                clashes.add(new Clash(n, id, next));
                return next;
            });
        }
        @Override public boolean accountBanned(int id) { return false; }
    }
}
