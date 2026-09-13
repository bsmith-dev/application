package app.prompts.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import app.prompts.application.port.GroupMembershipRepository;
import app.prompts.domain.model.GroupId;
import app.prompts.domain.model.GroupMembership;
import app.prompts.domain.model.MemberId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class GroupMembershipPersistenceAdapter implements GroupMembershipRepository {

    private final JpaGroupMembershipRepository jpaGroupMembershipRepository;

    GroupMembershipPersistenceAdapter(JpaGroupMembershipRepository jpaGroupMembershipRepository) {
        this.jpaGroupMembershipRepository = jpaGroupMembershipRepository;
    }

    @Override
    public GroupMembership save(GroupMembership membership) {
        // Upsert: check for existing row to preserve generated ID
        JpaGroupMembershipEntity existing = jpaGroupMembershipRepository
                .findByGroupIdAndMemberId(membership.groupId().value(), membership.memberId().value())
                .orElse(null);
        JpaGroupMembershipEntity entity = existing != null
                ? existing
                : PromptsPersistenceMapper.toEntity(membership);
        entity.setRole(membership.role());
        JpaGroupMembershipEntity saved = jpaGroupMembershipRepository.save(entity);
        return PromptsPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<GroupMembership> findByGroupIdAndMemberId(GroupId groupId, MemberId memberId) {
        return jpaGroupMembershipRepository
                .findByGroupIdAndMemberId(groupId.value(), memberId.value())
                .map(PromptsPersistenceMapper::toDomain);
    }

    @Override
    public List<GroupMembership> findByGroupId(GroupId groupId) {
        return jpaGroupMembershipRepository.findByGroupId(groupId.value()).stream()
                .map(PromptsPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<GroupMembership> findByMemberId(MemberId memberId) {
        return jpaGroupMembershipRepository.findByMemberId(memberId.value()).stream()
                .map(PromptsPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteByGroupIdAndMemberId(GroupId groupId, MemberId memberId) {
        jpaGroupMembershipRepository.deleteByGroupIdAndMemberId(groupId.value(), memberId.value());
    }

    @Override
    @Transactional
    public void deleteByGroupId(GroupId groupId) {
        jpaGroupMembershipRepository.deleteByGroupId(groupId.value());
    }
}
