package ventures.of.api.model.api.requests;

import lombok.Data;

@Data
public class ResetAccountRequest {
    //GenericRequestData
    String mode;
    String captchaToken;

    String email;
}
