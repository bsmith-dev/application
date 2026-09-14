package app.prompts.api.application.dto;

import java.util.UUID;

public record RenameOrganizationCommand(UUID organizationId, String name, UUID requesterId) {
}
