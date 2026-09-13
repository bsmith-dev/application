package app.prompts.application.dto;

import java.util.UUID;
import app.prompts.domain.model.Role;

public record GroupMembershipResponse(UUID groupId, UUID memberId, Role role) {}
