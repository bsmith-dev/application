package app.prompts.presentation.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import app.prompts.application.dto.OrganizationResult;
import app.prompts.application.port.ManageOrganizationUseCase;
import app.prompts.application.service.AccessDeniedException;
import app.prompts.application.service.OrganizationNotFoundException;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrganizationControllerTest {

    @Mock
    private ManageOrganizationUseCase organizationUseCase;

    private MockMvc mockMvc;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(new JwtProperties(WebMvcTestSupport.TEST_SECRET, WebMvcTestSupport.TEST_EXPIRY));
        mockMvc = MockMvcBuilders
                .standaloneSetup(new OrganizationController(organizationUseCase))
                .setControllerAdvice(new PromptsExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .addFilter(new JwtAuthFilter(jwtService))
                .build();
        SecurityContextHolder.clearContext();
    }

    private String bearerToken(UUID memberId, UUID orgId, List<String> authorities) {
        return "Bearer " + jwtService.generateToken(memberId, orgId, "alice", authorities);
    }

    @Test
    void createOrganization_validRequest_returns201() throws Exception {
        UUID orgId = UUID.randomUUID();
        when(organizationUseCase.createOrganization(any()))
                .thenReturn(new OrganizationResult(orgId, "Acme"));

        mockMvc.perform(post("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Acme"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.organizationId").value(orgId.toString()))
                .andExpect(jsonPath("$.name").value("Acme"));
    }

    @Test
    void createOrganization_blankName_returns400() throws Exception {
        mockMvc.perform(post("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void listOrganizations_authenticatedAdmin_returns200() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        when(organizationUseCase.listOrganizations(requesterId))
                .thenReturn(List.of(new OrganizationResult(orgId, "Acme")));

        mockMvc.perform(get("/api/organizations")
                        .header("Authorization", bearerToken(requesterId, orgId, List.of("ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].organizationId").value(orgId.toString()))
                .andExpect(jsonPath("$[0].name").value("Acme"));
    }

    @Test
    void listOrganizations_serviceThrowsAccessDenied_returns403() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        when(organizationUseCase.listOrganizations(requesterId))
                .thenThrow(new AccessDeniedException("Only ADMIN can list organizations"));

        mockMvc.perform(get("/api/organizations")
                        .header("Authorization", bearerToken(requesterId, orgId, List.of("MEMBER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void renameOrganization_validRequest_returns200() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        when(organizationUseCase.renameOrganization(any()))
                .thenReturn(new OrganizationResult(orgId, "Acme Updated"));

        mockMvc.perform(put("/api/organizations/{organizationId}", orgId)
                        .header("Authorization", bearerToken(requesterId, orgId, List.of("ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Acme Updated"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Acme Updated"));
    }

    @Test
    void renameOrganization_blankName_returns400() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        mockMvc.perform(put("/api/organizations/{organizationId}", orgId)
                        .header("Authorization", bearerToken(requesterId, orgId, List.of("ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void renameOrganization_serviceThrowsNotFound_returns404() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        when(organizationUseCase.renameOrganization(any()))
                .thenThrow(new OrganizationNotFoundException("organization not found"));

        mockMvc.perform(put("/api/organizations/{organizationId}", orgId)
                        .header("Authorization", bearerToken(requesterId, orgId, List.of("ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Acme Updated"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOrganization_emptyOrg_returns204() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        mockMvc.perform(delete("/api/organizations/{organizationId}", orgId)
                        .header("Authorization", bearerToken(requesterId, orgId, List.of("ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteOrganization_nonEmptyOrg_returns409() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        doThrow(new IllegalStateException("Organization must not contain members or groups before deletion"))
                .when(organizationUseCase).deleteOrganization(orgId, requesterId);

        mockMvc.perform(delete("/api/organizations/{organizationId}", orgId)
                        .header("Authorization", bearerToken(requesterId, orgId, List.of("ADMIN"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"));
    }
}
