package app.prompts.api.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import app.prompts.api.application.port.GroupRepository;
import app.prompts.api.domain.model.Group;
import app.prompts.api.domain.model.GroupId;
import app.prompts.api.domain.model.OrganizationId;
import org.springframework.stereotype.Component;

@Component
class GroupPersistenceAdapter implements GroupRepository {

    private final JpaGroupRepository jpaGroupRepository;

    GroupPersistenceAdapter(JpaGroupRepository jpaGroupRepository) {
        this.jpaGroupRepository = jpaGroupRepository;
    }

    @Override
    public Group save(Group group) {
        JpaGroupEntity entity = PromptsPersistenceMapper.toEntity(group);
        JpaGroupEntity saved = jpaGroupRepository.save(entity);
        return PromptsPersistenceMapper.toDomain(saved);
    }

    @Override
    public List<Group> findAll() {
        return jpaGroupRepository.findAll().stream()
                .map(PromptsPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Group> findById(GroupId id) {
        return jpaGroupRepository.findById(id.value())
                .map(PromptsPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Group> findByIdAndOrganizationId(GroupId id, OrganizationId organizationId) {
        return jpaGroupRepository
                .findByIdAndOrganizationId(id.value(), organizationId.value())
                .map(PromptsPersistenceMapper::toDomain);
    }

    @Override
    public void deleteById(GroupId id) {
        jpaGroupRepository.deleteById(id.value());
    }
}
