package app.prompts.api.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import app.prompts.api.application.port.InfrastructurePortMarker;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaGroupMembershipRepository extends JpaRepository<JpaGroupMembershipEntity, UUID>, InfrastructurePortMarker {
    Optional<JpaGroupMembershipEntity> findByGroupIdAndMemberId(UUID groupId, UUID memberId);
    List<JpaGroupMembershipEntity> findByGroupId(UUID groupId);
    List<JpaGroupMembershipEntity> findByMemberId(UUID memberId);
    void deleteByGroupIdAndMemberId(UUID groupId, UUID memberId);
    void deleteByGroupId(UUID groupId);
}
