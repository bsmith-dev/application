package app.prompts.api.application.service;

import java.util.List;
import java.util.UUID;
import app.prompts.api.application.dto.MemberResult;
import app.prompts.api.application.dto.RegisterMemberCommand;
import app.prompts.api.application.port.GroupMembershipRepository;
import app.prompts.api.application.port.MemberQueryUseCase;
import app.prompts.api.application.port.MemberRepository;
import app.prompts.api.application.port.RegisterMemberUseCase;
import app.prompts.api.domain.model.GroupMembership;
import app.prompts.api.domain.model.Member;
import app.prompts.api.domain.model.MemberId;
import app.prompts.api.domain.model.OrganizationId;
import app.prompts.api.domain.model.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService implements RegisterMemberUseCase, MemberQueryUseCase {
    private final MemberRepository memberRepository;
    private final GroupMembershipRepository groupMembershipRepository;

    public MemberService(MemberRepository memberRepository,
                         GroupMembershipRepository groupMembershipRepository) {
        this.memberRepository = memberRepository;
        this.groupMembershipRepository = groupMembershipRepository;
    }

    @Transactional
    @Override
    public MemberResult registerMember(RegisterMemberCommand command) {
        Member member = new Member(
                MemberId.newId(),
                new app.prompts.api.domain.model.OrganizationId(command.organizationId()),
                command.username(),
                command.email());
        Member saved = memberRepository.saveWithPassword(member, command.rawPassword());
        return toResult(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MemberResult> listMembers(UUID organizationId) {
        return memberRepository.findByOrganizationId(new OrganizationId(organizationId)).stream()
                .map(this::toResult)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public MemberResult getMember(UUID memberId) {
        return memberRepository.findById(new MemberId(memberId))
                .map(this::toResult)
                .orElseThrow(() -> new MemberNotFoundException("Member not found: " + memberId));
    }

    private MemberResult toResult(Member member) {
        return new MemberResult(
                member.id().value(),
                member.organizationId().value(),
                member.username(),
                member.email(),
                highestRoleFor(member.id()));
    }

    private Role highestRoleFor(MemberId memberId) {
        return groupMembershipRepository.findByMemberId(memberId).stream()
                .map(GroupMembership::role)
                .min((left, right) -> Integer.compare(roleRank(left), roleRank(right)))
                .orElse(null);
    }

    private int roleRank(Role role) {
        return switch (role) {
            case ADMIN -> 0;
            case GROUP_LEAD -> 1;
            case MEMBER -> 2;
        };
    }
}
