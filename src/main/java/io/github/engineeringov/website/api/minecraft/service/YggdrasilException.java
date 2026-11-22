package io.github.engineeringov.website.api.minecraft.service;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** An error in the shape Minecraft's auth library expects: {error, errorMessage}. */
@Getter
public class YggdrasilException extends RuntimeException {
    private final HttpStatus status;
    private final String error;

    public YggdrasilException(HttpStatus status, String error, String message) {
        super(message);
        this.status = status;
        this.error = error;
    }

    public static YggdrasilException forbidden(String message) {
        return new YggdrasilException(HttpStatus.FORBIDDEN, "ForbiddenOperationException", message);
    }

    public static YggdrasilException badRequest(String message) {
        return new YggdrasilException(HttpStatus.BAD_REQUEST, "IllegalArgumentException", message);
    }

    public static YggdrasilException conflict(String message) {
        return new YggdrasilException(HttpStatus.CONFLICT, "NameUnavailableException", message);
    }

    public static YggdrasilException tooMany() {
        return new YggdrasilException(HttpStatus.TOO_MANY_REQUESTS, "TooManyRequestsException",
                "Too many failed logins, try again in a few minutes.");
    }
}
