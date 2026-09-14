package app.prompts.api.application.dto;

import java.util.UUID;
import app.prompts.api.domain.model.Role;

public record GroupMembershipResponse(UUID groupId, UUID memberId, Role role) {}
