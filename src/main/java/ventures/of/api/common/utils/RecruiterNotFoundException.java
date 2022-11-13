package ventures.of.api.common.utils;

public class RecruiterNotFoundException extends Exception {
    // Parameterless Constructor
    public RecruiterNotFoundException() {}

    // Constructor that accepts a message
    public RecruiterNotFoundException(String message) {
        super(message);
    }

    public RecruiterNotFoundException(Throwable cause) {
        super (cause);
    }

    public RecruiterNotFoundException(String message, Throwable cause) {
        super (message, cause);
    }
}