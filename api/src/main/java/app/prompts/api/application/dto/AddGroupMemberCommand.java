package app.prompts.api.application.dto;

import java.util.UUID;
import app.prompts.api.domain.model.Role;

public record AddGroupMemberCommand(UUID groupId, UUID requesterId, UUID memberId, Role role, UUID organizationId) {
}
