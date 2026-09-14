package app.prompts.ui.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * This application does not use Spring Security's authentication model - the prompt_db API is
 * the single source of truth for identity, and the JWT it issues is held server-side in the
 * HTTP session (see {@link CurrentApiSession}). Spring Security is used only for:
 *  - CSRF token issuance/validation on every state-changing form post
 *  - standard security response headers
 * Route-level "must be logged in" / "must be ADMIN" gating is done by {@link AuthInterceptor}
 * and the page controllers themselves, which call the real API and let 401/403 responses drive
 * the UI - client-side gating here would just be a usability shortcut, not real enforcement.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();

        http
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(csrfHandler)
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable());

        return http.build();
    }
}
