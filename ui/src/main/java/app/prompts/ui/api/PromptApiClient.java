package app.prompts.ui.api;

import app.prompts.ui.api.dto.CreatePromptRequest;
import app.prompts.ui.api.dto.PromptResponse;
import app.prompts.ui.api.dto.UpdatePromptRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Wraps /api/groups/{groupId}/prompts and /api/groups/{groupId}/prompts/{promptId}. */
@Component
public class PromptApiClient {

    private final RestClient apiRestClient;

    public PromptApiClient(@Qualifier("apiRestClient") RestClient apiRestClient) {
        this.apiRestClient = apiRestClient;
    }

    public List<PromptResponse> listPrompts(UUID groupId) {
        return apiRestClient.get()
                .uri("/api/groups/{groupId}/prompts", groupId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public PromptResponse createPrompt(UUID groupId, CreatePromptRequest request) {
        return apiRestClient.post()
                .uri("/api/groups/{groupId}/prompts", groupId)
                .body(request)
                .retrieve()
                .body(PromptResponse.class);
    }

    public PromptResponse getPrompt(UUID groupId, UUID promptId) {
        return apiRestClient.get()
                .uri("/api/groups/{groupId}/prompts/{promptId}", groupId, promptId)
                .retrieve()
                .body(PromptResponse.class);
    }

    public PromptResponse updatePrompt(UUID groupId, UUID promptId, UpdatePromptRequest request) {
        return apiRestClient.put()
                .uri("/api/groups/{groupId}/prompts/{promptId}", groupId, promptId)
                .body(request)
                .retrieve()
                .body(PromptResponse.class);
    }

    public void deletePrompt(UUID groupId, UUID promptId) {
        apiRestClient.delete()
                .uri("/api/groups/{groupId}/prompts/{promptId}", groupId, promptId)
                .retrieve()
                .toBodilessEntity();
    }
}
