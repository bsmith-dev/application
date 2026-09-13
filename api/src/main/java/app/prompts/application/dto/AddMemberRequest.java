package app.prompts.application.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import app.prompts.domain.model.Role;

public record AddMemberRequest(
        @NotNull UUID memberId,
        @NotNull Role role
) {}
