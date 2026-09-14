package com.example.promptdb.api;

/** Maps HTTP 401 responses - the session token is missing, invalid, or expired. */
public final class ApiUnauthorizedException extends ApiException {
    public ApiUnauthorizedException(String message) {
        super(message, 401);
    }
}
