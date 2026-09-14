package app.prompts.ui.api.dto;

import java.util.UUID;

public record RegisterMemberRequest(
        String username,
        String password,
        String email,
        UUID organizationId
) {
}
