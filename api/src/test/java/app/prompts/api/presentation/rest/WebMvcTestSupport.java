package app.prompts.api.presentation.rest;

import app.prompts.api.infrastructure.security.JwtProperties;
import app.prompts.api.infrastructure.security.JwtService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.UUID;

/**
 * Shared test infrastructure for {@code @WebMvcTest} slices.
 *
 * <p>Provides a real {@link JwtService} (with a fixed test secret) and the
 * {@link JwtProperties} configuration bean so the {@code JwtAuthFilter}
 * can be wired into the security filter chain during slice tests.
 *
 * <p>Also exposes {@link #bearerTokenFor} to generate valid JWTs inside tests.
 */
@Import(WebMvcTestSupport.Config.class)
public class WebMvcTestSupport {

    public static final String TEST_SECRET = "test-secret-key-at-least-32-bytes!!";
    public static final long   TEST_EXPIRY  = 3600L;

    /** Generates a bearer-prefixed Authorization header value for the given member. */
    public static String bearerTokenFor(JwtService jwtService, UUID memberId, UUID organizationId,
                                        String username, List<String> authorities) {
        return "Bearer " + jwtService.generateToken(memberId, organizationId, username, authorities);
    }

    @TestConfiguration
    static class Config {
        @Bean
        JwtProperties jwtProperties() {
            return new JwtProperties(TEST_SECRET, TEST_EXPIRY);
        }

        @Bean
        JwtService jwtService(JwtProperties jwtProperties) {
            return new JwtService(jwtProperties);
        }
    }
}
