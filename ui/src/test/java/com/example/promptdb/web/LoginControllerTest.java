package com.example.promptdb.web;

import com.example.promptdb.api.ApiUnauthorizedException;
import com.example.promptdb.api.ApiUnavailableException;
import com.example.promptdb.api.ApiValidationException;
import com.example.promptdb.api.dto.MemberResponse;
import com.example.promptdb.api.dto.Role;
import com.example.promptdb.security.CurrentApiSession;
import com.example.promptdb.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.WebApplicationContext;

import org.springframework.mock.web.MockHttpSession;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link LoginController}. The full application context is loaded
 * with a mock servlet environment, but {@link AuthenticationService} and
 * {@link CurrentApiSession} are replaced with Mockito mocks so no live API is required.
 * All state-changing requests include a CSRF token because the application uses
 * {@code CookieCsrfTokenRepository}.
 */
@SpringBootTest
@TestPropertySource(properties = {"app.api.base-url=http://127.0.0.1:1"})
class LoginControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    /**
     * Mocking the session-scoped bean replaces the scoped proxy with a controllable singleton.
     * {@link GlobalModelAttributes} calls {@code currentMember()} and {@code isAuthenticated()}
     * on every request, so safe defaults are configured in {@link #setUp()}.
     */
    @MockitoBean
    private CurrentApiSession currentApiSession;

    private static final MemberResponse ALICE = new MemberResponse(
            UUID.fromString("00000000-0000-0000-0000-000000000001"),
            UUID.fromString("00000000-0000-0000-0000-000000000002"),
            "alice", "alice@example.com", Role.MEMBER
    );

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        given(currentApiSession.isAuthenticated()).willReturn(false);
        given(currentApiSession.currentMember()).willReturn(Optional.empty());
        given(currentApiSession.accessToken()).willReturn(Optional.empty());
    }

    // ── GET /login ────────────────────────────────────────────────────────────

    @DisplayName("Login page renders when not authenticated")
    @Test
    void loginPageRendersWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @DisplayName("Login page redirects to dashboard when already authenticated")
    @Test
    void loginPageRedirectsToDashboardWhenAlreadyAuthenticated() throws Exception {
        given(currentApiSession.isAuthenticated()).willReturn(true);

        mockMvc.perform(get("/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @DisplayName("Login page exposes redirectTo in the model")
    @Test
    void loginPageExposesRedirectToInModel() throws Exception {
        mockMvc.perform(get("/login").param("redirectTo", "/groups"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("redirectTo", "/groups"));
    }

    @DisplayName("Login page shows session expired message")
    @Test
    void loginPageShowsSessionExpiredMessage() throws Exception {
        mockMvc.perform(get("/login").param("expired", ""))
                .andExpect(status().isOk())
                .andExpect(model().attribute("infoMessage", "Your session expired. Please sign in again."));
    }

    @DisplayName("Login page shows logged out message")
    @Test
    void loginPageShowsLoggedOutMessage() throws Exception {
        mockMvc.perform(get("/login").param("loggedOut", ""))
                .andExpect(status().isOk())
                .andExpect(model().attribute("infoMessage", "You have been signed out."));
    }

    // ── POST /login ───────────────────────────────────────────────────────────

    @DisplayName("Login post with blank credentials returns login view")
    @Test
    void loginPostWithBlankCredentialsReturnsLoginView() throws Exception {
        mockMvc.perform(post("/login").with(csrf())
                        .param("username", "").param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @DisplayName("Login post with valid credentials redirects to dashboard")
    @Test
    void loginPostWithValidCredentialsRedirectsToDashboard() throws Exception {
        given(authenticationService.login("alice", "s3cr3t")).willReturn(ALICE);

        mockMvc.perform(post("/login").with(csrf()).session(new MockHttpSession())
                        .param("username", "alice").param("password", "s3cr3t"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @DisplayName("Login post follows a safe redirectTo parameter")
    @Test
    void loginPostFollowsSafeRedirectToParam() throws Exception {
        given(authenticationService.login("alice", "s3cr3t")).willReturn(ALICE);

        mockMvc.perform(post("/login").with(csrf()).session(new MockHttpSession())
                        .param("username", "alice").param("password", "s3cr3t")
                        .param("redirectTo", "/groups"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/groups"));
    }

    @DisplayName("Login post ignores an absolute redirectTo parameter")
    @Test
    void loginPostIgnoresAbsoluteRedirectTo() throws Exception {
        given(authenticationService.login("alice", "s3cr3t")).willReturn(ALICE);

        mockMvc.perform(post("/login").with(csrf()).session(new MockHttpSession())
                        .param("username", "alice").param("password", "s3cr3t")
                        .param("redirectTo", "https://evil.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @DisplayName("Login post ignores a protocol-relative redirectTo parameter")
    @Test
    void loginPostIgnoresProtocolRelativeRedirectTo() throws Exception {
        given(authenticationService.login("alice", "s3cr3t")).willReturn(ALICE);

        mockMvc.perform(post("/login").with(csrf()).session(new MockHttpSession())
                        .param("username", "alice").param("password", "s3cr3t")
                        .param("redirectTo", "//evil.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @DisplayName("Login post with invalid credentials returns login view")
    @Test
    void loginPostWithInvalidCredentialsReturnsLoginView() throws Exception {
        given(authenticationService.login(any(), any()))
                .willThrow(new ApiUnauthorizedException("Invalid credentials"));

        mockMvc.perform(post("/login").with(csrf())
                        .param("username", "alice").param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @DisplayName("Login post when API validation fails shows error message")
    @Test
    void loginPostWhenApiValidationFailsShowsErrorMessage() throws Exception {
        given(authenticationService.login(any(), any()))
                .willThrow(new ApiValidationException("Field errors", 422));

        mockMvc.perform(post("/login").with(csrf())
                        .param("username", "alice").param("password", "s3cr3t"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @DisplayName("Login post when API unavailable shows error message")
    @Test
    void loginPostWhenApiUnavailableShowsErrorMessage() throws Exception {
        given(authenticationService.login(any(), any()))
                .willThrow(new ApiUnavailableException("Service unavailable"));

        mockMvc.perform(post("/login").with(csrf())
                        .param("username", "alice").param("password", "s3cr3t"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("errorMessage",
                        "The API is currently unavailable. Please try again shortly."));
    }

    @DisplayName("Login post when API not reachable shows error message")
    @Test
    void loginPostWhenApiNotReachableShowsErrorMessage() throws Exception {
        given(authenticationService.login(any(), any()))
                .willThrow(new ResourceAccessException("Connection refused"));

        mockMvc.perform(post("/login").with(csrf())
                        .param("username", "alice").param("password", "s3cr3t"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    // ── GET /logout ───────────────────────────────────────────────────────────

    @DisplayName("Logout delegates to authentication service and redirects to login page")
    @Test
    void logoutDelegatesToAuthenticationServiceAndRedirectsToLoginPage() throws Exception {
        mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?loggedOut"));

        verify(authenticationService).logout();
    }
}
