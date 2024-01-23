package ventures.of.api.common.security.util;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ventures.of.api.common.security.UserDetailsImpl;
import ventures.of.api.common.jpa.model.acore.Account;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for working with Spring Security.
 */
public class SecurityUtil {
    private SecurityUtil() {}

    public static Account getLoggedInUser() {
        Account user = null;
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication auth;
        if (securityContext != null) {
            auth = securityContext.getAuthentication();
            if (auth != null) {
                Object principal = auth.getPrincipal();
                if (principal instanceof UserDetailsImpl) {
                    UserDetailsImpl authUser = (UserDetailsImpl) principal;
                    user = authUser.getAccount();
                }
            }
        }
        return user;
    }

    public static Authentication signInUser(Account user) {
        List<GrantedAuthority> roles = getRoles(user);
        UserDetailsImpl springSecurityUser = new UserDetailsImpl(user, roles);
        Authentication authentication = new UsernamePasswordAuthenticationToken(springSecurityUser, new String(user.getVerifier()), roles);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    public static List<GrantedAuthority> getRoles(Account user) {
        List<GrantedAuthority> roles = new ArrayList<>();
        if(user.getAccountAccess() != null) {
            roles.add(new SimpleGrantedAuthority(user.getAccountAccess().gmLevelToString()));
            return roles;
        }
        else {
            roles.add(new SimpleGrantedAuthority("ROLE_PLAYER"));
            return roles;
        }
    }
}
