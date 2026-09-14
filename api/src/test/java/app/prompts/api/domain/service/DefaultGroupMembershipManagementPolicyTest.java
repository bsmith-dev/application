package app.prompts.api.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import app.prompts.api.domain.model.Role;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultGroupMembershipManagementPolicyTest {
    private final GroupMembershipManagementPolicy policy = new DefaultGroupMembershipManagementPolicy();

    @DisplayName("Group lead can manage members")
    @Test
    void groupLeadCanManageMembers() {
        assertTrue(policy.canManageMembers(Role.GROUP_LEAD));
    }

    @DisplayName("Admin can manage members")
    @Test
    void adminCanManageMembers() {
        assertTrue(policy.canManageMembers(Role.ADMIN));
    }

    @DisplayName("Plain member cannot manage members")
    @Test
    void plainMemberCannotManageMembers() {
        assertFalse(policy.canManageMembers(Role.MEMBER));
    }
}
