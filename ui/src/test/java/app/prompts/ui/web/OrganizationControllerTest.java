package app.prompts.ui.web;

import app.prompts.ui.api.ApiConflictException;
import app.prompts.ui.api.ApiValidationException;
import app.prompts.ui.api.OrganizationApiClient;
import app.prompts.ui.api.dto.CreateOrganizationRequest;
import app.prompts.ui.api.dto.RenameOrganizationRequest;
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

class OrganizationControllerTest extends ControllerTestSupport {

    @MockitoBean
    private OrganizationApiClient organizationApiClient;

    @DisplayName("List renders organizations")
    @Test
    void listRendersOrganizations() throws Exception {
        given(organizationApiClient.listOrganizations()).willReturn(List.of(ORGANIZATION));

        mockMvc.perform(get("/organizations"))
                .andExpect(status().isOk())
                .andExpect(view().name("organizations/list"))
                .andExpect(model().attribute("organizations", List.of(ORGANIZATION)));
    }

    @DisplayName("Create form renders")
    @Test
    void createFormRenders() throws Exception {
        mockMvc.perform(get("/organizations/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("organizations/form"))
                .andExpect(model().attribute("editing", false));
    }

    @DisplayName("Create post with blank name returns form")
    @Test
    void createPostWithBlankNameReturnsForm() throws Exception {
        mockMvc.perform(post("/organizations").with(csrf()).param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("organizations/form"))
                .andExpect(model().attribute("editing", false));
    }

    @DisplayName("Create post creates organization and redirects to list")
    @Test
    void createPostCreatesOrganizationAndRedirectsToList() throws Exception {
        mockMvc.perform(post("/organizations").with(csrf()).param("name", "Acme"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/organizations"));

        verify(organizationApiClient).createOrganization(new CreateOrganizationRequest("Acme"));
    }

    @DisplayName("Create post when API validation fails shows error message")
    @Test
    void createPostWhenApiValidationFailsShowsErrorMessage() throws Exception {
        given(organizationApiClient.createOrganization(any())).willThrow(new ApiValidationException("duplicate", 422));

        mockMvc.perform(post("/organizations").with(csrf()).param("name", "Acme"))
                .andExpect(status().isOk())
                .andExpect(view().name("organizations/form"))
                .andExpect(model().attribute("errorMessage", "The API rejected this organization: duplicate"))
                .andExpect(model().attribute("editing", false));
    }

    @DisplayName("Edit form renders existing organization")
    @Test
    void editFormRendersExistingOrganization() throws Exception {
        given(organizationApiClient.listOrganizations()).willReturn(List.of(ORGANIZATION));

        mockMvc.perform(get("/organizations/{organizationId}/edit", ORGANIZATION_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("organizations/form"))
                .andExpect(model().attribute("organizationId", ORGANIZATION_ID))
                .andExpect(model().attribute("editing", true));
    }

    @DisplayName("Update post updates organization and redirects to list")
    @Test
    void updatePostUpdatesOrganizationAndRedirectsToList() throws Exception {
        mockMvc.perform(post("/organizations/{organizationId}", ORGANIZATION_ID).with(csrf()).param("name", "New name"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/organizations"));

        verify(organizationApiClient).renameOrganization(ORGANIZATION_ID, new RenameOrganizationRequest("New name"));
    }

    @DisplayName("Update post with blank name returns form")
    @Test
    void updatePostWithBlankNameReturnsForm() throws Exception {
        mockMvc.perform(post("/organizations/{organizationId}", ORGANIZATION_ID).with(csrf()).param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("organizations/form"))
                .andExpect(model().attribute("organizationId", ORGANIZATION_ID))
                .andExpect(model().attribute("editing", true));
    }

    @DisplayName("Update post when API validation fails shows error message")
    @Test
    void updatePostWhenApiValidationFailsShowsErrorMessage() throws Exception {
        given(organizationApiClient.renameOrganization(any(), any())).willThrow(new ApiValidationException("bad", 422));

        mockMvc.perform(post("/organizations/{organizationId}", ORGANIZATION_ID).with(csrf()).param("name", "New name"))
                .andExpect(status().isOk())
                .andExpect(view().name("organizations/form"))
                .andExpect(model().attribute("errorMessage", "The API rejected this update: bad"))
                .andExpect(model().attribute("organizationId", ORGANIZATION_ID))
                .andExpect(model().attribute("editing", true));
    }

    @DisplayName("Delete confirm renders existing organization")
    @Test
    void deleteConfirmRendersExistingOrganization() throws Exception {
        given(organizationApiClient.listOrganizations()).willReturn(List.of(ORGANIZATION));

        mockMvc.perform(get("/organizations/{organizationId}/delete", ORGANIZATION_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("organizations/delete-confirm"))
                .andExpect(model().attribute("organization", ORGANIZATION));
    }

    @DisplayName("Delete post deletes organization and redirects to list")
    @Test
    void deletePostDeletesOrganizationAndRedirectsToList() throws Exception {
        mockMvc.perform(post("/organizations/{organizationId}/delete", ORGANIZATION_ID).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/organizations"));

        verify(organizationApiClient).deleteOrganization(ORGANIZATION_ID);
    }

    @DisplayName("Delete post when API conflict returns confirm view")
    @Test
    void deletePostWhenApiConflictReturnsConfirmView() throws Exception {
        given(organizationApiClient.listOrganizations()).willReturn(List.of(ORGANIZATION));
        org.mockito.BDDMockito.willThrow(new ApiConflictException("non-empty"))
                .given(organizationApiClient).deleteOrganization(ORGANIZATION_ID);

        mockMvc.perform(post("/organizations/{organizationId}/delete", ORGANIZATION_ID).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("organizations/delete-confirm"))
                .andExpect(model().attribute("organization", ORGANIZATION))
                .andExpect(model().attributeExists("conflictMessage"));
    }
}
