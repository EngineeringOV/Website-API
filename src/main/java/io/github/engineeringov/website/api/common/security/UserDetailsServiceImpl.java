package io.github.engineeringov.website.api.common.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import io.github.engineeringov.website.api.common.jpa.repositories.acore.AccountRepository;
import io.github.engineeringov.website.api.common.jpa.model.acore.Account;

import java.util.List;

public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private AccountRepository accountRepository;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        Account domainUser = accountRepository.findByUsername(username);
        if (domainUser == null) {
            throw new UsernameNotFoundException("Could not find user with name '" + username + "'");
        }
        List<GrantedAuthority> roles = List.of(new SimpleGrantedAuthority(domainUser.getAccountAccess().gmLevelToString()));
        return new UserDetailsImpl(domainUser, roles);
    }
}
