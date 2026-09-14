package app.prompts.api.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import app.prompts.api.application.port.TokenService;

public class JwtService implements TokenService {
    private static final String ISSUER = "prompt-db";
    private static final String AUDIENCE = "prompt-db-api";

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateToken(UUID memberId, UUID organizationId, String username, List<String> authorities) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(ISSUER)
                .audience().add(AUDIENCE).and()
                .subject(memberId.toString())
                .claim("username", username)
                .claim("memberId", memberId.toString())
                .claim("organizationId", organizationId.toString())
                .claim("authorities", authorities)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(properties.expirationSeconds())))
                .signWith(signingKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(ISSUER)
                .requireAudience(AUDIENCE)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractMemberId(String token) {
        return UUID.fromString(parseToken(token).getSubject());
    }

    public UUID extractOrganizationId(String token) {
        return UUID.fromString(parseToken(token).get("organizationId", String.class));
    }

    public List<String> extractAuthorities(String token) {
        Object authorities = parseToken(token).get("authorities");
        if (authorities instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    public String extractUsername(String token) {
        return parseToken(token).get("username", String.class);
    }
}
