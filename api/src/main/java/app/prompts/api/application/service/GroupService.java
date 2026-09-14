package app.prompts.api.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import app.prompts.api.application.dto.AddGroupMemberCommand;
import app.prompts.api.application.dto.CreateGroupCommand;
import app.prompts.api.application.dto.GroupMembershipResult;
import app.prompts.api.application.dto.GroupResult;
import app.prompts.api.application.port.GroupMembershipRepository;
import app.prompts.api.application.port.GroupRepository;
import app.prompts.api.application.port.ManageGroupUseCase;
import app.prompts.api.application.port.MemberRepository;
import app.prompts.api.application.port.PromptRepository;
import app.prompts.api.domain.model.Group;
import app.prompts.api.domain.model.GroupId;
import app.prompts.api.domain.model.GroupMembership;
import app.prompts.api.domain.model.MemberId;
import app.prompts.api.domain.model.OrganizationId;
import app.prompts.api.domain.model.Role;
import app.prompts.api.domain.service.DefaultGroupAccessPolicy;
import app.prompts.api.domain.service.DefaultGroupMembershipManagementPolicy;
import app.prompts.api.domain.service.GroupAccessPolicy;
import app.prompts.api.domain.service.GroupMembershipManagementPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GroupService implements ManageGroupUseCase {

    public static final String MEMBER_IS_NOT_IN_THE_GROUP = "Member is not in the group";
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final PromptRepository promptRepository;
    private final GroupAccessPolicy groupAccessPolicy;
    private final GroupMembershipManagementPolicy groupMembershipManagementPolicy;

    @Autowired
    public GroupService(GroupRepository groupRepository,
                        MemberRepository memberRepository,
                        GroupMembershipRepository groupMembershipRepository,
                        PromptRepository promptRepository) {
        this(groupRepository, memberRepository, groupMembershipRepository, promptRepository,
                new DefaultGroupAccessPolicy(), new DefaultGroupMembershipManagementPolicy());
    }

    public GroupService(GroupRepository groupRepository,
                        MemberRepository memberRepository,
                        GroupMembershipRepository groupMembershipRepository,
                        PromptRepository promptRepository,
                        GroupAccessPolicy groupAccessPolicy,
                        GroupMembershipManagementPolicy groupMembershipManagementPolicy) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.groupMembershipRepository = groupMembershipRepository;
        this.promptRepository = promptRepository;
        this.groupAccessPolicy = groupAccessPolicy;
        this.groupMembershipManagementPolicy = groupMembershipManagementPolicy;
    }

    @Transactional
    @Override
    public GroupResult createGroup(CreateGroupCommand command) {
        // Bootstrap rule: a user with no existing memberships may create their first group
        // and is auto-assigned ADMIN. Once they belong to at least one group they must
        // already hold ADMIN somewhere to create additional groups.
        if (command.requesterId() != null) {
            List<GroupMembership> existing =
                    groupMembershipRepository.findByMemberId(new MemberId(command.requesterId()));
            boolean hasAnyMembership = !existing.isEmpty();
            boolean isAdmin = existing.stream().anyMatch(m -> m.role() == Role.ADMIN);
            if (hasAnyMembership && !isAdmin) {
                throw new AccessDeniedException("Only ADMIN can create additional groups");
            }
        }
        OrganizationId organizationId = new OrganizationId(command.organizationId());
        Group group = new Group(GroupId.newId(), organizationId, command.name());
        Group saved = groupRepository.save(group);
        // Auto-add creator as ADMIN so they can manage the group immediately
        if (command.requesterId() != null) {
            groupMembershipRepository.save(
                    new GroupMembership(saved.id(), new MemberId(command.requesterId()), Role.ADMIN));
        }
        return toGroupResult(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public GroupResult getGroup(UUID groupId, UUID requesterId, UUID organizationId) {
        GroupId gid = new GroupId(groupId);
        MemberId rid = new MemberId(requesterId);
        Group group = findGroupInOrg(gid, organizationId);
        List<GroupMembership> memberships = groupMembershipRepository.findByGroupId(gid);
        if (!groupAccessPolicy.isMember(gid, rid, memberships)) {
            throw new AccessDeniedException(MEMBER_IS_NOT_IN_THE_GROUP);
        }
        return toGroupResult(group);
    }

    @Transactional
    @Override
    public GroupResult renameGroup(UUID groupId, String name, UUID requesterId, UUID organizationId) {
        GroupId gid = new GroupId(groupId);
        MemberId rid = new MemberId(requesterId);
        Group group = findGroupInOrg(gid, organizationId);
        GroupMembership requester = groupMembershipRepository.findByGroupIdAndMemberId(gid, rid)
                .orElseThrow(() -> new AccessDeniedException(MEMBER_IS_NOT_IN_THE_GROUP));
        if (!groupMembershipManagementPolicy.canManageMembers(requester.role())) {
            throw new AccessDeniedException("Only ADMIN or GROUP_LEAD can rename a group");
        }
        group.rename(name);
        return toGroupResult(groupRepository.save(group));
    }

    @Transactional
    @Override
    public void deleteGroup(UUID groupId, UUID requesterId, UUID organizationId) {
        GroupId gid = new GroupId(groupId);
        MemberId rid = new MemberId(requesterId);
        findGroupInOrg(gid, organizationId);
        GroupMembership requester = groupMembershipRepository.findByGroupIdAndMemberId(gid, rid)
                .orElseThrow(() -> new AccessDeniedException(MEMBER_IS_NOT_IN_THE_GROUP));
        if (requester.role() != Role.ADMIN) {
            throw new AccessDeniedException("Only ADMIN can delete a group");
        }
        promptRepository.deleteByGroupId(gid);
        groupMembershipRepository.deleteByGroupId(gid);
        groupRepository.deleteById(gid);
    }

    @Transactional
    @Override
    public GroupMembershipResult addMember(AddGroupMemberCommand command) {
        GroupId groupId = new GroupId(command.groupId());
        MemberId requesterId = new MemberId(command.requesterId());
        MemberId memberId = new MemberId(command.memberId());

        findGroupInOrg(groupId, command.organizationId());
        memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found: " + memberId.value()));

        GroupMembership requesterMembership = groupMembershipRepository
                .findByGroupIdAndMemberId(groupId, requesterId)
                .orElseThrow(() -> new AccessDeniedException(MEMBER_IS_NOT_IN_THE_GROUP));
        if (!groupMembershipManagementPolicy.canManageMembers(requesterMembership.role())) {
            throw new AccessDeniedException("Member cannot manage group members");
        }

        GroupMembership membership = groupMembershipRepository.save(
                new GroupMembership(groupId, memberId, command.role()));
        return toMembershipResult(membership);
    }

    @Transactional
    @Override
    public void removeMember(UUID groupId, UUID targetMemberId, UUID requesterId, UUID organizationId) {
        GroupId gid = new GroupId(groupId);
        MemberId rid = new MemberId(requesterId);
        MemberId targetId = new MemberId(targetMemberId);

        findGroupInOrg(gid, organizationId);
        GroupMembership requester = groupMembershipRepository.findByGroupIdAndMemberId(gid, rid)
                .orElseThrow(() -> new AccessDeniedException(MEMBER_IS_NOT_IN_THE_GROUP));
        if (!groupMembershipManagementPolicy.canManageMembers(requester.role())) {
            throw new AccessDeniedException("Only ADMIN or GROUP_LEAD can remove members");
        }
        groupMembershipRepository.deleteByGroupIdAndMemberId(gid, targetId);
    }

    @Transactional
    @Override
    public GroupMembershipResult changeMemberRole(UUID groupId, UUID targetMemberId, Role role,
                                                  UUID requesterId, UUID organizationId) {
        GroupId gid = new GroupId(groupId);
        MemberId rid = new MemberId(requesterId);
        MemberId targetId = new MemberId(targetMemberId);

        findGroupInOrg(gid, organizationId);
        GroupMembership requester = groupMembershipRepository.findByGroupIdAndMemberId(gid, rid)
                .orElseThrow(() -> new AccessDeniedException(MEMBER_IS_NOT_IN_THE_GROUP));
        if (!groupMembershipManagementPolicy.canManageMembers(requester.role())) {
            throw new AccessDeniedException("Only ADMIN or GROUP_LEAD can change roles");
        }
        groupMembershipRepository.findByGroupIdAndMemberId(gid, targetId)
                .orElseThrow(() -> new MemberNotFoundException("Member not in group: " + targetMemberId));
        GroupMembership updated = groupMembershipRepository.save(new GroupMembership(gid, targetId, role));
        return toMembershipResult(updated);
    }

    @Transactional(readOnly = true)
    @Override
    public List<GroupMembershipResult> listMembers(UUID rawGroupId, UUID rawRequesterId, UUID organizationId) {
        GroupId groupId = new GroupId(rawGroupId);
        MemberId requesterId = new MemberId(rawRequesterId);
        findGroupInOrg(groupId, organizationId);
        List<GroupMembership> memberships = groupMembershipRepository.findByGroupId(groupId);
        if (!groupAccessPolicy.isMember(groupId, requesterId, memberships)) {
            throw new AccessDeniedException(MEMBER_IS_NOT_IN_THE_GROUP);
        }
        return memberships.stream().map(this::toMembershipResult).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<GroupResult> listGroupsForMember(UUID rawMemberId, UUID organizationId) {
        MemberId memberId = new MemberId(rawMemberId);
        OrganizationId orgId = new OrganizationId(organizationId);
        return groupMembershipRepository.findByMemberId(memberId).stream()
                .map(GroupMembership::groupId)
                .map(gid -> groupRepository.findByIdAndOrganizationId(gid, orgId))
                .flatMap(Optional::stream)
                .map(this::toGroupResult)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<GroupResult> listGroups(UUID organizationId) {
        OrganizationId orgId = new OrganizationId(organizationId);
        return groupRepository.findAll().stream()
                .filter(g -> orgId.equals(g.organizationId()))
                .map(this::toGroupResult)
                .toList();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Loads a group by id, enforcing that it belongs to the given org.
     * Returns {@link GroupNotFoundException} if absent OR in a different org —
     * never leaking whether the group exists in another tenant.
     */
    private Group findGroupInOrg(GroupId gid, UUID organizationId) {
        return groupRepository.findByIdAndOrganizationId(gid, new OrganizationId(organizationId))
                .orElseThrow(() -> new GroupNotFoundException("Group not found: " + gid.value()));
    }

    private GroupResult toGroupResult(Group group) {
        return new GroupResult(group.id().value(), group.organizationId().value(), group.name());
    }

    private GroupMembershipResult toMembershipResult(GroupMembership membership) {
        return new GroupMembershipResult(membership.groupId().value(), membership.memberId().value(), membership.role());
    }
}
