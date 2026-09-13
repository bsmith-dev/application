package app.prompts.domain.service;

import org.junit.jupiter.api.Test;
import app.prompts.domain.model.Role;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultGroupMembershipManagementPolicyTest {
    private final GroupMembershipManagementPolicy policy = new DefaultGroupMembershipManagementPolicy();

    @Test
    void groupLeadCanManageMembers() {
        assertTrue(policy.canManageMembers(Role.GROUP_LEAD));
    }

    @Test
    void adminCanManageMembers() {
        assertTrue(policy.canManageMembers(Role.ADMIN));
    }

    @Test
    void plainMemberCannotManageMembers() {
        assertFalse(policy.canManageMembers(Role.MEMBER));
    }
}
