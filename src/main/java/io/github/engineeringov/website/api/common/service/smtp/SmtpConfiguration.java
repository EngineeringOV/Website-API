package io.github.engineeringov.website.api.common.service.smtp;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.mail")
public class SmtpConfiguration {
    private String host;
    private String username;
    private String password;
    private Integer port;
}
