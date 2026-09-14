package app.prompts.api.application.dto;

import java.util.UUID;

public record CreateGroupCommand(String name, UUID organizationId, UUID requesterId) {
}
