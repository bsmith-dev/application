package app.prompts.domain.service;

import app.prompts.domain.model.Role;

public interface GroupMembershipManagementPolicy {
    boolean canManageMembers(Role role);
}
