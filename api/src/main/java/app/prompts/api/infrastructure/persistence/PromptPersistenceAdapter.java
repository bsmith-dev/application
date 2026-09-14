package app.prompts.api.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import app.prompts.api.application.port.PromptRepository;
import app.prompts.api.domain.model.GroupId;
import app.prompts.api.domain.model.Prompt;
import app.prompts.api.domain.model.PromptId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class PromptPersistenceAdapter implements PromptRepository {

    private final JpaPromptRepository jpaPromptRepository;

    PromptPersistenceAdapter(JpaPromptRepository jpaPromptRepository) {
        this.jpaPromptRepository = jpaPromptRepository;
    }

    @Override
    public Prompt save(Prompt prompt) {
        JpaPromptEntity entity = PromptsPersistenceMapper.toEntity(prompt);
        JpaPromptEntity saved = jpaPromptRepository.save(entity);
        return PromptsPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Prompt> findById(PromptId id) {
        return jpaPromptRepository.findById(id.value())
                .map(PromptsPersistenceMapper::toDomain);
    }

    @Override
    public List<Prompt> findByGroupId(GroupId groupId) {
        return jpaPromptRepository.findByGroupId(groupId.value()).stream()
                .map(PromptsPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(PromptId id) {
        jpaPromptRepository.deleteById(id.value());
    }

    @Override
    @Transactional
    public void deleteByGroupId(GroupId groupId) {
        jpaPromptRepository.deleteByGroupId(groupId.value());
    }
}
