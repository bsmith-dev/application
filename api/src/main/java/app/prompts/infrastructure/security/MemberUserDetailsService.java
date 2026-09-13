package app.prompts.infrastructure.security;

import java.util.List;
import java.util.stream.Collectors;
import app.prompts.application.port.GroupMembershipRepository;
import app.prompts.application.port.MemberCredentials;
import app.prompts.application.port.MemberRepository;
import app.prompts.domain.model.MemberId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class MemberUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final GroupMembershipRepository groupMembershipRepository;

    public MemberUserDetailsService(MemberRepository memberRepository,
                                    GroupMembershipRepository groupMembershipRepository) {
        this.memberRepository = memberRepository;
        this.groupMembershipRepository = groupMembershipRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MemberCredentials credentials = memberRepository.findCredentialsByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Collect distinct roles across all group memberships and map to Spring authorities
        List<GrantedAuthority> authorities = groupMembershipRepository
                .findByMemberId(new MemberId(credentials.memberId()))
                .stream()
                .map(m -> m.role())
                .distinct()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());

        return new MemberUserDetails(
                credentials.memberId(),
                credentials.organizationId(),
                credentials.username(),
                credentials.passwordHash(),
                authorities);
    }
}
