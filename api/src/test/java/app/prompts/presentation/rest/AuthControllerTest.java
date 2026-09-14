package app.prompts.presentation.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import app.prompts.application.dto.LoginResponse;
import app.prompts.application.port.LoginUseCase;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit-level MockMvc test for {@link AuthController}.
 *
 * Uses {@code standaloneSetup} — no Spring context, no database.
 * Security is intentionally bypassed here because {@code /api/auth/login}
 * is a public endpoint; behaviour under the full security filter chain is
 * covered by the JWT filter unit tests.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private LoginUseCase loginUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(loginUseCase))
                .setControllerAdvice(new PromptsExceptionHandler())
                .build();
    }

    @DisplayName("Login valid credentials returns 200 with token")
    @Test
    void login_validCredentials_returns200WithToken() throws Exception {
        when(loginUseCase.login(any())).thenReturn(new LoginResponse("jwt-abc"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-abc"));
    }

    @DisplayName("Login blank username returns 400 with errors")
    @Test
    void login_blankUsername_returns400WithErrors() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"","password":"password123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").exists());
    }

    @DisplayName("Login short password returns 400")
    @Test
    void login_shortPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","password":"short"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Login username too short returns 400")
    @Test
    void login_usernameTooShort_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"ab","password":"password123"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Login bad credentials propagates exception")
    @Test
    void login_badCredentials_propagatesException() {
        when(loginUseCase.login(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // BadCredentialsException is not mapped by PromptsExceptionHandler (it is handled by
        // Spring Security's AuthenticationEntryPoint in the real filter chain).
        // In standalone mode MockMvc rethrows it as a ServletException wrapping our exception.
        assertThat(
                org.junit.jupiter.api.Assertions.assertThrows(
                        Exception.class,
                        () -> performLogin("alice", "wrongpassword")))
                .satisfiesAnyOf(
                        e -> assertThat(e).isInstanceOf(BadCredentialsException.class),
                        e -> assertThat(e).hasRootCauseInstanceOf(BadCredentialsException.class));
    }

    private void performLogin(String username, String password) throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, password)));
    }
}
