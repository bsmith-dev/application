package com.example.promptdb.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Redirects to /login for any route that requires a session, unless the current session already
 * holds a bearer token. Public bootstrap routes (login, organization/member self-registration,
 * static assets, error pages) are excluded via {@code WebConfig}'s interceptor registration.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final CurrentApiSession currentApiSession;

    public AuthInterceptor(CurrentApiSession currentApiSession) {
        this.currentApiSession = currentApiSession;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (currentApiSession.isAuthenticated()) {
            return true;
        }

        String target = UriComponentsBuilder.fromPath("/login")
                .queryParam("redirectTo", request.getRequestURI())
                .toUriString();
        response.sendRedirect(target);
        return false;
    }
}
