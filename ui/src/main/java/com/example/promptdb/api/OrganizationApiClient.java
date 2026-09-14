package com.example.promptdb.api;

import com.example.promptdb.api.dto.CreateOrganizationRequest;
import com.example.promptdb.api.dto.OrganizationResponse;
import com.example.promptdb.api.dto.RenameOrganizationRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Wraps /api/organizations and /api/organizations/{organizationId}.
 * Creation is reachable pre-login (an organization must exist before a member can register into
 * it); list/rename/delete require a session and are gated to ADMIN members in the UI.
 */
@Component
public class OrganizationApiClient {

    private final RestClient apiRestClient;

    public OrganizationApiClient(RestClient apiRestClient) {
        this.apiRestClient = apiRestClient;
    }

    public List<OrganizationResponse> listOrganizations() {
        return apiRestClient.get()
                .uri("/api/organizations")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public OrganizationResponse createOrganization(CreateOrganizationRequest request) {
        return apiRestClient.post()
                .uri("/api/organizations")
                .body(request)
                .retrieve()
                .body(OrganizationResponse.class);
    }

    public OrganizationResponse renameOrganization(UUID organizationId, RenameOrganizationRequest request) {
        return apiRestClient.put()
                .uri("/api/organizations/{organizationId}", organizationId)
                .body(request)
                .retrieve()
                .body(OrganizationResponse.class);
    }

    public void deleteOrganization(UUID organizationId) {
        apiRestClient.delete()
                .uri("/api/organizations/{organizationId}", organizationId)
                .retrieve()
                .toBodilessEntity();
    }
}
