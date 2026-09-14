package app.prompts.api.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import app.prompts.api.domain.model.GroupId;
import app.prompts.api.domain.model.GroupMembership;
import app.prompts.api.domain.model.MemberId;
import app.prompts.api.domain.model.Role;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultGroupAccessPolicyTest {
    private final GroupAccessPolicy policy = new DefaultGroupAccessPolicy();

    @DisplayName("Member of group is granted access")
    @Test
    void memberOfGroupIsGrantedAccess() {
        GroupId groupId = GroupId.newId();
        MemberId memberId = MemberId.newId();
        List<GroupMembership> memberships = List.of(new GroupMembership(groupId, memberId, Role.MEMBER));

        assertTrue(policy.isMember(groupId, memberId, memberships));
    }

    @DisplayName("Non-member of group is denied access")
    @Test
    void nonMemberOfGroupIsDeniedAccess() {
        GroupId groupId = GroupId.newId();
        MemberId memberId = MemberId.newId();
        List<GroupMembership> memberships = List.of(new GroupMembership(GroupId.newId(), memberId, Role.MEMBER));

        assertFalse(policy.isMember(groupId, memberId, memberships));
    }
}
