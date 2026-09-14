package app.prompts.ui.api;

import app.prompts.ui.api.dto.LoginRequest;
import app.prompts.ui.api.dto.LoginResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** POST /api/auth/login - called before a session token exists, so the bearer interceptor is a no-op. */
@Component
public class AuthApiClient {

    private final RestClient apiRestClient;

    public AuthApiClient(RestClient apiRestClient) {
        this.apiRestClient = apiRestClient;
    }

    public LoginResponse login(LoginRequest request) {
        return apiRestClient.post()
                .uri("/api/auth/login")
                .body(request)
                .retrieve()
                .body(LoginResponse.class);
    }
}
