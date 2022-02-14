package io.github.engineeringov.website.api.model.api.requests;

import lombok.Data;

@Data
public class ResetAccountRequest {
    //GenericRequestData
    String captchaToken;
    String email;
}
