package io.github.engineeringov.website.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class StoreException extends RuntimeException{
    public StoreException(String message) {
        super(message);
    }
    public StoreException() {
        super();
    }
}
