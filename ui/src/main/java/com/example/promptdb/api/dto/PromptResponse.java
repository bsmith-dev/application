package com.example.promptdb.api.dto;

import java.time.Instant;
import java.util.UUID;

public record PromptResponse(
        UUID id,
        UUID groupId,
        UUID createdBy,
        String title,
        String content,
        Instant createdAt,
        Instant updatedAt
) {
}
