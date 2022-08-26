package ventures.of.api.model.api.requests;

import lombok.Data;

@Data
public class ConfirmResetAccountRequest {
    //GenericRequestData
    String mode;

    String uuid;
    String email;
    char[] passwordBase64;
}
