package app.prompts.presentation.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import app.prompts.application.dto.MemberResult;
import app.prompts.application.port.MemberQueryUseCase;
import app.prompts.application.port.RegisterMemberUseCase;
import app.prompts.infrastructure.security.JwtAuthFilter;
import app.prompts.infrastructure.security.JwtProperties;
import app.prompts.infrastructure.security.JwtService;
import app.prompts.domain.model.Role;
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

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @Mock
    private RegisterMemberUseCase registerMemberUseCase;
    @Mock
    private MemberQueryUseCase memberQueryUseCase;

    private MockMvc mockMvc;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(new JwtProperties(WebMvcTestSupport.TEST_SECRET, WebMvcTestSupport.TEST_EXPIRY));
        mockMvc = MockMvcBuilders
                .standaloneSetup(new MemberController(registerMemberUseCase, memberQueryUseCase))
                .setControllerAdvice(new PromptsExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .addFilter(new JwtAuthFilter(jwtService))
                .build();
        SecurityContextHolder.clearContext();
    }

    private static final UUID ORG_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private String bearerToken(UUID memberId, UUID orgId) {
        return "Bearer " + jwtService.generateToken(memberId, orgId, "alice", List.of());
    }

    // ── POST /api/members ────────────────────────────────────────────────────

    @DisplayName("Register valid request returns 201 with body")
    @Test
    void register_validRequest_returns201WithBody() throws Exception {
        UUID id = UUID.randomUUID();
        when(registerMemberUseCase.registerMember(any()))
                .thenReturn(new MemberResult(id, ORG_ID, "alice", "alice@example.com", null));

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","password":"password123","email":"alice@example.com","organizationId":"%s"}
                                """.formatted(ORG_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.memberId").value(id.toString()))
                .andExpect(jsonPath("$.organizationId").value(ORG_ID.toString()))
                .andExpect(jsonPath("$.role").doesNotExist());
    }

    @DisplayName("Register blank username returns 400 with errors")
    @Test
    void register_blankUsername_returns400WithErrors() throws Exception {
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"","password":"password123","email":"alice@example.com","organizationId":"%s"}
                                """.formatted(ORG_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").exists());
    }

    @DisplayName("Register too short username returns 400")
    @Test
    void register_tooShortUsername_returns400() throws Exception {
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"ab","password":"password123","email":"alice@example.com","organizationId":"%s"}
                                """.formatted(ORG_ID)))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Register too short password returns 400")
    @Test
    void register_tooShortPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","password":"short","email":"alice@example.com","organizationId":"%s"}
                                """.formatted(ORG_ID)))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Register invalid email returns 400")
    @Test
    void register_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","password":"password123","email":"not-an-email","organizationId":"%s"}
                                """.formatted(ORG_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @DisplayName("Register missing organization ID returns 400")
    @Test
    void register_missingOrganizationId_returns400() throws Exception {
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","password":"password123","email":"alice@example.com"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Register missing body returns 400")
    @Test
    void register_missingBody_returns400() throws Exception {
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ── GET /api/members ─────────────────────────────────────────────────────

    @DisplayName("Get current member authenticated returns profile with role")
    @Test
    void getCurrentMember_authenticated_returnsProfileWithRole() throws Exception {
        UUID memberId = UUID.randomUUID();
        when(memberQueryUseCase.getMember(memberId))
                .thenReturn(new MemberResult(memberId, ORG_ID, "alice", "alice@example.com", Role.GROUP_LEAD));

        mockMvc.perform(get("/api/members/me")
                        .header("Authorization", bearerToken(memberId, ORG_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(memberId.toString()))
                .andExpect(jsonPath("$.organizationId").value(ORG_ID.toString()))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.role").value("GROUP_LEAD"));
    }

    @DisplayName("List members authenticated returns org scoped list")
    @Test
    void listMembers_authenticated_returnsOrgScopedList() throws Exception {
        UUID memberId = UUID.randomUUID();
        UUID m2Id = UUID.randomUUID();
        when(memberQueryUseCase.listMembers(ORG_ID))
                .thenReturn(List.of(
                        new MemberResult(memberId, ORG_ID, "alice", "alice@example.com", Role.ADMIN),
                        new MemberResult(m2Id,     ORG_ID, "bob",   "bob@example.com", Role.MEMBER)));

        mockMvc.perform(get("/api/members")
                        .header("Authorization", bearerToken(memberId, ORG_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[1].username").value("bob"))
                .andExpect(jsonPath("$[0].organizationId").value(ORG_ID.toString()))
                .andExpect(jsonPath("$[0].role").value("ADMIN"))
                .andExpect(jsonPath("$[1].role").value("MEMBER"));
    }

    @DisplayName("List members returns empty list when org has no members")
    @Test
    void listMembers_returnsEmptyListWhenOrgHasNoMembers() throws Exception {
        UUID memberId = UUID.randomUUID();
        when(memberQueryUseCase.listMembers(ORG_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/members")
                        .header("Authorization", bearerToken(memberId, ORG_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
