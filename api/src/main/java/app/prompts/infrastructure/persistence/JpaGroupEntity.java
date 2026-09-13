package app.prompts.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import app.prompts.application.port.InfrastructurePortMarker;

@Entity
@Table(name = "groups")
class JpaGroupEntity implements InfrastructurePortMarker {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(nullable = false, unique = true)
    private String name;

    protected JpaGroupEntity() {}

    JpaGroupEntity(UUID id, UUID organizationId, String name) {
        this.id = id;
        this.organizationId = organizationId;
        this.name = name;
    }

    UUID getId() { return id; }
    UUID getOrganizationId() { return organizationId; }
    String getName() { return name; }
    void setName(String name) { this.name = name; }
}
