package app.prompts.api.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import app.prompts.api.application.dto.AddGroupMemberCommand;
import app.prompts.api.application.dto.CreateGroupCommand;
import app.prompts.api.application.dto.GroupMembershipResult;
import app.prompts.api.application.dto.GroupResult;
import app.prompts.api.application.port.GroupMembershipRepository;
import app.prompts.api.application.port.GroupRepository;
import app.prompts.api.application.port.MemberRepository;
import app.prompts.api.application.port.PromptRepository;
import app.prompts.api.domain.model.Group;
import app.prompts.api.domain.model.GroupId;
import app.prompts.api.domain.model.GroupMembership;
import app.prompts.api.domain.model.Member;
import app.prompts.api.domain.model.MemberId;
import app.prompts.api.domain.model.OrganizationId;
import app.prompts.api.domain.model.Role;
import app.prompts.api.domain.service.DefaultGroupAccessPolicy;
import app.prompts.api.domain.service.DefaultGroupMembershipManagementPolicy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private GroupMembershipRepository groupMembershipRepository;
    @Mock
    private PromptRepository promptRepository;

    private GroupService groupService;

    @BeforeEach
    void setUp() {
        groupService = new GroupService(
                groupRepository,
                memberRepository,
                groupMembershipRepository,
                promptRepository,
                new DefaultGroupAccessPolicy(),
                new DefaultGroupMembershipManagementPolicy());
    }

    // ── createGroup ──────────────────────────────────────────────────────────

    @DisplayName("Create group with organization ID preserves it")
    @Test
    void createGroupWithOrganizationIdPreservesIt() {
        UUID orgId = UUID.randomUUID();
        when(groupRepository.save(any(Group.class))).thenAnswer(inv -> inv.getArgument(0));

        GroupResult result = groupService.createGroup(new CreateGroupCommand("Ops", orgId, null));

        assertThat(result.name()).isEqualTo("Ops");
        assertThat(result.organizationId()).isEqualTo(orgId);
    }

    @DisplayName("Create group auto assigns requester as admin")
    @Test
    void createGroupAutoAssignsRequesterAsAdmin() {
        UUID orgId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        when(groupMembershipRepository.findByMemberId(any())).thenReturn(List.of());
        when(groupRepository.save(any(Group.class))).thenAnswer(inv -> inv.getArgument(0));
        when(groupMembershipRepository.save(any(GroupMembership.class))).thenAnswer(inv -> inv.getArgument(0));

        GroupResult result = groupService.createGroup(new CreateGroupCommand("Support", orgId, requesterId));

        assertThat(result.organizationId()).isEqualTo(orgId);
    }

    // ── addMember ────────────────────────────────────────────────────────────

    @DisplayName("Add member by group lead succeeds")
    @Test
    void addMemberByGroupLeadSucceeds() {
        GroupId groupId = GroupId.newId();
        MemberId requesterId = MemberId.newId();
        MemberId memberId = MemberId.newId();
        OrganizationId orgId = OrganizationId.newId();

        when(groupRepository.findByIdAndOrganizationId(groupId, orgId))
                .thenReturn(Optional.of(new Group(groupId, orgId, "Support")));
        when(memberRepository.findById(memberId))
                .thenReturn(Optional.of(new Member(memberId, orgId, "jdoe", "jdoe@example.com")));
        when(groupMembershipRepository.findByGroupIdAndMemberId(groupId, requesterId))
                .thenReturn(Optional.of(new GroupMembership(groupId, requesterId, Role.GROUP_LEAD)));
        when(groupMembershipRepository.save(any(GroupMembership.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        GroupMembershipResult result = groupService.addMember(
                new AddGroupMemberCommand(groupId.value(), requesterId.value(), memberId.value(), Role.MEMBER, orgId.value()));

        assertThat(result.memberId()).isEqualTo(memberId.value());
        assertThat(result.role()).isEqualTo(Role.MEMBER);
    }

    @DisplayName("Add member by admin succeeds")
    @Test
    void addMemberByAdminSucceeds() {
        GroupId groupId = GroupId.newId();
        MemberId requesterId = MemberId.newId();
        MemberId memberId = MemberId.newId();
        OrganizationId orgId = OrganizationId.newId();

        when(groupRepository.findByIdAndOrganizationId(groupId, orgId))
                .thenReturn(Optional.of(new Group(groupId, orgId, "Support")));
        when(memberRepository.findById(memberId))
                .thenReturn(Optional.of(new Member(memberId, orgId, "jdoe", "jdoe@example.com")));
        when(groupMembershipRepository.findByGroupIdAndMemberId(groupId, requesterId))
                .thenReturn(Optional.of(new GroupMembership(groupId, requesterId, Role.ADMIN)));
        when(groupMembershipRepository.save(any(GroupMembership.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        GroupMembershipResult result = groupService.addMember(
                new AddGroupMemberCommand(groupId.value(), requesterId.value(), memberId.value(), Role.GROUP_LEAD, orgId.value()));

        assertThat(result.role()).isEqualTo(Role.GROUP_LEAD);
    }

    @DisplayName("Add member by non-member requester is denied")
    @Test
    void addMemberByNonMemberRequesterIsDenied() {
        GroupId groupId = GroupId.newId();
        MemberId requesterId = MemberId.newId();
        MemberId memberId = MemberId.newId();
        OrganizationId orgId = OrganizationId.newId();
        AddGroupMemberCommand command = new AddGroupMemberCommand(groupId.value(), requesterId.value(), memberId.value(), Role.MEMBER, orgId.value());

        when(groupRepository.findByIdAndOrganizationId(groupId, orgId))
                .thenReturn(Optional.of(new Group(groupId, orgId, "Support")));
        when(memberRepository.findById(memberId))
                .thenReturn(Optional.of(new Member(memberId, orgId, "jdoe", "jdoe@example.com")));
        when(groupMembershipRepository.findByGroupIdAndMemberId(groupId, requesterId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> addMember(command))
                .isInstanceOf(AccessDeniedException.class);
    }

    @DisplayName("Add member by plain member is denied")
    @Test
    void addMemberByPlainMemberIsDenied() {
        GroupId groupId = GroupId.newId();
        MemberId requesterId = MemberId.newId();
        MemberId memberId = MemberId.newId();
        OrganizationId orgId = OrganizationId.newId();
        AddGroupMemberCommand command = new AddGroupMemberCommand(groupId.value(), requesterId.value(), memberId.value(), Role.MEMBER, orgId.value());

        when(groupRepository.findByIdAndOrganizationId(groupId, orgId))
                .thenReturn(Optional.of(new Group(groupId, orgId, "Support")));
        when(memberRepository.findById(memberId))
                .thenReturn(Optional.of(new Member(memberId, orgId, "jdoe", "jdoe@example.com")));
        when(groupMembershipRepository.findByGroupIdAndMemberId(groupId, requesterId))
                .thenReturn(Optional.of(new GroupMembership(groupId, requesterId, Role.MEMBER)));

        assertThatThrownBy(() -> addMember(command))
                .isInstanceOf(AccessDeniedException.class);
    }

    @DisplayName("Add member group not found throws an exception")
    @Test
    void addMemberGroupNotFoundThrows() {
        GroupId groupId = GroupId.newId();
        MemberId requesterId = MemberId.newId();
        MemberId memberId = MemberId.newId();
        OrganizationId orgId = OrganizationId.newId();
        AddGroupMemberCommand command = new AddGroupMemberCommand(groupId.value(), requesterId.value(), memberId.value(), Role.MEMBER, orgId.value());

        when(groupRepository.findByIdAndOrganizationId(groupId, orgId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addMember(command))
                .isInstanceOf(GroupNotFoundException.class);
    }

    @DisplayName("Add member target member not found throws an exception")
    @Test
    void addMemberTargetMemberNotFoundThrows() {
        GroupId groupId = GroupId.newId();
        MemberId requesterId = MemberId.newId();
        MemberId memberId = MemberId.newId();
        OrganizationId orgId = OrganizationId.newId();
        AddGroupMemberCommand command = new AddGroupMemberCommand(groupId.value(), requesterId.value(), memberId.value(), Role.MEMBER, orgId.value());

        when(groupRepository.findByIdAndOrganizationId(groupId, orgId))
                .thenReturn(Optional.of(new Group(groupId, orgId, "Support")));
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addMember(command))
                .isInstanceOf(MemberNotFoundException.class);
    }

    // ── listMembers ──────────────────────────────────────────────────────────

    @DisplayName("List members by member succeeds")
    @Test
    void listMembersByMemberSucceeds() {
        GroupId groupId = GroupId.newId();
        MemberId requesterId = MemberId.newId();
        OrganizationId orgId = OrganizationId.newId();

        when(groupMembershipRepository.findByGroupId(groupId))
                .thenReturn(List.of(new GroupMembership(groupId, requesterId, Role.MEMBER)));
        when(groupRepository.findByIdAndOrganizationId(groupId, orgId))
                .thenReturn(Optional.of(new Group(groupId, orgId, "Support")));

        List<GroupMembershipResult> results = groupService.listMembers(groupId.value(), requesterId.value(), orgId.value());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).memberId()).isEqualTo(requesterId.value());
    }

    @DisplayName("List members by non-member is denied")
    @Test
    void listMembersByNonMemberIsDenied() {
        GroupId groupId = GroupId.newId();
        MemberId requesterId = MemberId.newId();
        OrganizationId orgId = OrganizationId.newId();
        when(groupRepository.findByIdAndOrganizationId(groupId, orgId))
                .thenReturn(Optional.of(new Group(groupId, orgId, "Support")));

        when(groupMembershipRepository.findByGroupId(groupId))
                .thenReturn(List.of(new GroupMembership(groupId, MemberId.newId(), Role.MEMBER)));

        assertThatThrownBy(() -> listMembersFor(groupId, requesterId, orgId))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ── Cross-org IDOR isolation ──────────────────────────────────────────────

    @DisplayName("Get group in wrong org throws not found")
    @Test
    void getGroupInWrongOrgThrowsNotFound() {
        GroupId groupId = GroupId.newId();
        MemberId requesterId = MemberId.newId();
        UUID attackerOrgId = UUID.randomUUID();

        // findByIdAndOrganizationId returns empty — group is in a different org
        when(groupRepository.findByIdAndOrganizationId(groupId, new OrganizationId(attackerOrgId)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.getGroup(groupId.value(), requesterId.value(), attackerOrgId))
                .isInstanceOf(GroupNotFoundException.class);
    }

    // ── listGroupsForMember ──────────────────────────────────────────────────

    @DisplayName("List groups for member returns only groups in same org")
    @Test
    void listGroupsForMemberReturnsOnlyGroupsInSameOrg() {
        MemberId memberId = MemberId.newId();
        GroupId groupId = GroupId.newId();
        OrganizationId orgId = OrganizationId.newId();

        when(groupMembershipRepository.findByMemberId(memberId))
                .thenReturn(List.of(new GroupMembership(groupId, memberId, Role.MEMBER)));
        when(groupRepository.findByIdAndOrganizationId(groupId, orgId))
                .thenReturn(Optional.of(new Group(groupId, orgId, "Support")));

        List<GroupResult> results = groupService.listGroupsForMember(memberId.value(), orgId.value());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).groupId()).isEqualTo(groupId.value());
        assertThat(results.get(0).organizationId()).isEqualTo(orgId.value());
    }

    @DisplayName("List groups for member skips groups in different org")
    @Test
    void listGroupsForMemberSkipsGroupsInDifferentOrg() {
        MemberId memberId = MemberId.newId();
        GroupId groupId = GroupId.newId();
        OrganizationId orgId = OrganizationId.newId();

        when(groupMembershipRepository.findByMemberId(memberId))
                .thenReturn(List.of(new GroupMembership(groupId, memberId, Role.MEMBER)));
        // findByIdAndOrganizationId returns empty — group belongs to a different org
        when(groupRepository.findByIdAndOrganizationId(groupId, orgId)).thenReturn(Optional.empty());

        List<GroupResult> results = groupService.listGroupsForMember(memberId.value(), orgId.value());

        assertThat(results).isEmpty();
    }

    private void addMember(AddGroupMemberCommand command) {
        groupService.addMember(command);
    }

    private List<GroupMembershipResult> listMembersFor(GroupId groupId, MemberId requesterId, OrganizationId orgId) {
        return groupService.listMembers(groupId.value(), requesterId.value(), orgId.value());
    }
}
