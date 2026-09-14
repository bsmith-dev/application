package app.prompts.ui.web;

import app.prompts.ui.api.ApiValidationException;
import app.prompts.ui.api.GroupApiClient;
import app.prompts.ui.api.PromptApiClient;
import app.prompts.ui.api.dto.CreatePromptRequest;
import app.prompts.ui.api.dto.PromptResponse;
import app.prompts.ui.api.dto.Role;
import app.prompts.ui.api.dto.UpdatePromptRequest;
import app.prompts.ui.service.GroupContextService;
import app.prompts.ui.support.GroupPermissions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class PromptControllerTest extends ControllerTestSupport {

    private static final GroupPermissions PERMISSIONS = new GroupPermissions(true, true, true, true);
    private static final PromptResponse PROMPT = new PromptResponse(
            PROMPT_ID, GROUP_ID, MEMBER_ID, "Title", "Content", Instant.EPOCH, Instant.EPOCH
    );

    @MockitoBean
    private PromptApiClient promptApiClient;

    @MockitoBean
    private GroupApiClient groupApiClient;

    @MockitoBean
    private GroupContextService groupContextService;

    @DisplayName("List renders prompts and permissions")
    @Test
    void listRendersPromptsAndPermissions() throws Exception {
        given(groupApiClient.getGroup(GROUP_ID)).willReturn(GROUP);
        given(promptApiClient.listPrompts(GROUP_ID)).willReturn(List.of(PROMPT));
        given(groupContextService.permissions(GROUP_ID)).willReturn(PERMISSIONS);

        mockMvc.perform(get("/groups/{groupId}/prompts", GROUP_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/prompts/list"))
                .andExpect(model().attribute("group", GROUP))
                .andExpect(model().attribute("prompts", List.of(PROMPT)))
                .andExpect(model().attribute("permissions", PERMISSIONS));
    }

    @DisplayName("Create form renders")
    @Test
    void createFormRenders() throws Exception {
        given(groupApiClient.getGroup(GROUP_ID)).willReturn(GROUP);

        mockMvc.perform(get("/groups/{groupId}/prompts/new", GROUP_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/prompts/form"))
                .andExpect(model().attribute("group", GROUP));
    }

    @DisplayName("Create post with validation errors returns form")
    @Test
    void createPostWithValidationErrorsReturnsForm() throws Exception {
        given(groupApiClient.getGroup(GROUP_ID)).willReturn(GROUP);

        mockMvc.perform(post("/groups/{groupId}/prompts", GROUP_ID).with(csrf())
                        .param("title", "")
                        .param("content", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/prompts/form"))
                .andExpect(model().attribute("group", GROUP));
    }

    @DisplayName("Create post creates prompt and redirects to detail")
    @Test
    void createPostCreatesPromptAndRedirectsToDetail() throws Exception {
        given(promptApiClient.createPrompt(GROUP_ID, new CreatePromptRequest("Title", "Content"))).willReturn(PROMPT);

        mockMvc.perform(post("/groups/{groupId}/prompts", GROUP_ID).with(csrf())
                        .param("title", "Title")
                        .param("content", "Content"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/groups/" + GROUP_ID + "/prompts/" + PROMPT_ID));
    }

    @DisplayName("Create post when API validation fails shows error message")
    @Test
    void createPostWhenApiValidationFailsShowsErrorMessage() throws Exception {
        given(groupApiClient.getGroup(GROUP_ID)).willReturn(GROUP);
        given(promptApiClient.createPrompt(any(), any())).willThrow(new ApiValidationException("bad", 422));

        mockMvc.perform(post("/groups/{groupId}/prompts", GROUP_ID).with(csrf())
                        .param("title", "Title")
                        .param("content", "Content"))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/prompts/form"))
                .andExpect(model().attribute("errorMessage", "The API rejected this prompt: bad"));
    }

    @DisplayName("Detail renders prompt with can edit flag")
    @Test
    void detailRendersPromptWithCanEditFlag() throws Exception {
        given(groupApiClient.getGroup(GROUP_ID)).willReturn(GROUP);
        given(promptApiClient.getPrompt(GROUP_ID, PROMPT_ID)).willReturn(PROMPT);
        given(groupContextService.membershipRole(GROUP_ID)).willReturn(Role.MEMBER);

        mockMvc.perform(get("/groups/{groupId}/prompts/{promptId}", GROUP_ID, PROMPT_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/prompts/detail"))
                .andExpect(model().attribute("group", GROUP))
                .andExpect(model().attribute("prompt", PROMPT))
                .andExpect(model().attribute("canEdit", true));
    }

    @DisplayName("Edit form renders existing prompt")
    @Test
    void editFormRendersExistingPrompt() throws Exception {
        given(groupApiClient.getGroup(GROUP_ID)).willReturn(GROUP);
        given(promptApiClient.getPrompt(GROUP_ID, PROMPT_ID)).willReturn(PROMPT);

        mockMvc.perform(get("/groups/{groupId}/prompts/{promptId}/edit", GROUP_ID, PROMPT_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/prompts/form"))
                .andExpect(model().attribute("group", GROUP))
                .andExpect(model().attribute("promptId", PROMPT_ID))
                .andExpect(model().attribute("editing", true));
    }

    @DisplayName("Update post with validation errors returns form")
    @Test
    void updatePostWithValidationErrorsReturnsForm() throws Exception {
        given(groupApiClient.getGroup(GROUP_ID)).willReturn(GROUP);

        mockMvc.perform(post("/groups/{groupId}/prompts/{promptId}", GROUP_ID, PROMPT_ID).with(csrf())
                        .param("title", "")
                        .param("content", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/prompts/form"))
                .andExpect(model().attribute("group", GROUP))
                .andExpect(model().attribute("promptId", PROMPT_ID))
                .andExpect(model().attribute("editing", true));
    }

    @DisplayName("Update post updates prompt and redirects to detail")
    @Test
    void updatePostUpdatesPromptAndRedirectsToDetail() throws Exception {
        mockMvc.perform(post("/groups/{groupId}/prompts/{promptId}", GROUP_ID, PROMPT_ID).with(csrf())
                        .param("title", "New title")
                        .param("content", "New content"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/groups/" + GROUP_ID + "/prompts/" + PROMPT_ID));

        verify(promptApiClient).updatePrompt(
                GROUP_ID, PROMPT_ID, new UpdatePromptRequest("New title", "New content")
        );
    }

    @DisplayName("Update post when API validation fails shows error message")
    @Test
    void updatePostWhenApiValidationFailsShowsErrorMessage() throws Exception {
        given(groupApiClient.getGroup(GROUP_ID)).willReturn(GROUP);
        given(promptApiClient.updatePrompt(any(), any(), any())).willThrow(new ApiValidationException("bad", 422));

        mockMvc.perform(post("/groups/{groupId}/prompts/{promptId}", GROUP_ID, PROMPT_ID).with(csrf())
                        .param("title", "New title")
                        .param("content", "New content"))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/prompts/form"))
                .andExpect(model().attribute("errorMessage", "The API rejected this update: bad"))
                .andExpect(model().attribute("editing", true));
    }

    @DisplayName("Delete confirm renders existing prompt")
    @Test
    void deleteConfirmRendersExistingPrompt() throws Exception {
        given(groupApiClient.getGroup(GROUP_ID)).willReturn(GROUP);
        given(promptApiClient.getPrompt(GROUP_ID, PROMPT_ID)).willReturn(PROMPT);

        mockMvc.perform(get("/groups/{groupId}/prompts/{promptId}/delete", GROUP_ID, PROMPT_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/prompts/delete-confirm"))
                .andExpect(model().attribute("group", GROUP))
                .andExpect(model().attribute("prompt", PROMPT));
    }

    @DisplayName("Delete post deletes prompt and redirects to list")
    @Test
    void deletePostDeletesPromptAndRedirectsToList() throws Exception {
        mockMvc.perform(post("/groups/{groupId}/prompts/{promptId}/delete", GROUP_ID, PROMPT_ID).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/groups/" + GROUP_ID + "/prompts"));

        verify(promptApiClient).deletePrompt(GROUP_ID, PROMPT_ID);
    }
}
