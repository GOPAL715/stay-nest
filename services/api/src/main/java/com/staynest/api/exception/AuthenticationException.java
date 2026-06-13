package com.staynest.api.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends ApplicationException {
    public AuthenticationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED");
    }
}
