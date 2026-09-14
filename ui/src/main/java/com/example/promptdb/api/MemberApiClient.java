package com.example.promptdb.api;

import com.example.promptdb.api.dto.MemberResponse;
import com.example.promptdb.api.dto.RegisterMemberRequest;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Wraps /api/members and /api/members/me. */
@Component
public class MemberApiClient {

    private final RestClient apiRestClient;

    public MemberApiClient(RestClient apiRestClient) {
        this.apiRestClient = apiRestClient;
    }

    /** Self-registration; reachable without a session per the API's access rules. */
    public MemberResponse register(RegisterMemberRequest request) {
        return apiRestClient.post()
                .uri("/api/members")
                .body(request)
                .retrieve()
                .body(MemberResponse.class);
    }

    public List<MemberResponse> listMembers() {
        return apiRestClient.get()
                .uri("/api/members")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public MemberResponse getCurrentMember() {
        return apiRestClient.get()
                .uri("/api/members/me")
                .retrieve()
                .body(MemberResponse.class);
    }
}
