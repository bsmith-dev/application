package app.prompts.application.port;

import java.util.List;
import java.util.UUID;
import app.prompts.application.dto.CreatePromptCommand;
import app.prompts.application.dto.PromptResult;
import app.prompts.application.dto.UpdatePromptCommand;

/**
 * Inbound use-case port: prompt CRUD operations.
 */
public interface ManagePromptUseCase {

    PromptResult createPrompt(CreatePromptCommand command);

    List<PromptResult> listPrompts(UUID groupId, UUID requesterId);

    PromptResult getPrompt(UUID groupId, UUID promptId, UUID requesterId);

    PromptResult updatePrompt(UpdatePromptCommand command);

    void deletePrompt(UUID groupId, UUID promptId, UUID requesterId);
}
