package com.example.promptdb.api;

/** Maps HTTP 403 responses - the API rejected the operation for the current member. */
public final class ApiForbiddenException extends ApiException {
    public ApiForbiddenException(String message) {
        super(message, 403);
    }
}
