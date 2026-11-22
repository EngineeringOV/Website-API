package io.github.engineeringov.website.api.minecraft.service;

import io.github.engineeringov.website.api.common.jpa.model.acore.Account;
import io.github.engineeringov.website.api.common.jpa.repositories.acore.AccountRepository;
import io.github.engineeringov.website.api.common.security.SRP6PasswordEncoder;
import io.github.engineeringov.website.api.minecraft.ConditionalOnMinecraftAuth;
import io.github.engineeringov.website.api.minecraft.store.MinecraftStore;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Read-only view of the AzerothCore accounts, checked exactly like the website login. */
@Component
@ConditionalOnMinecraftAuth
public class AzerothAccounts {

    public record AcAccount(int id, String username, boolean locked) {
    }

    private final AccountRepository accounts;
    private final SRP6PasswordEncoder srp6;
    private final MinecraftStore store;

    public AzerothAccounts(AccountRepository accounts, SRP6PasswordEncoder srp6, MinecraftStore store) {
        this.accounts = accounts;
        this.srp6 = srp6;
        this.store = store;
    }

    /** The account if the password is right, else empty. */
    public Optional<AcAccount> login(String username, String password) {
        Account a = accounts.findByUsername(username.toUpperCase());
        if (a == null || !srp6.efficientMatches((username + ":" + password).toUpperCase(), a)) return Optional.empty();
        return Optional.of(new AcAccount(a.getId(), a.getUsername(), a.isLocked()));
    }

    public Optional<AcAccount> byId(int id) {
        return accounts.findById(id).map(a -> new AcAccount(a.getId(), a.getUsername(), a.isLocked()));
    }

    /** Id of the account with this login name, if any. */
    public Optional<Integer> idByUsername(String username) {
        return Optional.ofNullable(accounts.findByUsername(username.toUpperCase())).map(Account::getId);
    }

    public boolean banned(int accountId) {
        return store.accountBanned(accountId);
    }
}
