package app.prompts.api.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import app.prompts.api.application.port.OrganizationRepository;
import app.prompts.api.domain.model.Organization;
import app.prompts.api.domain.model.OrganizationId;
import org.springframework.stereotype.Component;

@Component
class OrganizationPersistenceAdapter implements OrganizationRepository {

    private final JpaOrganizationRepository jpaOrganizationRepository;

    OrganizationPersistenceAdapter(JpaOrganizationRepository jpaOrganizationRepository) {
        this.jpaOrganizationRepository = jpaOrganizationRepository;
    }

    @Override
    public Organization save(Organization organization) {
        JpaOrganizationEntity entity = PromptsPersistenceMapper.toEntity(organization);
        JpaOrganizationEntity saved = jpaOrganizationRepository.save(entity);
        return PromptsPersistenceMapper.toDomain(saved);
    }

    @Override
    public List<Organization> findAll() {
        return jpaOrganizationRepository.findAll().stream()
                .map(PromptsPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Organization> findById(OrganizationId id) {
        return jpaOrganizationRepository.findById(id.value())
                .map(PromptsPersistenceMapper::toDomain);
    }

    @Override
    public void deleteById(OrganizationId id) {
        jpaOrganizationRepository.deleteById(id.value());
    }
}
