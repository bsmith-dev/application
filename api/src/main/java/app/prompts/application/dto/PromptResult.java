package app.prompts.application.dto;

import java.time.Instant;
import java.util.UUID;

public record PromptResult(UUID promptId, UUID groupId, UUID createdByMemberId, String title, String content,
                            Instant createdAt, Instant updatedAt) {
}
