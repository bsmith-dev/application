package app.prompts.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import app.prompts.application.port.InfrastructurePortMarker;
import app.prompts.domain.model.Role;

@Entity
@Table(
    name = "group_memberships",
    uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "member_id"})
)
class JpaGroupMembershipEntity implements InfrastructurePortMarker {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    protected JpaGroupMembershipEntity() {}

    JpaGroupMembershipEntity(UUID id, UUID groupId, UUID memberId, Role role) {
        this.id = id;
        this.groupId = groupId;
        this.memberId = memberId;
        this.role = role;
    }

    UUID getId() { return id; }
    UUID getGroupId() { return groupId; }
    UUID getMemberId() { return memberId; }
    Role getRole() { return role; }
    void setRole(Role role) { this.role = role; }
}
