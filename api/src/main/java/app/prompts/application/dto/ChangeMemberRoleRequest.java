package app.prompts.application.dto;

import jakarta.validation.constraints.NotNull;
import app.prompts.domain.model.Role;

public record ChangeMemberRoleRequest(
        @NotNull Role role
) {
}
