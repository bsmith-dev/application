package app.prompts.api.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import app.prompts.api.application.port.GroupMembershipRepository;
import app.prompts.api.domain.model.GroupId;
import app.prompts.api.domain.model.GroupMembership;
import app.prompts.api.domain.model.MemberId;
import app.prompts.api.domain.model.Role;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupAccessServiceTest {

    @Mock
    private GroupMembershipRepository groupMembershipRepository;

    private GroupAccessService groupAccessService;

    @BeforeEach
    void setUp() {
        groupAccessService = new GroupAccessService(groupMembershipRepository);
    }

    private MemberUserDetails principalFor(UUID memberId) {
        return new MemberUserDetails(memberId, UUID.randomUUID(), "user", "");
    }

    @DisplayName("Is member when membership exists returns true")
    @Test
    void isMember_whenMembershipExists_returnsTrue() {
        UUID groupId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        GroupId gid = new GroupId(groupId);
        MemberId mid = new MemberId(memberId);

        when(groupMembershipRepository.findByGroupIdAndMemberId(gid, mid))
                .thenReturn(Optional.of(new GroupMembership(gid, mid, Role.MEMBER)));

        assertThat(groupAccessService.isMember(groupId, principalFor(memberId))).isTrue();
    }

    @DisplayName("Is member when no membership returns false")
    @Test
    void isMember_whenNoMembership_returnsFalse() {
        UUID groupId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        when(groupMembershipRepository.findByGroupIdAndMemberId(any(), any()))
                .thenReturn(Optional.empty());

        assertThat(groupAccessService.isMember(groupId, principalFor(memberId))).isFalse();
    }

    @DisplayName("Is member when principal is not authenticated member returns false")
    @Test
    void isMember_whenPrincipalIsNotAuthenticatedMember_returnsFalse() {
        UUID groupId = UUID.randomUUID();
        Object notAMember = "some-string-principal";

        assertThat(groupAccessService.isMember(groupId, notAMember)).isFalse();
    }

    @DisplayName("Is member when principal is null returns false")
    @Test
    void isMember_whenPrincipalIsNull_returnsFalse() {
        assertThat(groupAccessService.isMember(UUID.randomUUID(), null)).isFalse();
    }
}
