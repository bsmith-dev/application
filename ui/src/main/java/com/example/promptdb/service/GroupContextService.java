package com.example.promptdb.service;

import com.example.promptdb.api.GroupMembershipApiClient;
import com.example.promptdb.api.dto.GroupMembershipResponse;
import com.example.promptdb.api.dto.MemberResponse;
import com.example.promptdb.api.dto.Role;
import com.example.promptdb.security.CurrentApiSession;
import com.example.promptdb.support.GroupPermissions;
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
