package io.github.engineeringov.website.api.common.security;

import io.github.engineeringov.website.api.common.jpa.model.acore.Account;
import io.github.engineeringov.website.api.common.jpa.repositories.acore.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private SRP6PasswordEncoder srp6PasswordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication.getCredentials() == null && authentication.isAuthenticated()) {
            return authentication;
        }
        String inputUsername = authentication.getName();
        String modifiedInputPassword = (inputUsername + ":" + authentication.getCredentials()).toUpperCase();
        Account account = accountRepository.findByUsername(authentication.getName().toUpperCase());

        if (account == null) {
            throw new UsernameNotFoundException("Invalid credentials");
        } else if (!srp6PasswordEncoder.efficientMatches(modifiedInputPassword, account)) {
            throw new AuthenticationException("Invalid credentials") {
            };
        }
        GrantedAuthority userRole = new SimpleGrantedAuthority(account.getAccountAccess().gmLevelToString());
        Authentication modifiedAuthentication = new UsernamePasswordAuthenticationToken(inputUsername, authentication.getCredentials(), List.of(userRole));
        SecurityContextHolder.getContext().setAuthentication(modifiedAuthentication);

        return modifiedAuthentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}