package ventures.of.api.common.service.captcha;

public class GenericCaptchaException extends Exception {
    // Parameterless Constructor
    public GenericCaptchaException() {}

    // Constructor that accepts a message
    public GenericCaptchaException(String message) {
        super(message);
    }

    public GenericCaptchaException (Throwable cause) {
        super (cause);
    }

    public GenericCaptchaException (String message, Throwable cause) {
        super (message, cause);
    }
}