package app.prompts.application.dto;

import java.util.UUID;
import app.prompts.domain.model.Role;

public record MemberResponse(UUID memberId, UUID organizationId, String username, String email, Role role) {}
