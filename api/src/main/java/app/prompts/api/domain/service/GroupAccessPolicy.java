package app.prompts.api.domain.service;

import java.util.List;
import app.prompts.api.domain.model.GroupId;
import app.prompts.api.domain.model.GroupMembership;
import app.prompts.api.domain.model.MemberId;

public interface GroupAccessPolicy {
    boolean isMember(GroupId groupId, MemberId memberId, List<GroupMembership> memberships);
}
