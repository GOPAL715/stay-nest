package com.staynest.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApplicationException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public ApplicationException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public ApplicationException(String message, HttpStatus status) {
        this(message, status, status.name());
    }
}
