package app.prompts.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;
import app.prompts.application.port.InfrastructurePortMarker;
import app.prompts.domain.model.Group;
import app.prompts.domain.model.GroupId;
import app.prompts.domain.model.GroupMembership;
import app.prompts.domain.model.Member;
import app.prompts.domain.model.MemberId;
import app.prompts.domain.model.Organization;
import app.prompts.domain.model.OrganizationId;
import app.prompts.domain.model.Prompt;
import app.prompts.domain.model.PromptId;

/**
 * Package-private mapper: translates between domain objects and JPA entities.
 * Lives in infrastructure — domain objects have zero knowledge of this class.
 */
final class PromptsPersistenceMapper implements InfrastructurePortMarker {

    private PromptsPersistenceMapper() {}

    // ── Group ────────────────────────────────────────────────────────────────

    static JpaGroupEntity toEntity(Group group) {
        UUID orgId = group.organizationId() == null ? null : group.organizationId().value();
        return new JpaGroupEntity(group.id().value(), orgId, group.name());
    }

    static Group toDomain(JpaGroupEntity entity) {
        OrganizationId orgId = entity.getOrganizationId() == null
                ? null
                : new OrganizationId(entity.getOrganizationId());
        return new Group(new GroupId(entity.getId()), orgId, entity.getName());
    }

    // ── Organization ─────────────────────────────────────────────────────────

    static JpaOrganizationEntity toEntity(Organization organization) {
        return new JpaOrganizationEntity(organization.id().value(), organization.name(), Instant.now());
    }

    static Organization toDomain(JpaOrganizationEntity entity) {
        return new Organization(new OrganizationId(entity.getId()), entity.getName());
    }

    // ── Member ───────────────────────────────────────────────────────────────

    static JpaMemberEntity toEntity(Member member, String passwordHash, Instant createdAt) {
        return new JpaMemberEntity(
                member.id().value(),
                member.organizationId().value(),
                member.username(),
                passwordHash,
                member.email(),
                createdAt);
    }

    static Member toDomain(JpaMemberEntity entity) {
        return new Member(
                new MemberId(entity.getId()),
                new OrganizationId(entity.getOrganizationId()),
                entity.getUsername(),
                entity.getEmail());
    }

    // ── GroupMembership ──────────────────────────────────────────────────────

    static JpaGroupMembershipEntity toEntity(GroupMembership membership) {
        return new JpaGroupMembershipEntity(
                UUID.randomUUID(),
                membership.groupId().value(),
                membership.memberId().value(),
                membership.role()
        );
    }

    static GroupMembership toDomain(JpaGroupMembershipEntity entity) {
        return new GroupMembership(
                new GroupId(entity.getGroupId()),
                new MemberId(entity.getMemberId()),
                entity.getRole()
        );
    }

    // ── Prompt ───────────────────────────────────────────────────────────────

    static JpaPromptEntity toEntity(Prompt prompt) {
        return new JpaPromptEntity(
                prompt.id().value(),
                prompt.groupId().value(),
                prompt.createdByMemberId().value(),
                prompt.title(),
                prompt.content(),
                prompt.createdAt(),
                prompt.updatedAt()
        );
    }

    static Prompt toDomain(JpaPromptEntity entity) {
        return new Prompt(
                new PromptId(entity.getId()),
                new GroupId(entity.getGroupId()),
                new MemberId(entity.getCreatedByMemberId()),
                entity.getTitle(),
                entity.getContent(),
                entity.getCreatedAt()
        );
    }
}
