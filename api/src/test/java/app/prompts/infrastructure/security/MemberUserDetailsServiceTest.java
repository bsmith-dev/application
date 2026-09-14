package app.prompts.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import app.prompts.application.port.GroupMembershipRepository;
import app.prompts.application.port.MemberCredentials;
import app.prompts.application.port.MemberRepository;
import app.prompts.domain.model.MemberId;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberUserDetailsServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private GroupMembershipRepository groupMembershipRepository;

    private MemberUserDetailsService service;

    @BeforeEach
    void setUp() {
        service = new MemberUserDetailsService(memberRepository, groupMembershipRepository);
    }

    @DisplayName("Load user by username found returns user details")
    @Test
    void loadUserByUsername_found_returnsUserDetails() {
        UUID memberId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        MemberCredentials creds = new MemberCredentials(memberId, orgId, "alice", "$2a$10$hash");
        when(memberRepository.findCredentialsByUsername("alice")).thenReturn(Optional.of(creds));
        when(groupMembershipRepository.findByMemberId(any(MemberId.class))).thenReturn(List.of());

        UserDetails details = service.loadUserByUsername("alice");

        assertThat(details.getUsername()).isEqualTo("alice");
        assertThat(details.getPassword()).isEqualTo("$2a$10$hash");
        assertThat(((MemberUserDetails) details).getMemberIdRaw()).isEqualTo(memberId);
        assertThat(((MemberUserDetails) details).getOrganizationIdRaw()).isEqualTo(orgId);
    }

    @DisplayName("Load user by username not found throws username not found exception")
    @Test
    void loadUserByUsername_notFound_throwsUsernameNotFoundException() {
        when(memberRepository.findCredentialsByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loadUser("unknown"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    private UserDetails loadUser(String username) {
        return service.loadUserByUsername(username);
    }
}
