package app.prompts.api.infrastructure.security;

import java.util.UUID;
import app.prompts.api.application.port.GroupMembershipRepository;
import app.prompts.api.domain.model.GroupId;
import app.prompts.api.domain.model.MemberId;
import org.springframework.security.core.Authentication;

public class GroupAccessService {
    private final GroupMembershipRepository groupMembershipRepository;

    public GroupAccessService(GroupMembershipRepository groupMembershipRepository) {
        this.groupMembershipRepository = groupMembershipRepository;
    }

    public boolean isMember(UUID groupId, Object principal) {
        if (!(principal instanceof MemberUserDetails details)) {
            return false;
        }
        return groupMembershipRepository.findByGroupIdAndMemberId(new GroupId(groupId), new MemberId(details.getMemberIdRaw()))
                .isPresent();
    }

    public boolean isMember(UUID groupId, Authentication authentication) {
        return authentication != null && isMember(groupId, authentication.getPrincipal());
    }
}
