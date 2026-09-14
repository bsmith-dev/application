package com.example.promptdb.config;

import com.example.promptdb.api.ApiConflictException;
import com.example.promptdb.api.ApiForbiddenException;
import com.example.promptdb.api.ApiNotFoundException;
import com.example.promptdb.api.ApiUnauthorizedException;
import com.example.promptdb.api.ApiUnavailableException;
import com.example.promptdb.api.ApiUnknownException;
import com.example.promptdb.api.ApiValidationException;
import com.example.promptdb.security.ApiBearerTokenInterceptor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

/**
 * Builds the single RestClient used to reach the configurable {@code app.api.base-url}.
 * {@link ApiBearerTokenInterceptor} attaches {@code Authorization: Bearer <token>} whenever the
 * current HTTP session already holds one, and is a no-op otherwise - which lets the same client
 * serve both anonymous calls (login, and the org/member bootstrap endpoints the spec marks as
 * unauthenticated) and authenticated calls. Every non-2xx response is translated here into a
 * typed {@link com.example.promptdb.api.ApiException} so API client classes can just call
 * {@code retrieve().body(...)} and let the UI error handler render the right page.
 */
@Configuration
@EnableConfigurationProperties(ApiProperties.class)
public class ApiClientConfig {

    @Bean
    ClientHttpRequestFactory apiRequestFactory(ApiProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.getConnectTimeout().toMillis());
        factory.setReadTimeout((int) properties.getReadTimeout().toMillis());
        return factory;
    }

    @Bean
    RestClient apiRestClient(
            ApiProperties properties,
            ClientHttpRequestFactory apiRequestFactory,
            ApiBearerTokenInterceptor bearerTokenInterceptor
    ) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(apiRequestFactory)
                .requestInterceptor(bearerTokenInterceptor)
                .defaultStatusHandler(HttpStatusCode::isError, ApiClientConfig::raise)
                .build();
    }

    private static void raise(HttpRequest request, ClientHttpResponse response) throws IOException {
        int code = response.getStatusCode().value();
        String body;
        try {
            body = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            body = "";
        }

        String message = body.isBlank()
                ? "The API responded with HTTP " + code + " for " + request.getMethod() + " " + request.getURI()
                : body;

        switch (code) {
            case 400, 422 -> throw new ApiValidationException(message, code);
            case 401 -> throw new ApiUnauthorizedException(message);
            case 403 -> throw new ApiForbiddenException(message);
            case 404 -> throw new ApiNotFoundException(message);
            case 409 -> throw new ApiConflictException(message);
            default -> {
                if (code >= 500) {
                    throw new ApiUnavailableException(message);
                }
                throw new ApiUnknownException(message, code);
            }
        }
    }
}
