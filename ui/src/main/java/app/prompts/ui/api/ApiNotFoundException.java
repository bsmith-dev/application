package app.prompts.ui.api;

/** Maps HTTP 404 responses. */
public final class ApiNotFoundException extends ApiException {
    public ApiNotFoundException(String message) {
        super(message, 404);
    }
}
