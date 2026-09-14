package app.prompts.api.application.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import app.prompts.api.domain.model.Role;

public record AddMemberRequest(
        @NotNull UUID memberId,
        @NotNull Role role
) {}
