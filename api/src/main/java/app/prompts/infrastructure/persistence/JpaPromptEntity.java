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
@Table(name = "prompts")
class JpaPromptEntity implements InfrastructurePortMarker {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "created_by_member_id", nullable = false)
    private UUID createdByMemberId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected JpaPromptEntity() {}

    JpaPromptEntity(UUID id, UUID groupId, UUID createdByMemberId,
                    String title, String content, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.groupId = groupId;
        this.createdByMemberId = createdByMemberId;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    UUID getId() { return id; }
    UUID getGroupId() { return groupId; }
    UUID getCreatedByMemberId() { return createdByMemberId; }
    String getTitle() { return title; }
    void setTitle(String title) { this.title = title; }
    String getContent() { return content; }
    void setContent(String content) { this.content = content; }
    Instant getCreatedAt() { return createdAt; }
    Instant getUpdatedAt() { return updatedAt; }
    void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
