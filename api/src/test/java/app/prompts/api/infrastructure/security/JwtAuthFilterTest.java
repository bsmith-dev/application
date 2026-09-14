package app.prompts.api.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    private static final String SECRET = "test-secret-key-at-least-32-bytes!!";

    private JwtService jwtService;
    private JwtAuthFilter filter;

    @Mock
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(new JwtProperties(SECRET, 3600L));
        filter = new JwtAuthFilter(jwtService);
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequest requestWithBearer(String token) {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer " + token);
        return req;
    }

    @DisplayName("Valid token populates security context")
    @Test
    void validToken_populatesSecurityContext() throws Exception {
        UUID memberId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        String token = jwtService.generateToken(memberId, orgId, "alice", List.of());

        filter.doFilter(requestWithBearer(token), new MockHttpServletResponse(), filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.isAuthenticated()).isTrue();
        MemberUserDetails principal = (MemberUserDetails) auth.getPrincipal();
        assertThat(principal.getMemberIdRaw()).isEqualTo(memberId);
        assertThat(principal.getOrganizationIdRaw()).isEqualTo(orgId);
        assertThat(principal.getUsername()).isEqualTo("alice");
    }

    @DisplayName("Valid token with authorities populates granted authorities")
    @Test
    void validTokenWithAuthorities_populatesGrantedAuthorities() throws Exception {
        String token = jwtService.generateToken(UUID.randomUUID(), UUID.randomUUID(), "bob", List.of("ADMIN"));

        filter.doFilter(requestWithBearer(token), new MockHttpServletResponse(), filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactly("ADMIN");
    }

    @DisplayName("Missing authorization header does not set security context")
    @Test
    void missingAuthorizationHeader_doesNotSetSecurityContext() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();

        filter.doFilter(req, new MockHttpServletResponse(), filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @DisplayName("Invalid token does not set security context and continues chain")
    @Test
    void invalidToken_doesNotSetSecurityContextAndContinuesChain() throws Exception {
        MockHttpServletRequest req = requestWithBearer("not.a.valid.token");
        MockHttpServletResponse res = new MockHttpServletResponse();

        // Should not throw — the filter swallows JwtException and passes the request along
        filter.doFilter(req, res, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @DisplayName("Header without bearer prefix does not set security context")
    @Test
    void headerWithoutBearerPrefix_doesNotSetSecurityContext() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        filter.doFilter(req, new MockHttpServletResponse(), filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
