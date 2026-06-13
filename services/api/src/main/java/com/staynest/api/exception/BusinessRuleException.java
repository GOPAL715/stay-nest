package com.staynest.api.exception;

import org.springframework.http.HttpStatus;

public class BusinessRuleException extends ApplicationException {
    public BusinessRuleException(String message) {
        super(message, HttpStatus.CONFLICT, "BUSINESS_RULE_VIOLATION");
    }
}
