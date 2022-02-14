package io.github.engineeringov.website.api.common.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import io.github.engineeringov.website.api.common.jpa.model.acore.Account;

import java.util.Collection;
import java.util.List;

public class UserDetailsImpl implements UserDetails {

    private final Account account;
    private final List<GrantedAuthority> roles;

    public UserDetailsImpl(Account account, List<GrantedAuthority> roles) {
        this.account = account;
        this.roles = roles;
    }

    public Collection<GrantedAuthority> getAuthorities() {
        return roles;
    }
    public String getPassword() {
        return new String(account.getVerifier());
    }
    public String getUsername() {
        return account.getUsername();
    }
    public boolean isAccountNonExpired() {
        return true;
    }
    public boolean isAccountNonLocked() {
        return !account.isLocked();
    }
    public boolean isCredentialsNonExpired() {
        return true;
    }
    public boolean isEnabled() {
        return true;
    }
    public Account getAccount() {
        return account;
    }
}