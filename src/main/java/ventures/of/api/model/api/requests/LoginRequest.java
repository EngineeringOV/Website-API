package ventures.of.api.model.api.requests;

import lombok.Data;

@Data
public class LoginRequest {
    //GenericRequestData
    String mode;
    String captchaToken;

    String username;
    char[] password;
}
