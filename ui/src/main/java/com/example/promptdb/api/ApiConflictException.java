package com.example.promptdb.api;

/** Maps HTTP 409 responses, e.g. deleting a non-empty organization. */
public final class ApiConflictException extends ApiException {
    public ApiConflictException(String message) {
        super(message, 409);
    }
}
