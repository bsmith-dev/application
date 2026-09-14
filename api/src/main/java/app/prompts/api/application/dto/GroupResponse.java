package app.prompts.api.application.dto;

import java.util.UUID;

public record GroupResponse(UUID groupId, UUID organizationId, String name) {}
