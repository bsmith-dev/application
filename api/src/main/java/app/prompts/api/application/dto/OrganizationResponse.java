package app.prompts.api.application.dto;

import java.util.UUID;

public record OrganizationResponse(UUID organizationId, String name) {
}
