package com.staynest.api.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final Instant timestamp;
    private final String path;

    public static <T> ApiResponse<T> success(T data, String message, String path) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .path(path)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, String path) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(Instant.now())
                .path(path)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(Instant.now())
                .path(path)
                .build();
    }
}
