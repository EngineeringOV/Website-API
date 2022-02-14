package io.github.engineeringov.website.api.model.api.requests;

import lombok.Data;

@Data
public class CreateAccountRequest {
    //GenericRequestData
    String captchaToken;
    String email;
    String username;
    char[] passwordBase64;
    String recruiterName;
}
