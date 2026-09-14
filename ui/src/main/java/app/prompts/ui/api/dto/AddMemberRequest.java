package app.prompts.ui.api.dto;

import java.util.UUID;

public record AddMemberRequest(UUID memberId, Role role) {
}
