package app.prompts.ui.api.dto;

import java.util.UUID;

public record GroupMembershipResponse(
        UUID groupId,
        UUID memberId,
        Role role
) {
}
