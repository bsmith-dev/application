package app.prompts.api.domain.service;

import app.prompts.api.domain.model.Role;

public class DefaultGroupMembershipManagementPolicy implements GroupMembershipManagementPolicy {
    @Override
    public boolean canManageMembers(Role role) {
        return role == Role.GROUP_LEAD || role == Role.ADMIN;
    }
}
