package com.freelance.freelance_platform.shared.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        String code,
        String message,
        List<FieldError> errors,
        Instant timestamp
) {
    public record FieldError(String field, String message) {}

    public static ApiError of(String code, String message) {
        return new ApiError(code, message, null, Instant.now());
    }

    public static ApiError of(String code, String message, List<FieldError> errors) {
        return new ApiError(code, message, errors, Instant.now());
    }
}