package app.prompts.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import app.prompts.application.port.InfrastructurePortMarker;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaMemberRepository extends JpaRepository<JpaMemberEntity, UUID>, InfrastructurePortMarker {
    Optional<JpaMemberEntity> findByUsername(String username);
    List<JpaMemberEntity> findByOrganizationId(UUID organizationId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
