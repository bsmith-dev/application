package app.prompts.ui.api;

import app.prompts.ui.api.dto.AddMemberRequest;
import app.prompts.ui.api.dto.ChangeMemberRoleRequest;
import app.prompts.ui.api.dto.GroupMembershipResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Wraps /api/groups/{groupId}/members and /api/groups/{groupId}/members/{memberId}. */
@Component
public class GroupMembershipApiClient {

    private final RestClient apiRestClient;

    public GroupMembershipApiClient(@Qualifier("apiRestClient") RestClient apiRestClient) {
        this.apiRestClient = apiRestClient;
    }

    public List<GroupMembershipResponse> listGroupMembers(UUID groupId) {
        return apiRestClient.get()
                .uri("/api/groups/{groupId}/members", groupId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public GroupMembershipResponse addMember(UUID groupId, AddMemberRequest request) {
        return apiRestClient.post()
                .uri("/api/groups/{groupId}/members", groupId)
                .body(request)
                .retrieve()
                .body(GroupMembershipResponse.class);
    }

    public GroupMembershipResponse changeMemberRole(UUID groupId, UUID memberId, ChangeMemberRoleRequest request) {
        return apiRestClient.put()
                .uri("/api/groups/{groupId}/members/{memberId}", groupId, memberId)
                .body(request)
                .retrieve()
                .body(GroupMembershipResponse.class);
    }

    public void removeMember(UUID groupId, UUID memberId) {
        apiRestClient.delete()
                .uri("/api/groups/{groupId}/members/{memberId}", groupId, memberId)
                .retrieve()
                .toBodilessEntity();
    }
}
