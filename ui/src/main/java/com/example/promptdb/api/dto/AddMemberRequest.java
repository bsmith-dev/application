package com.example.promptdb.api.dto;

import java.util.UUID;

public record AddMemberRequest(UUID memberId, Role role) {
}
