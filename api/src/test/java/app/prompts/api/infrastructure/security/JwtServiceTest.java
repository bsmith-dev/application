package app.prompts.api.infrastructure.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    // 32+ character secret satisfies @Size(min=32) validation
    private static final String TEST_SECRET = "test-secret-key-at-least-32-bytes!!";
    private static final long EXPIRATION_SECONDS = 3600L;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties(TEST_SECRET, EXPIRATION_SECONDS);
        jwtService = new JwtService(props);
    }

    @DisplayName("Generated token preserves member ID, organization ID, and username")
    @Test
    void generateToken_roundTrip_memberIdAndUsernameRoundTrip() {
        UUID memberId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        String username = "alice";

        String token = jwtService.generateToken(memberId, orgId, username, List.of());

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractMemberId(token)).isEqualTo(memberId);
        assertThat(jwtService.extractOrganizationId(token)).isEqualTo(orgId);
        assertThat(jwtService.parseToken(token).get("username", String.class)).isEqualTo(username);
    }

    @DisplayName("Generate token authorities claim round trip")
    @Test
    void generateToken_authoritiesClaimRoundTrip() {
        UUID memberId = UUID.randomUUID();
        List<String> authorities = List.of("ADMIN", "GROUP_LEAD");

        String token = jwtService.generateToken(memberId, UUID.randomUUID(), "bob", authorities);

        assertThat(jwtService.extractAuthorities(token)).containsExactlyInAnyOrder("ADMIN", "GROUP_LEAD");
    }

    @DisplayName("Generated token without authorities extracts an empty authority list")
    @Test
    void generateToken_emptyAuthorities_extractReturnsEmpty() {
        String token = jwtService.generateToken(UUID.randomUUID(), UUID.randomUUID(), "carol", List.of());

        assertThat(jwtService.extractAuthorities(token)).isEmpty();
    }

    @DisplayName("Generate token sets issuer and audience")
    @Test
    void generateToken_setsIssuerAndAudience() {
        String token = jwtService.generateToken(UUID.randomUUID(), UUID.randomUUID(), "dave", List.of());

        // parseToken validates issuer + audience internally; if it doesn't throw the claims are correct
        var claims = jwtService.parseToken(token);
        assertThat(claims.getIssuer()).isEqualTo("prompt-db");
        assertThat(claims.getAudience()).contains("prompt-db-api");
    }

    @DisplayName("Parsing an expired token throws a JWT exception")
    @Test
    void parseToken_expiredToken_throwsJwtException() {
        // Build an already-expired token directly using jjwt, signed with the same key
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .issuer("prompt-db")
                .audience().add("prompt-db-api").and()
                .subject(UUID.randomUUID().toString())
                .issuedAt(Date.from(Instant.now().minusSeconds(3600)))
                .expiration(Date.from(Instant.now().minusSeconds(10)))
                .signWith(key)
                .compact();

        assertThatThrownBy(() -> parseToken(expiredToken))
                .isInstanceOf(JwtException.class);
    }

    @DisplayName("Parsing a token with the wrong issuer throws a JWT exception")
    @Test
    void parseToken_wrongIssuer_throwsJwtException() {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        String badIssuerToken = Jwts.builder()
                .issuer("evil-issuer")
                .audience().add("prompt-db-api").and()
                .subject(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(key)
                .compact();

        assertThatThrownBy(() -> parseToken(badIssuerToken))
                .isInstanceOf(JwtException.class);
    }

    @DisplayName("Parsing a token with the wrong audience throws a JWT exception")
    @Test
    void parseToken_wrongAudience_throwsJwtException() {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        String badAudienceToken = Jwts.builder()
                .issuer("prompt-db")
                .audience().add("other-service").and()
                .subject(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(key)
                .compact();

        assertThatThrownBy(() -> parseToken(badAudienceToken))
                .isInstanceOf(JwtException.class);
    }

    @DisplayName("Parsing a token with a tampered signature throws a JWT exception")
    @Test
    void parseToken_tamperedSignature_throwsJwtException() {
        String token = jwtService.generateToken(UUID.randomUUID(), UUID.randomUUID(), "eve", List.of());
        // Corrupt the last character of the signature section
        String tampered = token.substring(0, token.length() - 4) + "XXXX";

        assertThatThrownBy(() -> parseToken(tampered))
                .isInstanceOf(JwtException.class);
    }

    @DisplayName("Extract member ID returns correct UUID")
    @Test
    void extractMemberId_returnsCorrectUuid() {
        UUID memberId = UUID.randomUUID();
        String token = jwtService.generateToken(memberId, UUID.randomUUID(), "frank", List.of());

        assertThat(jwtService.extractMemberId(token)).isEqualTo(memberId);
    }

    @DisplayName("Extract organization ID returns correct UUID")
    @Test
    void extractOrganizationId_returnsCorrectUuid() {
        UUID orgId = UUID.randomUUID();
        String token = jwtService.generateToken(UUID.randomUUID(), orgId, "grace", List.of());

        assertThat(jwtService.extractOrganizationId(token)).isEqualTo(orgId);
    }

    @DisplayName("Extracting authorities from a token without the claim returns empty")
    @Test
    void extractAuthorities_missingClaim_returnsEmpty() {
        // Token built without an authorities claim
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .issuer("prompt-db")
                .audience().add("prompt-db-api").and()
                .subject(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(key)
                .compact();

        assertThat(jwtService.extractAuthorities(token)).isEmpty();
    }

    private io.jsonwebtoken.Claims parseToken(String token) {
        return jwtService.parseToken(token);
    }
}
