package app.prompts.application.port;

import java.util.List;
import java.util.UUID;
import app.prompts.application.dto.AddGroupMemberCommand;
import app.prompts.application.dto.CreateGroupCommand;
import app.prompts.application.dto.GroupMembershipResult;
import app.prompts.application.dto.GroupResult;
import app.prompts.domain.model.Role;

/**
 * Inbound use-case port: group management operations.
 * All operations that target a specific group require the caller's {@code organizationId}
 * so the service can enforce org-scoped isolation and prevent cross-org IDOR.
 */
public interface ManageGroupUseCase {

    GroupResult createGroup(CreateGroupCommand command);

    GroupResult getGroup(UUID groupId, UUID requesterId, UUID organizationId);

    GroupResult renameGroup(UUID groupId, String name, UUID requesterId, UUID organizationId);

    void deleteGroup(UUID groupId, UUID requesterId, UUID organizationId);

    GroupMembershipResult addMember(AddGroupMemberCommand command);

    void removeMember(UUID groupId, UUID targetMemberId, UUID requesterId, UUID organizationId);

    GroupMembershipResult changeMemberRole(UUID groupId, UUID targetMemberId, Role role, UUID requesterId, UUID organizationId);

    List<GroupMembershipResult> listMembers(UUID groupId, UUID requesterId, UUID organizationId);

    List<GroupResult> listGroupsForMember(UUID memberId, UUID organizationId);

    List<GroupResult> listGroups(UUID organizationId);
}
