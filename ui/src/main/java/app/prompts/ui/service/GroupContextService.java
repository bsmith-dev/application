package app.prompts.ui.service;

import app.prompts.ui.api.GroupMembershipApiClient;
import app.prompts.ui.api.dto.GroupMembershipResponse;
import app.prompts.ui.api.dto.MemberResponse;
import app.prompts.ui.api.dto.Role;
import app.prompts.ui.security.CurrentApiSession;
import app.prompts.ui.support.GroupPermissions;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Resolves the current member's membership role within a specific group and the resulting UI permissions. */
@Service
public class GroupContextService {

    private final GroupMembershipApiClient groupMembershipApiClient;
    private final CurrentApiSession currentApiSession;

    public GroupContextService(GroupMembershipApiClient groupMembershipApiClient, CurrentApiSession currentApiSession) {
        this.groupMembershipApiClient = groupMembershipApiClient;
        this.currentApiSession = currentApiSession;
    }

    public List<GroupMembershipResponse> members(UUID groupId) {
        return groupMembershipApiClient.listGroupMembers(groupId);
    }

    public Role membershipRole(UUID groupId) {
        MemberResponse currentMember = currentApiSession.currentMember().orElse(null);
        if (currentMember == null) {
            return null;
        }
        return members(groupId).stream()
                .filter(membership -> currentMember.memberId().equals(membership.memberId()))
                .map(GroupMembershipResponse::role)
                .findFirst()
                .orElse(null);
    }

    public GroupPermissions permissions(UUID groupId) {
        MemberResponse currentMember = currentApiSession.currentMember().orElse(null);
        return GroupPermissions.of(currentMember, membershipRole(groupId));
    }
}
