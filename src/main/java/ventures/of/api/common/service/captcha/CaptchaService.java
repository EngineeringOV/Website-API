package ventures.of.api.common.service.captcha;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ventures.of.api.common.service.RestService;
import ventures.of.api.model.api.responses.CaptchaSiteVerifyResponse;

@Service
@Log4j2
public class CaptchaService {

    @Autowired
    private RestService restService;

    @Value("${google.captcha.private}")
    private String captchaPrivateKey;
    @Value("${google.captcha.verifyUrl}")
    private String siteVerifyUrl;

    public boolean verifyCaptcha(String captchaToken, String ipAddress) throws GenericCaptchaException {
        CaptchaSiteVerifyResponse response = null;
        try {
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

            log.info("Sent request to google to verify captcha and response was {}", response);
            if (captchaPrivateKey.length() > 10 && !response.getSuccess()) {
                log.info("captchaPrivateKey is starts with {}", captchaPrivateKey.substring(0, 4));
            }
        }
        catch (Exception e) {
            throw new GenericCaptchaException(e);
        }

        return response.getSuccess();
    }
    public void throwOnFailedCaptcha(String captchaToken, String realIp) throws FailedCaptchaException, GenericCaptchaException {
            if (!verifyCaptcha(captchaToken, realIp)) {
                //Captcha failed but maybe don't tell the client that
                log.debug("Failed to pass Captcha");
                throw new FailedCaptchaException("Failed to pass Captcha");
            }
        }

}
