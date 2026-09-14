package app.prompts.ui.api;

/** Maps connection failures, timeouts, and HTTP 5xx responses. */
public final class ApiUnavailableException extends ApiException {
    public ApiUnavailableException(String message) {
        super(message, 503);
    }

    public ApiUnavailableException(String message, Throwable cause) {
        super(message, 503);
        initCause(cause);
    }
}
