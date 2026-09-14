package app.prompts.api.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import app.prompts.api.application.port.InfrastructurePortMarker;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaGroupRepository extends JpaRepository<JpaGroupEntity, UUID>, InfrastructurePortMarker {
    boolean existsByName(String name);

    Optional<JpaGroupEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
