package app.prompts.application.dto;

import java.util.UUID;
import app.prompts.domain.model.Role;

public record AddGroupMemberCommand(UUID groupId, UUID requesterId, UUID memberId, Role role, UUID organizationId) {
}
