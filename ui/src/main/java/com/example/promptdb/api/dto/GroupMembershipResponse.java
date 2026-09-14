package com.example.promptdb.api.dto;

import java.util.UUID;

public record GroupMembershipResponse(
        UUID groupId,
        UUID memberId,
        Role role
) {
}
