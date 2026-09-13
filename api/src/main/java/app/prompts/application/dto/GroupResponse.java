package app.prompts.application.dto;

import java.util.UUID;

public record GroupResponse(UUID groupId, UUID organizationId, String name) {}
