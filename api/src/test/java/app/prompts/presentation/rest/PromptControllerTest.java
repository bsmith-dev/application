package app.prompts.presentation.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import app.prompts.application.dto.PromptResult;
import app.prompts.application.port.ManagePromptUseCase;
import app.prompts.application.service.AccessDeniedException;
import app.prompts.application.service.PromptNotFoundException;
import app.prompts.infrastructure.security.JwtAuthFilter;
import app.prompts.infrastructure.security.JwtProperties;
import app.prompts.infrastructure.security.JwtService;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc tests for {@link PromptController}.
 *
 * Uses {@code standaloneSetup} with {@link JwtAuthFilter} and
 * {@link AuthenticationPrincipalArgumentResolver} so that JWT-populated
 * {@code @AuthenticationPrincipal} parameters resolve correctly.
 */
@ExtendWith(MockitoExtension.class)
class PromptControllerTest {

    @Mock
    private ManagePromptUseCase promptUseCase;

    private MockMvc mockMvc;
    private JwtService jwtService;

    private static final UUID GROUP_ID  = UUID.randomUUID();
    private static final UUID MEMBER_ID = UUID.randomUUID();
    private static final UUID PROMPT_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(new JwtProperties(WebMvcTestSupport.TEST_SECRET, WebMvcTestSupport.TEST_EXPIRY));
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PromptController(promptUseCase))
                .setControllerAdvice(new PromptsExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .addFilter(new JwtAuthFilter(jwtService))
                .build();
        SecurityContextHolder.clearContext();
    }

    private String bearer(UUID memberId) {
        return "Bearer " + jwtService.generateToken(memberId, UUID.randomUUID(), "alice", List.of());
    }

    private PromptResult sampleResult() {
        return new PromptResult(PROMPT_ID, GROUP_ID, MEMBER_ID, "Hello", "World content",
                Instant.now(), Instant.now());
    }

    // ── GET /api/groups/{groupId}/prompts ────────────────────────────────────

    @Test
    void listPrompts_returns200WithList() throws Exception {
        when(promptUseCase.listPrompts(GROUP_ID, MEMBER_ID)).thenReturn(List.of(sampleResult()));

        mockMvc.perform(get("/api/groups/{groupId}/prompts", GROUP_ID)
                        .header("Authorization", bearer(MEMBER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Hello"))
                .andExpect(jsonPath("$[0].content").value("World content"));
    }

    @Test
    void listPrompts_emptyList_returns200() throws Exception {
        when(promptUseCase.listPrompts(GROUP_ID, MEMBER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/groups/{groupId}/prompts", GROUP_ID)
                        .header("Authorization", bearer(MEMBER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── POST /api/groups/{groupId}/prompts ───────────────────────────────────

    @Test
    void createPrompt_validRequest_returns201() throws Exception {
        when(promptUseCase.createPrompt(any())).thenReturn(sampleResult());

        mockMvc.perform(post("/api/groups/{groupId}/prompts", GROUP_ID)
                        .header("Authorization", bearer(MEMBER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Hello","content":"World content"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Hello"))
                .andExpect(jsonPath("$.id").value(PROMPT_ID.toString()));
    }

    @Test
    void createPrompt_blankTitle_returns400() throws Exception {
        mockMvc.perform(post("/api/groups/{groupId}/prompts", GROUP_ID)
                        .header("Authorization", bearer(MEMBER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"","content":"some content"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void createPrompt_blankContent_returns400() throws Exception {
        mockMvc.perform(post("/api/groups/{groupId}/prompts", GROUP_ID)
                        .header("Authorization", bearer(MEMBER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Hello","content":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.content").exists());
    }

    @Test
    void createPrompt_contentExceedsMax_returns400() throws Exception {
        String huge = "x".repeat(100_001);

        mockMvc.perform(post("/api/groups/{groupId}/prompts", GROUP_ID)
                        .header("Authorization", bearer(MEMBER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Hello\",\"content\":\"" + huge + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPrompt_missingBody_returns400() throws Exception {
        mockMvc.perform(post("/api/groups/{groupId}/prompts", GROUP_ID)
                        .header("Authorization", bearer(MEMBER_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ── GET /api/groups/{groupId}/prompts/{promptId} ─────────────────────────

    @Test
    void getPrompt_found_returns200() throws Exception {
        when(promptUseCase.getPrompt(GROUP_ID, PROMPT_ID, MEMBER_ID)).thenReturn(sampleResult());

        mockMvc.perform(get("/api/groups/{groupId}/prompts/{promptId}", GROUP_ID, PROMPT_ID)
                        .header("Authorization", bearer(MEMBER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PROMPT_ID.toString()))
                .andExpect(jsonPath("$.title").value("Hello"));
    }

    @Test
    void getPrompt_notFound_returns404() throws Exception {
        when(promptUseCase.getPrompt(any(), any(), any()))
                .thenThrow(new PromptNotFoundException("not found"));

        mockMvc.perform(get("/api/groups/{groupId}/prompts/{promptId}", GROUP_ID, PROMPT_ID)
                        .header("Authorization", bearer(MEMBER_ID)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPrompt_accessDenied_returns403() throws Exception {
        when(promptUseCase.getPrompt(any(), any(), any()))
                .thenThrow(new AccessDeniedException("denied"));

        mockMvc.perform(get("/api/groups/{groupId}/prompts/{promptId}", GROUP_ID, PROMPT_ID)
                        .header("Authorization", bearer(MEMBER_ID)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPrompt_invalidPromptId_returns400() throws Exception {
        mockMvc.perform(get("/api/groups/{groupId}/prompts/{promptId}", GROUP_ID, "not-a-uuid")
                        .header("Authorization", bearer(MEMBER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Parameter"));
    }

    // ── PUT /api/groups/{groupId}/prompts/{promptId} ─────────────────────────

    @Test
    void updatePrompt_validRequest_returns200() throws Exception {
        when(promptUseCase.updatePrompt(any())).thenReturn(sampleResult());

        mockMvc.perform(put("/api/groups/{groupId}/prompts/{promptId}", GROUP_ID, PROMPT_ID)
                        .header("Authorization", bearer(MEMBER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Updated","content":"new content"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Hello"));
    }

    @Test
    void updatePrompt_blankTitle_returns400() throws Exception {
        mockMvc.perform(put("/api/groups/{groupId}/prompts/{promptId}", GROUP_ID, PROMPT_ID)
                        .header("Authorization", bearer(MEMBER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"","content":"content"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void updatePrompt_notFound_returns404() throws Exception {
        when(promptUseCase.updatePrompt(any()))
                .thenThrow(new PromptNotFoundException("not found"));

        mockMvc.perform(put("/api/groups/{groupId}/prompts/{promptId}", GROUP_ID, PROMPT_ID)
                        .header("Authorization", bearer(MEMBER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Title","content":"content"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePrompt_accessDenied_returns403() throws Exception {
        when(promptUseCase.updatePrompt(any()))
                .thenThrow(new AccessDeniedException("denied"));

        mockMvc.perform(put("/api/groups/{groupId}/prompts/{promptId}", GROUP_ID, PROMPT_ID)
                        .header("Authorization", bearer(MEMBER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Title","content":"content"}
                                """))
                .andExpect(status().isForbidden());
    }

    // ── DELETE /api/groups/{groupId}/prompts/{promptId} ──────────────────────

    @Test
    void deletePrompt_success_returns204() throws Exception {
        mockMvc.perform(delete("/api/groups/{groupId}/prompts/{promptId}", GROUP_ID, PROMPT_ID)
                        .header("Authorization", bearer(MEMBER_ID)))
                .andExpect(status().isNoContent());

        verify(promptUseCase).deletePrompt(GROUP_ID, PROMPT_ID, MEMBER_ID);
    }

    @Test
    void deletePrompt_notFound_returns404() throws Exception {
        doThrow(new PromptNotFoundException("not found"))
                .when(promptUseCase).deletePrompt(any(), any(), any());

        mockMvc.perform(delete("/api/groups/{groupId}/prompts/{promptId}", GROUP_ID, PROMPT_ID)
                        .header("Authorization", bearer(MEMBER_ID)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePrompt_accessDenied_returns403() throws Exception {
        doThrow(new AccessDeniedException("denied"))
                .when(promptUseCase).deletePrompt(any(), any(), any());

        mockMvc.perform(delete("/api/groups/{groupId}/prompts/{promptId}", GROUP_ID, PROMPT_ID)
                        .header("Authorization", bearer(MEMBER_ID)))
                .andExpect(status().isForbidden());
    }
}
