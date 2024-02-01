package ventures.of.api.model.api.requests;

import lombok.Data;

@Data
public class ResetAccountRequest {
    //GenericRequestData
    String captchaToken;
    String email;
}
