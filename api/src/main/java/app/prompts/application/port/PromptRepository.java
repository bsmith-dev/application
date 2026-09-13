package app.prompts.application.port;

import java.util.List;
import java.util.Optional;
import app.prompts.domain.model.GroupId;
import app.prompts.domain.model.Prompt;
import app.prompts.domain.model.PromptId;

public interface PromptRepository {
    Prompt save(Prompt prompt);

    Optional<Prompt> findById(PromptId id);

    List<Prompt> findByGroupId(GroupId groupId);

    void deleteById(PromptId id);

    void deleteByGroupId(GroupId groupId);
}
