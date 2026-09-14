package app.prompts.api.application.port;

import java.util.UUID;

public record MemberCredentials(UUID memberId, UUID organizationId, String username, String passwordHash) {}
