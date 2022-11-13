package ventures.of.api.common.security;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class CSRFTokenGeneratorFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");

        response.setHeader("X-CSRF-HEADER", token.getHeaderName());
        response.setHeader("X-CSRF-PARAM", token.getParameterName());
        response.setHeader("X-CSRF-TOKEN", token.getToken());
        filterChain.doFilter(request, response);
    }
}