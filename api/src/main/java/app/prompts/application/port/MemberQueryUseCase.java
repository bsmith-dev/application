package app.prompts.application.port;

import java.util.List;
import java.util.UUID;
import app.prompts.application.dto.MemberResult;

/**
 * Inbound use-case port: member query operations.
 */
public interface MemberQueryUseCase {

    /**
     * Returns all members belonging to the given organization.
     * Callers must pass the authenticated member's own organization ID;
     * members from other organizations are never returned.
     */
    List<MemberResult> listMembers(UUID organizationId);

    MemberResult getMember(UUID memberId);
}
