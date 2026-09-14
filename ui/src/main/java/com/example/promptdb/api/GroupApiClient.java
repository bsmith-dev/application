package com.example.promptdb.api;

import com.example.promptdb.api.dto.CreateGroupRequest;
import com.example.promptdb.api.dto.GroupResponse;
import com.example.promptdb.api.dto.RenameGroupRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Wraps /api/groups and /api/groups/{groupId}. */
@Component
public class GroupApiClient {

    private final RestClient apiRestClient;

    public GroupApiClient(@Qualifier("apiRestClient") RestClient apiRestClient) {
        this.apiRestClient = apiRestClient;
    }

    public List<GroupResponse> listGroups() {
        return apiRestClient.get()
                .uri("/api/groups")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public GroupResponse createGroup(CreateGroupRequest request) {
        return apiRestClient.post()
                .uri("/api/groups")
                .body(request)
                .retrieve()
                .body(GroupResponse.class);
    }

    public GroupResponse getGroup(UUID groupId) {
        return apiRestClient.get()
                .uri("/api/groups/{groupId}", groupId)
                .retrieve()
                .body(GroupResponse.class);
    }

    public GroupResponse renameGroup(UUID groupId, RenameGroupRequest request) {
        return apiRestClient.put()
                .uri("/api/groups/{groupId}", groupId)
                .body(request)
                .retrieve()
                .body(GroupResponse.class);
    }

    public void deleteGroup(UUID groupId) {
        apiRestClient.delete()
                .uri("/api/groups/{groupId}", groupId)
                .retrieve()
                .toBodilessEntity();
    }
}
