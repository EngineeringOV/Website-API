package ventures.of.api.common.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ventures.of.api.common.jpa.acore.AccountRepository;
import ventures.of.api.model.db.acore.Account;

import java.util.List;

public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private AccountRepository accountRepository;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        Account domainUser = accountRepository.findByUsername(username);
        if (domainUser == null) {
            throw new UsernameNotFoundException("Could not find user with name '" + username + "'");
        }
        List<GrantedAuthority> roles = SecurityUtil.getRoles(domainUser);
        return new UserDetailsImpl(domainUser, roles);
    }
}
