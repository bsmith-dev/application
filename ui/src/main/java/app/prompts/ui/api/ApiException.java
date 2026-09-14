package app.prompts.ui.api;

/** Base type for every error translated from an API HTTP response. */
public sealed class ApiException extends RuntimeException
        permits ApiValidationException, ApiUnauthorizedException, ApiForbiddenException,
        ApiNotFoundException, ApiConflictException, ApiUnavailableException, ApiUnknownException {

    private final int statusCode;

    protected ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}
