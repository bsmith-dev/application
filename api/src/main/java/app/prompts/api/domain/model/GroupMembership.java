package app.prompts.api.domain.model;

public record GroupMembership(GroupId groupId, MemberId memberId, Role role) {
    public GroupMembership {
        if (groupId == null) {
            throw new IllegalArgumentException("Group is required");
        }
        if (memberId == null) {
            throw new IllegalArgumentException("Member is required");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role is required");
        }
    }

    public boolean hasRole(Role candidate) {
        return this.role == candidate;
    }
}
