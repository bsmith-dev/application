package app.prompts.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupMembershipTest {
    @Test
    void missingRoleThrows() {
        GroupId groupId = GroupId.newId();
        MemberId memberId = MemberId.newId();

        assertThrows(IllegalArgumentException.class,
                () -> createMembership(groupId, memberId, null));
    }

    @Test
    void missingGroupIdThrows() {
        MemberId memberId = MemberId.newId();

        assertThrows(IllegalArgumentException.class,
                () -> createMembership(null, memberId, Role.MEMBER));
    }

    @Test
    void missingMemberIdThrows() {
        GroupId groupId = GroupId.newId();

        assertThrows(IllegalArgumentException.class,
                () -> createMembership(groupId, null, Role.MEMBER));
    }

    @Test
    void hasRoleMatchesAssignedRole() {
        GroupMembership membership = new GroupMembership(GroupId.newId(), MemberId.newId(), Role.GROUP_LEAD);
        assertTrue(membership.hasRole(Role.GROUP_LEAD));
        assertFalse(membership.hasRole(Role.MEMBER));
    }

    private static GroupMembership createMembership(GroupId groupId, MemberId memberId, Role role) {
        return new GroupMembership(groupId, memberId, role);
    }
}
