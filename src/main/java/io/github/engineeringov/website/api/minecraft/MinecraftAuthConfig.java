package io.github.engineeringov.website.api.minecraft;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

/**
 * Minecraft accounts on top of the AzerothCore ones. Loaded only with
 * {@code minecraft.auth.enabled=true}; the AzerothCore side doesn't depend on any of it.
 */
@Log4j2
@Configuration
@ConditionalOnMinecraftAuth
@EnableConfigurationProperties(MinecraftAuthProperties.class)
public class MinecraftAuthConfig {

    public static final String PATH = "/minecraft";

    public MinecraftAuthConfig(MinecraftAuthProperties props,
                               @Value("${api.customization.website.url:}") String websiteUrl) {
        if (props.getPublicUrl() == null || props.getPublicUrl().isBlank()) {
            // nginx serves the API under /api/ (see website-container/nginx.conf)
            props.setPublicUrl(stripSlash(websiteUrl) + "/api" + PATH);
        }
        props.setPublicUrl(stripSlash(props.getPublicUrl()));
        log.info("Minecraft auth enabled at {} (real accounts {})", props.getPublicUrl(),
                props.isMojangFallback() ? "allowed" : "refused");
    }

    /**
     * Game clients and launchers: no CSRF, no cookies, no sessions. Checked before the website's
     * chain, and matches nothing outside /minecraft.
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain minecraftSecurityFilterChain(HttpSecurity http) throws Exception {
        return http.securityMatcher(PATH, PATH + "/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                .build();
    }

    /** CREATE TABLE IF NOT EXISTS for the mc_* tables (minecraft.auth.create-tables=false to skip). */
    @Bean
    public Object minecraftSchema(DataSource dataSource,
                                  @Value("${minecraft.auth.create-tables:true}") boolean createTables) {
        if (createTables) new ResourceDatabasePopulator(new ClassPathResource("minecraft/schema.sql")).execute(dataSource);
        return "minecraftSchema";
    }

    private static String stripSlash(String s) {
        String r = s == null ? "" : s.trim();
        while (r.endsWith("/")) r = r.substring(0, r.length() - 1);
        return r;
    }
}
