package app.prompts.api.application.dto;

import java.util.UUID;

public record UpdatePromptCommand(UUID groupId, UUID promptId, UUID requesterId, String title, String content) {
}
