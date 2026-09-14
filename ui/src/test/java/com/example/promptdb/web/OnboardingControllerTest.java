package com.example.promptdb.web;

import com.example.promptdb.api.ApiUnavailableException;
import com.example.promptdb.api.ApiValidationException;
import com.example.promptdb.api.MemberApiClient;
import com.example.promptdb.api.OrganizationApiClient;
import com.example.promptdb.api.dto.CreateOrganizationRequest;
import com.example.promptdb.api.dto.RegisterMemberRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.ResourceAccessException;

import java.util.Optional;

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

class OnboardingControllerTest extends ControllerTestSupport {

    @MockitoBean
    private OrganizationApiClient organizationApiClient;

    @MockitoBean
    private MemberApiClient memberApiClient;

    @DisplayName("Bootstrap form renders")
    @Test
    void bootstrapFormRenders() throws Exception {
        given(currentApiSession.isAuthenticated()).willReturn(false);
        given(currentApiSession.currentMember()).willReturn(Optional.empty());

        mockMvc.perform(get("/organizations/bootstrap"))
                .andExpect(status().isOk())
                .andExpect(view().name("organizations/bootstrap"));
    }

    @DisplayName("Bootstrap post with blank name returns form")
    @Test
    void bootstrapPostWithBlankNameReturnsForm() throws Exception {
        mockMvc.perform(post("/organizations/bootstrap").with(csrf()).param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("organizations/bootstrap"));
    }

    @DisplayName("Bootstrap post creates organization and renders registration prompt")
    @Test
    void bootstrapPostCreatesOrganizationAndRendersRegistrationPrompt() throws Exception {
        given(organizationApiClient.createOrganization(new CreateOrganizationRequest("Acme"))).willReturn(ORGANIZATION);

        mockMvc.perform(post("/organizations/bootstrap").with(csrf()).param("name", "Acme"))
                .andExpect(status().isOk())
                .andExpect(view().name("organizations/bootstrap"))
                .andExpect(model().attribute("createdOrganization", ORGANIZATION))
                .andExpect(model().attributeExists("registerForm"));
    }

    @DisplayName("Bootstrap post when API validation fails shows error message")
    @Test
    void bootstrapPostWhenApiValidationFailsShowsErrorMessage() throws Exception {
        given(organizationApiClient.createOrganization(any()))
                .willThrow(new ApiValidationException("duplicate", 422));

        mockMvc.perform(post("/organizations/bootstrap").with(csrf()).param("name", "Acme"))
                .andExpect(status().isOk())
                .andExpect(view().name("organizations/bootstrap"))
                .andExpect(model().attribute("errorMessage", "The API rejected this organization: duplicate"));
    }

    @DisplayName("Bootstrap post when API unavailable shows error message")
    @Test
    void bootstrapPostWhenApiUnavailableShowsErrorMessage() throws Exception {
        given(organizationApiClient.createOrganization(any())).willThrow(new ResourceAccessException("refused"));

        mockMvc.perform(post("/organizations/bootstrap").with(csrf()).param("name", "Acme"))
                .andExpect(status().isOk())
                .andExpect(view().name("organizations/bootstrap"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @DisplayName("Register form renders with organization ID")
    @Test
    void registerFormRendersWithOrganizationId() throws Exception {
        mockMvc.perform(get("/register").param("organizationId", ORGANIZATION_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerForm"));
    }

    @DisplayName("Register post with invalid input returns form")
    @Test
    void registerPostWithInvalidInputReturnsForm() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "")
                        .param("password", "short")
                        .param("email", "not-email")
                        .param("organizationId", "bad-id"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @DisplayName("Register post creates member and redirects to login")
    @Test
    void registerPostCreatesMemberAndRedirectsToLogin() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "alice")
                        .param("password", "password1")
                        .param("email", "alice@example.com")
                        .param("organizationId", ORGANIZATION_ID.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(memberApiClient).register(new RegisterMemberRequest(
                "alice", "password1", "alice@example.com", ORGANIZATION_ID
        ));
    }

    @DisplayName("Register post when API validation fails shows error message")
    @Test
    void registerPostWhenApiValidationFailsShowsErrorMessage() throws Exception {
        given(memberApiClient.register(any())).willThrow(new ApiValidationException("duplicate", 422));

        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "alice")
                        .param("password", "password1")
                        .param("email", "alice@example.com")
                        .param("organizationId", ORGANIZATION_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("errorMessage", "Registration was rejected: duplicate"));
    }

    @DisplayName("Register post when API unavailable shows error message")
    @Test
    void registerPostWhenApiUnavailableShowsErrorMessage() throws Exception {
        given(memberApiClient.register(any())).willThrow(new ApiUnavailableException("down"));

        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "alice")
                        .param("password", "password1")
                        .param("email", "alice@example.com")
                        .param("organizationId", ORGANIZATION_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("errorMessage"));
    }
}
