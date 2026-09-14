package com.example.promptdb.api;

/** Maps HTTP 400/422 responses. */
public final class ApiValidationException extends ApiException {
    public ApiValidationException(String message, int statusCode) {
        super(message, statusCode);
    }
}
