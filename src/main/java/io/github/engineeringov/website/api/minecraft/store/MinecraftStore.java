package io.github.engineeringov.website.api.minecraft.store;

import io.github.engineeringov.website.api.minecraft.ConditionalOnMinecraftAuth;
import io.github.engineeringov.website.api.minecraft.MinecraftNames;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * The mc_* tables in acore_custom. Plain JDBC rather than JPA entities, so Hibernate's
 * ddl-auto never creates them while Minecraft is switched off.
 */
@Repository
@ConditionalOnMinecraftAuth
public class MinecraftStore {

    public record Profile(String id, int accountId, String name) {
    }

    public record Token(int accountId, String clientToken, Instant expiresAt) {
    }

    public record Clash(String name, String mojangId, int suffix) {
    }

    private static final RowMapper<Profile> PROFILE =
            (rs, i) -> new Profile(rs.getString("id"), rs.getInt("account_id"), rs.getString("name"));

    private final JdbcTemplate jdbc;

    public MinecraftStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // --- profiles -------------------------------------------------------------------------

    public Optional<Profile> profileByAccount(int accountId) {
        return one(jdbc.query("SELECT id, account_id, name FROM acore_custom.mc_profile WHERE account_id = ?",
                PROFILE, accountId));
    }

    public Optional<Profile> profileById(String id) {
        return one(jdbc.query("SELECT id, account_id, name FROM acore_custom.mc_profile WHERE id = ?",
                PROFILE, id));
    }

    public Optional<Profile> profileByName(String name) {
        return one(jdbc.query("SELECT id, account_id, name FROM acore_custom.mc_profile WHERE name_key = ?",
                PROFILE, MinecraftNames.key(name)));
    }

    /** Profiles whose name starts with {@code prefix}: to resolve names cut by a suffix. */
    public List<Profile> profilesByPrefix(String prefix) {
        return jdbc.query("SELECT id, account_id, name FROM acore_custom.mc_profile WHERE name_key LIKE ?",
                PROFILE, likePrefix(prefix));
    }

    /** False when the name was taken in the meantime. */
    public boolean createProfile(Profile p) {
        try {
            jdbc.update("INSERT INTO acore_custom.mc_profile (id, account_id, name, name_key, created_at) VALUES (?, ?, ?, ?, ?)",
                    p.id(), p.accountId(), p.name(), MinecraftNames.key(p.name()), Timestamp.from(Instant.now()));
            return true;
        } catch (DuplicateKeyException e) {
            return false;
        }
    }

    // --- tokens (stored as SHA-256, never in clear) ----------------------------------------

    public void saveToken(String tokenHash, String clientToken, int accountId, Instant expiresAt) {
        jdbc.update("INSERT INTO acore_custom.mc_token (token_hash, client_token, account_id, expires_at) VALUES (?, ?, ?, ?)",
                tokenHash, clientToken, accountId, Timestamp.from(expiresAt));
    }

    public Optional<Token> token(String tokenHash) {
        return one(jdbc.query("SELECT account_id, client_token, expires_at FROM acore_custom.mc_token WHERE token_hash = ?",
                (rs, i) -> new Token(rs.getInt("account_id"), rs.getString("client_token"),
                        rs.getTimestamp("expires_at").toInstant()), tokenHash));
    }

    public void deleteToken(String tokenHash) {
        jdbc.update("DELETE FROM acore_custom.mc_token WHERE token_hash = ?", tokenHash);
    }

    public void deleteExpiredTokens() {
        jdbc.update("DELETE FROM acore_custom.mc_token WHERE expires_at < ?", Timestamp.from(Instant.now()));
    }

    // --- name clashes with real accounts ----------------------------------------------------

    public boolean clashExists(String name) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM acore_custom.mc_name_clash WHERE name_key = ?",
                Integer.class, MinecraftNames.key(name));
        return n != null && n > 0;
    }

    public Optional<Clash> clash(String name, String mojangId) {
        return one(jdbc.query("SELECT name, mojang_id, suffix FROM acore_custom.mc_name_clash WHERE name_key = ? AND mojang_id = ?",
                (rs, i) -> new Clash(rs.getString(1), rs.getString(2), rs.getInt(3)), MinecraftNames.key(name), mojangId));
    }

    /** Clash rows with this suffix whose name starts with {@code prefix}. */
    public List<Clash> clashesByPrefix(String prefix, int suffix) {
        return jdbc.query("SELECT name, mojang_id, suffix FROM acore_custom.mc_name_clash WHERE name_key LIKE ? AND suffix = ?",
                (rs, i) -> new Clash(rs.getString(1), rs.getString(2), rs.getInt(3)), likePrefix(prefix), suffix);
    }

    /** The real player's suffix for this name, recorded on first sight: 2, then 3... */
    public int suffixFor(String name, String mojangId) {
        String key = MinecraftNames.key(name);
        for (int attempt = 0; attempt < 5; attempt++) {
            Optional<Clash> known = clash(name, mojangId);
            if (known.isPresent()) return known.get().suffix();
            Integer max = jdbc.queryForObject("SELECT MAX(suffix) FROM acore_custom.mc_name_clash WHERE name_key = ?",
                    Integer.class, key);
            int next = max == null ? 2 : max + 1;
            try {
                jdbc.update("INSERT INTO acore_custom.mc_name_clash (name_key, mojang_id, name, suffix, created_at) VALUES (?, ?, ?, ?, ?)",
                        key, mojangId, name, next, Timestamp.from(Instant.now()));
                return next;
            } catch (DuplicateKeyException raced) {
                // another join took that suffix at the same moment: look again
            }
        }
        throw new IllegalStateException("Could not record name clash for " + name);
    }

    // --- AzerothCore (read only) ------------------------------------------------------------

    /** Active ban: permanent (unbandate = bandate) or not yet expired. */
    public boolean accountBanned(int accountId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM acore_auth.account_banned WHERE id = ? AND active = 1 "
                        + "AND (unbandate = bandate OR unbandate > UNIX_TIMESTAMP())",
                Integer.class, accountId);
        return n != null && n > 0;
    }

    private static String likePrefix(String prefix) {
        return MinecraftNames.key(prefix).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }

    private static <T> Optional<T> one(List<T> rows) {
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }
}
