package app.prompts.presentation.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import app.prompts.application.dto.GroupMembershipResult;
import app.prompts.application.dto.GroupResult;
import app.prompts.application.port.ManageGroupUseCase;
import app.prompts.application.service.AccessDeniedException;
import app.prompts.application.service.GroupNotFoundException;
import app.prompts.domain.model.Role;
import app.prompts.infrastructure.security.JwtAuthFilter;
import app.prompts.infrastructure.security.JwtProperties;
import app.prompts.infrastructure.security.JwtService;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc tests for {@link GroupController}.
 *
 * Uses {@code standaloneSetup} with {@link JwtAuthFilter} added to the filter chain
 * so that Bearer-token authentication is exercised end-to-end at the filter level.
 */
@ExtendWith(MockitoExtension.class)
class GroupControllerTest {

    @Mock
    private ManageGroupUseCase groupUseCase;

    private MockMvc mockMvc;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(new JwtProperties(WebMvcTestSupport.TEST_SECRET, WebMvcTestSupport.TEST_EXPIRY));
        mockMvc = MockMvcBuilders
                .standaloneSetup(new GroupController(groupUseCase))
                .setControllerAdvice(new PromptsExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .addFilter(new JwtAuthFilter(jwtService))
                .build();
        SecurityContextHolder.clearContext();
    }

    private String bearerToken(UUID memberId, UUID orgId, String username, List<String> authorities) {
        return "Bearer " + jwtService.generateToken(memberId, orgId, username, authorities);
    }

    // ── GET /api/groups ──────────────────────────────────────────────────────

    @Test
    void listMyGroups_authenticated_returns200() throws Exception {
        UUID memberId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        GroupResult group = new GroupResult(UUID.randomUUID(), orgId, "Support");
        when(groupUseCase.listGroupsForMember(memberId, orgId)).thenReturn(List.of(group));

        mockMvc.perform(get("/api/groups")
                        .header("Authorization", bearerToken(memberId, orgId, "alice", List.of())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Support"));
    }

    @Test
    void listMyGroups_returnsEmptyListWhenNoGroups() throws Exception {
        UUID memberId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        when(groupUseCase.listGroupsForMember(memberId, orgId)).thenReturn(List.of());

        mockMvc.perform(get("/api/groups")
                        .header("Authorization", bearerToken(memberId, orgId, "alice", List.of())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── POST /api/groups ─────────────────────────────────────────────────────

    @Test
    void createGroup_withValidPayload_returns201() throws Exception {
        UUID memberId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        GroupResult result = new GroupResult(UUID.randomUUID(), orgId, "Ops");
        when(groupUseCase.createGroup(any())).thenReturn(result);

        mockMvc.perform(post("/api/groups")
                        .header("Authorization", bearerToken(memberId, orgId, "alice", List.of("ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ops"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Ops"));
    }

    @Test
    void createGroup_blankName_returns400() throws Exception {
        UUID memberId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        mockMvc.perform(post("/api/groups")
                        .header("Authorization", bearerToken(memberId, orgId, "alice", List.of("ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void createGroup_nameTooLong_returns400() throws Exception {
        UUID memberId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        String tooLong = "x".repeat(101);

        mockMvc.perform(post("/api/groups")
                        .header("Authorization", bearerToken(memberId, orgId, "alice", List.of("ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + tooLong + "\"}"))
                .andExpect(status().isBadRequest());
    }

    // ── GET /api/groups/{id}/members ─────────────────────────────────────────

    @Test
    void listMembers_authenticated_returns200() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        GroupMembershipResult r = new GroupMembershipResult(groupId, memberId, Role.MEMBER);
        when(groupUseCase.listMembers(groupId, memberId, orgId)).thenReturn(List.of(r));

        mockMvc.perform(get("/api/groups/{groupId}/members", groupId)
                        .header("Authorization", bearerToken(memberId, orgId, "alice", List.of())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("MEMBER"));
    }

    @Test
    void listMembers_serviceThrowsAccessDenied_returns403() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        when(groupUseCase.listMembers(any(), any(), any()))
                .thenThrow(new AccessDeniedException("not a member"));

        mockMvc.perform(get("/api/groups/{groupId}/members", groupId)
                        .header("Authorization", bearerToken(memberId, orgId, "alice", List.of())))
                .andExpect(status().isForbidden());
    }

    @Test
    void listMembers_serviceThrowsGroupNotFound_returns404() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        when(groupUseCase.listMembers(any(), any(), any()))
                .thenThrow(new GroupNotFoundException("group not found"));

        mockMvc.perform(get("/api/groups/{groupId}/members", groupId)
                        .header("Authorization", bearerToken(memberId, orgId, "alice", List.of())))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/groups/{id}/members ────────────────────────────────────────

    @Test
    void addMember_nullMemberId_returns400() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        mockMvc.perform(post("/api/groups/{groupId}/members", groupId)
                        .header("Authorization", bearerToken(requesterId, orgId, "alice", List.of()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"memberId":null,"role":"MEMBER"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addMember_invalidGroupId_returns400() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        mockMvc.perform(post("/api/groups/{groupId}/members", "not-a-uuid")
                        .header("Authorization", bearerToken(requesterId, orgId, "alice", List.of()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"memberId":"00000000-0000-0000-0000-000000000001","role":"MEMBER"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Parameter"));
    }
}
