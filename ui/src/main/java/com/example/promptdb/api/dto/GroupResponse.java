package com.example.promptdb.api.dto;

import java.util.UUID;

public record GroupResponse(
        UUID groupId,
        UUID organizationId,
        String name
) {
}
