package app.prompts.domain.service;

import java.util.List;
import app.prompts.domain.model.GroupId;
import app.prompts.domain.model.GroupMembership;
import app.prompts.domain.model.MemberId;

public class DefaultGroupAccessPolicy implements GroupAccessPolicy {
    @Override
    public boolean isMember(GroupId groupId, MemberId memberId, List<GroupMembership> memberships) {
        return memberships.stream()
                .anyMatch(m -> m.groupId().equals(groupId) && m.memberId().equals(memberId));
    }
}
