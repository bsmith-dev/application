package app.prompts.api.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import app.prompts.api.application.port.InfrastructurePortMarker;

@Entity
@Table(name = "members")
class JpaMemberEntity implements InfrastructurePortMarker {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected JpaMemberEntity() {}

    JpaMemberEntity(UUID id, UUID organizationId, String username, String passwordHash, String email, Instant createdAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.createdAt = createdAt;
    }

    UUID getId() { return id; }
    UUID getOrganizationId() { return organizationId; }
    String getUsername() { return username; }
    void setUsername(String username) { this.username = username; }
    String getPasswordHash() { return passwordHash; }
    String getEmail() { return email; }
    void setEmail(String email) { this.email = email; }
    Instant getCreatedAt() { return createdAt; }
}
