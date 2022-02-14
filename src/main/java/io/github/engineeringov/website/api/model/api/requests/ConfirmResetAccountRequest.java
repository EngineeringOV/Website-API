package io.github.engineeringov.website.api.model.api.requests;

import lombok.Data;

@Data
public class ConfirmResetAccountRequest {
    //GenericRequestData
    String uuid;
    String email;
    char[] passwordBase64;
}
