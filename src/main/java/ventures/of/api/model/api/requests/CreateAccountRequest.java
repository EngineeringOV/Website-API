package ventures.of.api.model.api.requests;

import lombok.Data;

@Data
public class CreateAccountRequest {
    //GenericRequestData
    String mode;
    String captchaToken;

    String email;
    String username;
    char[] passwordBase64;
    String recruiterName;
}
