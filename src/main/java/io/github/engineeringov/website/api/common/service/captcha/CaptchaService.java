package io.github.engineeringov.website.api.common.service.captcha;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import io.github.engineeringov.website.api.common.service.RestService;
import io.github.engineeringov.website.api.model.api.responses.CaptchaSiteVerifyResponse;

@Service
@Log4j2
public class CaptchaService {

    @Autowired
    private RestService restService;

    @Value("${google.captcha.private}")
    private String captchaPrivateKey;
    @Value("${google.captcha.verifyUrl}")
    private String siteVerifyUrl;
    @Value("${api.devMode}")
    private boolean devMode;

    public boolean captchaSuccessful(String captchaToken, String ipAddress) {
        if (devMode) {
            log.debug("DevMode is enabled, skipping captcha verification");
            return true;
        }

        CaptchaSiteVerifyResponse response;
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("secret", captchaPrivateKey);
        body.add("response", captchaToken);
        body.add("remoteip", ipAddress);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, null);
        response = restService.restCallToURL(
                siteVerifyUrl,
                HttpMethod.POST,
                entity,
                CaptchaSiteVerifyResponse.class,
                false);

        log.debug("Sent request to google to verify captcha and response was {}", response);
        if (captchaPrivateKey.length() > 10 && !response.getSuccess()) {
            log.debug("captchaPrivateKey is starts with {}", captchaPrivateKey.substring(0, 4));
        }

        return response.getSuccess();
    }

}
