package app.prompts.domain.service;

import java.util.List;
import app.prompts.domain.model.GroupId;
import app.prompts.domain.model.GroupMembership;
import app.prompts.domain.model.MemberId;

public interface GroupAccessPolicy {
    boolean isMember(GroupId groupId, MemberId memberId, List<GroupMembership> memberships);
}
