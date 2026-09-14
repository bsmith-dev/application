package app.prompts.api.application.dto;

import java.util.UUID;

public record CreateOrganizationCommand(UUID organizationId, String name) {
}
