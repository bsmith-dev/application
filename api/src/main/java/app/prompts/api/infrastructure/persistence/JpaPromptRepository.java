package app.prompts.api.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import app.prompts.api.application.port.InfrastructurePortMarker;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaPromptRepository extends JpaRepository<JpaPromptEntity, UUID>, InfrastructurePortMarker {
    List<JpaPromptEntity> findByGroupId(UUID groupId);
    void deleteByGroupId(UUID groupId);
}
