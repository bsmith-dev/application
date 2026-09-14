package com.example.promptdb.api.dto;

import java.util.UUID;

public record MemberResponse(
        UUID memberId,
        UUID organizationId,
        String username,
        String email,
        Role role
) {
}
