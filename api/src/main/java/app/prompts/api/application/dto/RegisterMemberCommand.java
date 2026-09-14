package app.prompts.api.application.dto;

import java.util.UUID;

/**
 * Command to register a new member.
 * {@code rawPassword} is the plaintext password; hashing is the responsibility
 * of the infrastructure adapter that implements {@link app.prompts.api.application.port.MemberRepository}.
 */
public record RegisterMemberCommand(UUID organizationId, String username, String rawPassword, String email) {
}
