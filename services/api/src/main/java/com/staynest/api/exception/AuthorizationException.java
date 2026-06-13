package com.staynest.api.exception;

import org.springframework.http.HttpStatus;

public class AuthorizationException extends ApplicationException {
    public AuthorizationException(String message) {
        super(message, HttpStatus.FORBIDDEN, "ACCESS_DENIED");
    }
}
