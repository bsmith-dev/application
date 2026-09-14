package app.prompts.api.application.dto;

import jakarta.validation.constraints.NotNull;
import app.prompts.api.domain.model.Role;

public record ChangeMemberRoleRequest(
        @NotNull Role role
) {
}
