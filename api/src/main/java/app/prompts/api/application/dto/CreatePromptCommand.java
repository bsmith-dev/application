package app.prompts.api.application.dto;

import java.util.UUID;

public record CreatePromptCommand(UUID groupId, UUID createdByMemberId, String title, String content) {
}
