package app.prompts.domain.service;

import org.junit.jupiter.api.Test;
import app.prompts.domain.model.GroupId;
import app.prompts.domain.model.GroupMembership;
import app.prompts.domain.model.MemberId;
import app.prompts.domain.model.Role;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultGroupAccessPolicyTest {
    private final GroupAccessPolicy policy = new DefaultGroupAccessPolicy();

    @Test
    void memberOfGroupIsGrantedAccess() {
        GroupId groupId = GroupId.newId();
        MemberId memberId = MemberId.newId();
        List<GroupMembership> memberships = List.of(new GroupMembership(groupId, memberId, Role.MEMBER));

        assertTrue(policy.isMember(groupId, memberId, memberships));
    }

    @Test
    void nonMemberOfGroupIsDeniedAccess() {
        GroupId groupId = GroupId.newId();
        MemberId memberId = MemberId.newId();
        List<GroupMembership> memberships = List.of(new GroupMembership(GroupId.newId(), memberId, Role.MEMBER));

        assertFalse(policy.isMember(groupId, memberId, memberships));
    }
}
