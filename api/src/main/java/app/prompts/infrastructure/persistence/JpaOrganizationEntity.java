package app.prompts.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import app.prompts.application.port.InfrastructurePortMarker;

@Entity
@Table(name = "organizations")
class JpaOrganizationEntity implements InfrastructurePortMarker {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected JpaOrganizationEntity() {}

    JpaOrganizationEntity(UUID id, String name, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    UUID getId() { return id; }
    String getName() { return name; }
    void setName(String name) { this.name = name; }
    Instant getCreatedAt() { return createdAt; }
}
