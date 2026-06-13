package com.staynest.api.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    private final boolean success;
    private final String message;
    private final List<FieldValidationError> errors;
    private final Instant timestamp;
    private final String path;

    public static ApiErrorResponse validationError(String path, List<FieldValidationError> errors) {
        return ApiErrorResponse.builder()
                .success(false)
                .message("Validation failed")
                .errors(errors)
                .timestamp(Instant.now())
                .path(path)
                .build();
    }

    public static ApiErrorResponse of(String message, String path) {
        return ApiErrorResponse.builder()
                .success(false)
                .message(message)
                .timestamp(Instant.now())
                .path(path)
                .build();
    }

    @Getter
    @Builder
    public static class FieldValidationError {
        private final String field;
        private final String message;
    }
}
