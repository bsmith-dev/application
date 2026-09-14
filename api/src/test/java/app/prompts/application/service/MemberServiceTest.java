package app.prompts.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import app.prompts.application.dto.MemberResult;
import app.prompts.application.dto.RegisterMemberCommand;
import app.prompts.application.port.GroupMembershipRepository;
import app.prompts.application.port.MemberRepository;
import app.prompts.domain.model.GroupId;
import app.prompts.domain.model.GroupMembership;
import app.prompts.domain.model.Member;
import app.prompts.domain.model.MemberId;
import app.prompts.domain.model.OrganizationId;
import app.prompts.domain.model.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private GroupMembershipRepository groupMembershipRepository;

    @DisplayName("Register member saves and returns result")
    @Test
    void registerMemberSavesAndReturnsResult() {
        UUID orgId = UUID.randomUUID();
        when(memberRepository.saveWithPassword(any(Member.class), any(String.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MemberService memberService = new MemberService(memberRepository, groupMembershipRepository);
        MemberResult result = memberService.registerMember(
                new RegisterMemberCommand(orgId, "jdoe", "secret", "jdoe@example.com"));

        assertEquals("jdoe", result.username());
        assertEquals("jdoe@example.com", result.email());
        assertEquals(orgId, result.organizationId());
        assertNotNull(result.memberId());
        assertNull(result.role());
    }

    @DisplayName("List members returns only members in given org")
    @Test
    void listMembers_returnsOnlyMembersInGivenOrg() {
        UUID orgId = UUID.randomUUID();
        UUID otherOrgId = UUID.randomUUID();
        Member m1 = new Member(MemberId.newId(), new OrganizationId(orgId),    "alice", "alice@example.com");
        Member m2 = new Member(MemberId.newId(), new OrganizationId(orgId),    "bob",   "bob@example.com");
        // m3 is in a different org — should never appear
        Member m3 = new Member(MemberId.newId(), new OrganizationId(otherOrgId), "carol", "carol@example.com");

        when(memberRepository.findByOrganizationId(new OrganizationId(orgId)))
                .thenReturn(List.of(m1, m2));

        when(groupMembershipRepository.findByMemberId(m1.id()))
                .thenReturn(List.of(new GroupMembership(GroupId.newId(), m1.id(), Role.MEMBER)));
        when(groupMembershipRepository.findByMemberId(m2.id()))
                .thenReturn(List.of(new GroupMembership(GroupId.newId(), m2.id(), Role.ADMIN)));

        MemberService memberService = new MemberService(memberRepository, groupMembershipRepository);
        List<MemberResult> results = memberService.listMembers(orgId);

        assertEquals(2, results.size());
        assertEquals("alice", results.get(0).username());
        assertEquals("bob",   results.get(1).username());
        assertEquals(Role.MEMBER, results.get(0).role());
        assertEquals(Role.ADMIN, results.get(1).role());
        // Confirm the third member from another org is not present
        results.forEach(r -> assertEquals(orgId, r.organizationId()));
    }

    @DisplayName("List members returns empty list when org has no members")
    @Test
    void listMembers_returnsEmptyListWhenOrgHasNoMembers() {
        UUID orgId = UUID.randomUUID();
        when(memberRepository.findByOrganizationId(new OrganizationId(orgId))).thenReturn(List.of());

        MemberService memberService = new MemberService(memberRepository, groupMembershipRepository);
        List<MemberResult> results = memberService.listMembers(orgId);

        assertEquals(0, results.size());
    }

    @DisplayName("Get member existing ID returns result")
    @Test
    void getMember_existingId_returnsResult() {
        UUID orgId = UUID.randomUUID();
        MemberId mid = MemberId.newId();
        Member member = new Member(mid, new OrganizationId(orgId), "dave", "dave@example.com");
        when(memberRepository.findById(mid)).thenReturn(Optional.of(member));

        when(groupMembershipRepository.findByMemberId(mid))
                .thenReturn(List.of(new GroupMembership(GroupId.newId(), mid, Role.GROUP_LEAD)));

        MemberService memberService = new MemberService(memberRepository, groupMembershipRepository);
        MemberResult result = memberService.getMember(mid.value());

        assertEquals("dave", result.username());
        assertEquals(mid.value(), result.memberId());
        assertEquals(Role.GROUP_LEAD, result.role());
    }

    @DisplayName("Get member unknown ID throws member not found exception")
    @Test
    void getMember_unknownId_throwsMemberNotFoundException() {
        UUID id = UUID.randomUUID();
        when(memberRepository.findById(new MemberId(id))).thenReturn(Optional.empty());

        MemberService memberService = new MemberService(memberRepository, groupMembershipRepository);
        assertThrows(MemberNotFoundException.class, () -> memberService.getMember(id));
    }

    @DisplayName("Get member with multiple memberships returns highest role")
    @Test
    void getMember_withMultipleMembershipsReturnsHighestRole() {
        UUID orgId = UUID.randomUUID();
        MemberId mid = MemberId.newId();
        Member member = new Member(mid, new OrganizationId(orgId), "erin", "erin@example.com");

        when(memberRepository.findById(mid)).thenReturn(Optional.of(member));
        when(groupMembershipRepository.findByMemberId(mid))
                .thenReturn(List.of(
                        new GroupMembership(GroupId.newId(), mid, Role.MEMBER),
                        new GroupMembership(GroupId.newId(), mid, Role.GROUP_LEAD),
                        new GroupMembership(GroupId.newId(), mid, Role.ADMIN)));

        MemberService memberService = new MemberService(memberRepository, groupMembershipRepository);
        MemberResult result = memberService.getMember(mid.value());

        assertEquals(Role.ADMIN, result.role());
    }
}
