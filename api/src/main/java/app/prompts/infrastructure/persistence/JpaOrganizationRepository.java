package app.prompts.infrastructure.persistence;

import java.util.UUID;
import app.prompts.application.port.InfrastructurePortMarker;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaOrganizationRepository extends JpaRepository<JpaOrganizationEntity, UUID>, InfrastructurePortMarker {
}
