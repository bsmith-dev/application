package com.example.promptdb.security;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

/**
 * Attaches {@code Authorization: Bearer <token>} to every outgoing call made through the
 * authenticated RestClient. The token is never exposed to the browser - it lives only in the
 * server-side HTTP session (see {@link CurrentApiSession}).
 */
@Component
public class ApiBearerTokenInterceptor implements ClientHttpRequestInterceptor {

    private final CurrentApiSession currentApiSession;

    public ApiBearerTokenInterceptor(CurrentApiSession currentApiSession) {
        this.currentApiSession = currentApiSession;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        currentApiSession.accessToken().ifPresent(token -> request.getHeaders().setBearerAuth(token));
        return execution.execute(request, body);
    }
}
