package io.github.engineeringov.website.api.model.api.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateAccountRequest {
    //GenericRequestData
    String captchaToken;
    String email;
    String username;
    char[] passwordBase64;
    String recruiterName;
}
