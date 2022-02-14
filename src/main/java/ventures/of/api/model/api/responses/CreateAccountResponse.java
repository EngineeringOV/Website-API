package ventures.of.api.model.api.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ventures.of.api.model.ResponseStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAccountResponse {
    ResponseStatus status;
    String message;
    String errorCode;
}
