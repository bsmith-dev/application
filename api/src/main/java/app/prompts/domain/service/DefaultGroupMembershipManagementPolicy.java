package app.prompts.domain.service;

import app.prompts.domain.model.Role;

public class DefaultGroupMembershipManagementPolicy implements GroupMembershipManagementPolicy {
    @Override
    public boolean canManageMembers(Role role) {
        return role == Role.GROUP_LEAD || role == Role.ADMIN;
    }
}
