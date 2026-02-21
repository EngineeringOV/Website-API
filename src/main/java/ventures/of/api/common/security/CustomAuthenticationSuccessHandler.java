package ventures.of.api.common.security;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import ventures.of.api.common.jpa.model.acore.Account;
import ventures.of.api.common.jpa.repositories.acore.AccountRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private AccountRepository accountRepository;

    @SneakyThrows
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // Get the authenticated user
        User user = (User) authentication.getPrincipal();
        // Look up the user in the database
        Account dbUser = accountRepository.findByUsername(user.getUsername());
        if (dbUser == null) {
            // Return an error if the user does not exist
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        //Authentication newAuthentication = new UsernamePasswordAuthenticationToken(dbUser, authentication.getCredentials(), authentication.getAuthorities());
        //SecurityContextHolder.getContext().setAuthentication(newAuthentication);
        // Redirect the user to the appropriate page
        response.sendRedirect("/home");

    }

}