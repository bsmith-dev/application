package app.prompts.api.application.dto;

import java.util.UUID;

public record GroupResult(UUID groupId, UUID organizationId, String name) {
}
