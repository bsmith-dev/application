package app.prompts.ui.web;

import app.prompts.ui.api.ApiConflictException;
import app.prompts.ui.api.ApiForbiddenException;
import app.prompts.ui.api.ApiNotFoundException;
import app.prompts.ui.api.ApiUnauthorizedException;
import app.prompts.ui.api.ApiUnavailableException;
import app.prompts.ui.api.ApiUnknownException;
import app.prompts.ui.api.ApiValidationException;
import app.prompts.ui.security.CurrentApiSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.ResourceAccessException;

/**
 * Central translation from API errors to UI pages. Every mutation and lookup in this app is a
 * direct pass-through to the prompt_db API, so its HTTP status codes are the single source of
 * truth for what went wrong.
 */
@ControllerAdvice
public class UiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(UiExceptionHandler.class);

    private final CurrentApiSession currentApiSession;

    public UiExceptionHandler(CurrentApiSession currentApiSession) {
        this.currentApiSession = currentApiSession;
    }

    @ExceptionHandler(ApiUnauthorizedException.class)
    String unauthorized() {
        currentApiSession.clear();
        return "redirect:/login?expired";
    }

    @ExceptionHandler(ApiForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    String forbidden(ApiForbiddenException ex, Model model) {
        model.addAttribute("detail", ex.getMessage());
        return "error/403";
    }

    @ExceptionHandler(ApiNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String notFound(ApiNotFoundException ex, Model model) {
        model.addAttribute("detail", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(ApiConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    String conflict(ApiConflictException ex, Model model) {
        model.addAttribute("detail", ex.getMessage());
        return "error/409";
    }

    @ExceptionHandler(ApiValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String validation(ApiValidationException ex, Model model) {
        model.addAttribute("detail", ex.getMessage());
        return "error/400";
    }

    @ExceptionHandler({ApiUnavailableException.class, ApiUnknownException.class})
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    String unavailable(Exception ex, Model model) {
        model.addAttribute("detail", ex.getMessage());
        return "error/5xx";
    }

    /**
     * A connection failure (refused/timeout/DNS) never reaches {@code ApiClientConfig}'s HTTP
     * status handler because no HTTP response was ever received - RestClient wraps these as
     * {@link ResourceAccessException} instead. Treat it the same as an unavailable API.
     */
    @ExceptionHandler(ResourceAccessException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    String connectionFailed(ResourceAccessException ex, Model model) {
        model.addAttribute("detail", "Could not reach the API: " + ex.getMessage());
        return "error/5xx";
    }

    /**
     * Last-resort safety net so an unanticipated exception still renders the app's own error
     * page instead of Spring Boot's default Whitelabel/JSON error response.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String fallback(Exception ex, Model model) {
        log.error("Unhandled exception while rendering a page", ex);
        model.addAttribute("detail", "An unexpected error occurred. Please try again.");
        return "error/5xx";
    }
}
