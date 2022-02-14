package ventures.of.api.model;

public enum CreateAccountErrorCode {
    SUCCESS(0, "ACCOUNT WAS CREATED"),
    GENERIC_FAILURE(1, "Failed to create account"),
    MISSING_FIELD(2, "MISSING FIELD"),
    ALREADY_IN_USE(3, "Field already in use"),
    CAPTCHA_FAILURE(4, "Captcha failed");

    int code;
    String message;

    CreateAccountErrorCode(int code, String messages) {
        this.code = code;
        this.message = messages;
    }
}
