package com.example.promptdb.api;

/** Fallback for any other non-2xx status code. */
public final class ApiUnknownException extends ApiException {
    public ApiUnknownException(String message, int statusCode) {
        super(message, statusCode);
    }
}
