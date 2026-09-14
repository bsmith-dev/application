package com.example.promptdb.web;

import com.example.promptdb.api.ApiValidationException;
import com.example.promptdb.api.GroupApiClient;
import com.example.promptdb.api.dto.CreateGroupRequest;
import com.example.promptdb.api.dto.RenameGroupRequest;
import com.example.promptdb.service.GroupContextService;
import com.example.promptdb.support.GroupPermissions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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

class GroupControllerTest extends ControllerTestSupport {

    private static final GroupPermissions PERMISSIONS = new GroupPermissions(true, true, true, true);

    @MockitoBean
    private GroupApiClient groupApiClient;

    @MockitoBean
    private GroupContextService groupContextService;

    @DisplayName("List renders groups")
    @Test
    void listRendersGroups() throws Exception {
        given(groupApiClient.listGroups()).willReturn(List.of(GROUP));

        mockMvc.perform(get("/groups"))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/list"))
                .andExpect(model().attribute("groups", List.of(GROUP)));
    }

    @DisplayName("Create form renders")
    @Test
    void createFormRenders() throws Exception {
        mockMvc.perform(get("/groups/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/form"))
                .andExpect(model().attribute("editing", false));
    }

    @DisplayName("Create post with blank name returns form")
    @Test
    void createPostWithBlankNameReturnsForm() throws Exception {
        mockMvc.perform(post("/groups").with(csrf()).param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/form"));
    }

    @DisplayName("Create post creates group and redirects to detail")
    @Test
    void createPostCreatesGroupAndRedirectsToDetail() throws Exception {
        given(groupApiClient.createGroup(new CreateGroupRequest("Engineering"))).willReturn(GROUP);

        mockMvc.perform(post("/groups").with(csrf()).param("name", "Engineering"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/groups/" + GROUP_ID));
    }

    @DisplayName("Create post when API validation fails shows error message")
    @Test
    void createPostWhenApiValidationFailsShowsErrorMessage() throws Exception {
        given(groupApiClient.createGroup(any())).willThrow(new ApiValidationException("duplicate", 422));

        mockMvc.perform(post("/groups").with(csrf()).param("name", "Engineering"))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/form"))
                .andExpect(model().attribute("errorMessage", "The API rejected this group: duplicate"));
    }

    @DisplayName("Detail renders group and permissions")
    @Test
    void detailRendersGroupAndPermissions() throws Exception {
        given(groupApiClient.getGroup(GROUP_ID)).willReturn(GROUP);
        given(groupContextService.permissions(GROUP_ID)).willReturn(PERMISSIONS);

        mockMvc.perform(get("/groups/{groupId}", GROUP_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/detail"))
                .andExpect(model().attribute("group", GROUP))
                .andExpect(model().attribute("permissions", PERMISSIONS));
    }

    @DisplayName("Edit form renders existing group")
    @Test
    void editFormRendersExistingGroup() throws Exception {
        given(groupApiClient.getGroup(GROUP_ID)).willReturn(GROUP);

        mockMvc.perform(get("/groups/{groupId}/edit", GROUP_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/form"))
                .andExpect(model().attribute("groupId", GROUP_ID))
                .andExpect(model().attribute("editing", true));
    }

    @DisplayName("Update post updates group and redirects to detail")
    @Test
    void updatePostUpdatesGroupAndRedirectsToDetail() throws Exception {
        mockMvc.perform(post("/groups/{groupId}", GROUP_ID).with(csrf()).param("name", "New name"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/groups/" + GROUP_ID));

        verify(groupApiClient).renameGroup(GROUP_ID, new RenameGroupRequest("New name"));
    }

    @DisplayName("Update post with blank name returns form")
    @Test
    void updatePostWithBlankNameReturnsForm() throws Exception {
        mockMvc.perform(post("/groups/{groupId}", GROUP_ID).with(csrf()).param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/form"))
                .andExpect(model().attribute("groupId", GROUP_ID))
                .andExpect(model().attribute("editing", true));
    }

    @DisplayName("Update post when API validation fails shows error message")
    @Test
    void updatePostWhenApiValidationFailsShowsErrorMessage() throws Exception {
        given(groupApiClient.renameGroup(any(), any())).willThrow(new ApiValidationException("bad", 422));

        mockMvc.perform(post("/groups/{groupId}", GROUP_ID).with(csrf()).param("name", "New name"))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/form"))
                .andExpect(model().attribute("errorMessage", "The API rejected this update: bad"))
                .andExpect(model().attribute("groupId", GROUP_ID))
                .andExpect(model().attribute("editing", true));
    }

    @DisplayName("Delete confirm renders existing group")
    @Test
    void deleteConfirmRendersExistingGroup() throws Exception {
        given(groupApiClient.getGroup(GROUP_ID)).willReturn(GROUP);

        mockMvc.perform(get("/groups/{groupId}/delete", GROUP_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/delete-confirm"))
                .andExpect(model().attribute("group", GROUP));
    }

    @DisplayName("Delete post deletes group and redirects to list")
    @Test
    void deletePostDeletesGroupAndRedirectsToList() throws Exception {
        mockMvc.perform(post("/groups/{groupId}/delete", GROUP_ID).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/groups"));

        verify(groupApiClient).deleteGroup(GROUP_ID);
    }
}
