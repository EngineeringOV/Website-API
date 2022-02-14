package io.github.engineeringov.website.api.common.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {



    private final String websiteUrl;
    private final CustomAuthenticationProvider customAuthenticationProvider;

    public WebSecurityConfig(@Autowired CustomAuthenticationProvider customAuthenticationProvider,
                             @Value("${api.customization.website.url}") String websiteUrl) {
        this.customAuthenticationProvider = customAuthenticationProvider;
        this.websiteUrl = websiteUrl;
    }

    @Bean
    @Override
    public UserDetailsServiceImpl userDetailsService() {
        return new UserDetailsServiceImpl();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf()
                .and()
                .cors()
                .and()
                .authorizeRequests()
                .antMatchers("/**/authenticated/*").hasAnyRole("PLAYER", "GM", "ADMIN")
                .antMatchers("/**/DEV/*").hasRole("ADMIN")
                .and()
                .formLogin()
                //.loginPage("/login")
                .and()
                .logout()
                .logoutUrl("/logout")
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(customAuthenticationProvider);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.addAllowedOrigin(websiteUrl);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS" /* "PUT", "PATCH", "DELETE", */));
        //configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token", "cookie"));
        configuration.addAllowedHeader(CorsConfiguration.ALL);
        configuration.setExposedHeaders(List.of("Access-Control-Allow-Credentials", "x-auth-token"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}