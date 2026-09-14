package app.prompts.api.domain.service;

import app.prompts.api.domain.model.Role;

public interface GroupMembershipManagementPolicy {
    boolean canManageMembers(Role role);
}
