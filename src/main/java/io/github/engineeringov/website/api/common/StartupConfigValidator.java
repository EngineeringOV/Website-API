package io.github.engineeringov.website.api.common;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Log4j2
public class StartupConfigValidator {

    @Value("${spring.datasource.password:}")
    private String datasourcePassword;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${google.captcha.private:}")
    private String captchaPrivateKey;

    @Value("${api.customization.website.url:}")
    private String websiteUrl;

    @Value("${api.devMode:false}")
    private boolean devMode;

    @EventListener(ApplicationReadyEvent.class)
    public void validateRequiredProperties() {
        List<String> missingProperties = new ArrayList<>();

        if (datasourcePassword.isBlank() || datasourcePassword.equals("!!REPLACE-ME!!")) {
            missingProperties.add("spring.datasource.password");
        }
        if (!devMode && mailPassword.isBlank()) {
            missingProperties.add("spring.mail.password");
        }
        if (!devMode && captchaPrivateKey.isBlank()) {
            missingProperties.add("google.captcha.private");
        }
        if (!devMode && websiteUrl.isBlank()) {
            missingProperties.add("api.customization.website.url");
        }

        if (!missingProperties.isEmpty()) {
            throw new IllegalStateException(
                    "The following required properties are not configured: " + String.join(", ", missingProperties)
                            + ". Please set them in your application properties before starting the application."
            );
        }

        log.info("All required configuration properties are set.");
    }
}

