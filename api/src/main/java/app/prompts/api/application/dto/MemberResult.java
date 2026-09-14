package app.prompts.api.application.dto;

import java.util.UUID;
import app.prompts.api.domain.model.Role;

public record MemberResult(UUID memberId, UUID organizationId, String username, String email, Role role) {
}
