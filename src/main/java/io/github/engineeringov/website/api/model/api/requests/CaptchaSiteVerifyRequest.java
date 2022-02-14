package io.github.engineeringov.website.api.model.api.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaptchaSiteVerifyRequest {
    String secret;
    String response;
    String remoteIp;
}
