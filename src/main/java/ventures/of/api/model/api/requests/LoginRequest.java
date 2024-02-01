package ventures.of.api.model.api.requests;

import lombok.Data;

@Data
public class LoginRequest {
    //GenericRequestData
    String captchaToken;
    String username;
    char[] password;
}
