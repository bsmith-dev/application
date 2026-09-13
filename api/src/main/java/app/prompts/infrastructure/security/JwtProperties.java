package app.prompts.infrastructure.security;

import app.prompts.application.port.InfrastructurePortMarker;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.security.jwt")
public record JwtProperties(
        @NotBlank @Size(min = 32) String secret,
        long expirationSeconds
) implements InfrastructurePortMarker {
    public JwtProperties {
        if (expirationSeconds <= 0) {
            throw new IllegalArgumentException("Expiration must be > 0");
        }
    }
}
