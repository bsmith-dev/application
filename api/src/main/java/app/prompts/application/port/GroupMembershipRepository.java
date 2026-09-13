package app.prompts.application.port;

import java.util.List;
import java.util.Optional;
import app.prompts.domain.model.GroupId;
import app.prompts.domain.model.GroupMembership;
import app.prompts.domain.model.MemberId;

public interface GroupMembershipRepository {
    GroupMembership save(GroupMembership membership);

    Optional<GroupMembership> findByGroupIdAndMemberId(GroupId groupId, MemberId memberId);

    List<GroupMembership> findByGroupId(GroupId groupId);

    List<GroupMembership> findByMemberId(MemberId memberId);

    void deleteByGroupIdAndMemberId(GroupId groupId, MemberId memberId);

    void deleteByGroupId(GroupId groupId);
}
