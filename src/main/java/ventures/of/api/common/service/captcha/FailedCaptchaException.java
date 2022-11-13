package ventures.of.api.common.service.captcha;

public class FailedCaptchaException extends Exception {
    // Parameterless Constructor
    public FailedCaptchaException() {}

    // Constructor that accepts a message
    public FailedCaptchaException(String message) {
        super(message);
    }

    public FailedCaptchaException (Throwable cause) {
        super (cause);
    }

    public FailedCaptchaException (String message, Throwable cause) {
        super (message, cause);
    }
}