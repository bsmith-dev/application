package app.prompts.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import app.prompts.application.dto.LoginRequest;
import app.prompts.application.dto.LoginResponse;
import app.prompts.application.port.TokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private TokenService tokenService;

    private LoginService loginService;

    @BeforeEach
    void setUp() {
        loginService = new LoginService(authenticationManager, tokenService);
    }

    @DisplayName("Login valid credentials returns token")
    @Test
    void login_validCredentials_returnsToken() {
        UUID memberId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        MemberUserDetails principal = new MemberUserDetails(memberId, orgId, "alice", "hash");
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(tokenService.generateToken(any(), any(), any(), any())).thenReturn("jwt-token-abc");

        LoginResponse response = loginService.login(new LoginRequest("alice", "password123"));

        assertThat(response.token()).isEqualTo("jwt-token-abc");
    }

    @DisplayName("Login bad credentials throws an exception")
    @Test
    void login_badCredentials_throws() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> loginWith("alice", "wrongpass"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @DisplayName("Login authorities are forwarded to token service")
    @Test
    void login_authoritiesAreForwardedToTokenService() {
        UUID memberId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        MemberUserDetails principal = new MemberUserDetails(memberId, orgId, "bob", "hash");
        // Authentication carrying an ADMIN authority
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal, null,
                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ADMIN")));

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(tokenService.generateToken(memberId, orgId, "bob", List.of("ADMIN")))
                .thenReturn("admin-jwt");

        LoginResponse response = loginService.login(new LoginRequest("bob", "password123"));

        assertThat(response.token()).isEqualTo("admin-jwt");
    }

    private LoginResponse loginWith(String username, String password) {
        return loginService.login(new LoginRequest(username, password));
    }
}
