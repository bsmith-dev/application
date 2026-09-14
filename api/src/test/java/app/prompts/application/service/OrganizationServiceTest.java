package app.prompts.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import app.prompts.application.dto.CreateOrganizationCommand;
import app.prompts.application.dto.OrganizationResult;
import app.prompts.application.dto.RenameOrganizationCommand;
import app.prompts.application.port.GroupMembershipRepository;
import app.prompts.application.port.GroupRepository;
import app.prompts.application.port.MemberRepository;
import app.prompts.application.port.OrganizationRepository;
import app.prompts.domain.model.Group;
import app.prompts.domain.model.GroupId;
import app.prompts.domain.model.GroupMembership;
import app.prompts.domain.model.Member;
import app.prompts.domain.model.MemberId;
import app.prompts.domain.model.Organization;
import app.prompts.domain.model.OrganizationId;
import app.prompts.domain.model.Role;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private GroupMembershipRepository groupMembershipRepository;

    private OrganizationService organizationService;

    @BeforeEach
    void setUp() {
        organizationService = new OrganizationService(
                organizationRepository,
                memberRepository,
                groupRepository,
                groupMembershipRepository);
    }

    @DisplayName("Create organization creates new ID when command has no ID")
    @Test
    void createOrganizationCreatesNewIdWhenCommandHasNoId() {
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        OrganizationResult result = organizationService.createOrganization(new CreateOrganizationCommand(null, "Acme"));

        assertThat(result.organizationId()).isNotNull();
        assertThat(result.name()).isEqualTo("Acme");
    }

    @DisplayName("List organizations requires admin membership")
    @Test
    void listOrganizationsRequiresAdminMembership() {
        MemberId requesterId = MemberId.newId();

        when(groupMembershipRepository.findByMemberId(requesterId))
                .thenReturn(List.of(new GroupMembership(GroupId.newId(), requesterId, Role.MEMBER)));

        assertThatThrownBy(() -> organizationService.listOrganizations(requesterId.value()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @DisplayName("List organizations returns all organizations for admin")
    @Test
    void listOrganizationsReturnsAllOrganizationsForAdmin() {
        MemberId requesterId = MemberId.newId();
        OrganizationId orgId = OrganizationId.newId();

        when(groupMembershipRepository.findByMemberId(requesterId))
                .thenReturn(List.of(new GroupMembership(GroupId.newId(), requesterId, Role.ADMIN)));
        when(organizationRepository.findAll())
                .thenReturn(List.of(new Organization(orgId, "Acme")));

        List<OrganizationResult> results = organizationService.listOrganizations(requesterId.value());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).organizationId()).isEqualTo(orgId.value());
        assertThat(results.get(0).name()).isEqualTo("Acme");
    }

    @DisplayName("Rename organization requires admin membership")
    @Test
    void renameOrganizationRequiresAdminMembership() {
        MemberId requesterId = MemberId.newId();
        OrganizationId orgId = OrganizationId.newId();

        when(groupMembershipRepository.findByMemberId(requesterId)).thenReturn(List.of());

        assertThatThrownBy(() -> organizationService.renameOrganization(
                new RenameOrganizationCommand(orgId.value(), "Renamed", requesterId.value())))
                .isInstanceOf(AccessDeniedException.class);
    }

    @DisplayName("Rename organization updates name for admin")
    @Test
    void renameOrganizationUpdatesNameForAdmin() {
        MemberId requesterId = MemberId.newId();
        OrganizationId orgId = OrganizationId.newId();

        when(groupMembershipRepository.findByMemberId(requesterId))
                .thenReturn(List.of(new GroupMembership(GroupId.newId(), requesterId, Role.ADMIN)));
        when(organizationRepository.findById(orgId))
                .thenReturn(Optional.of(new Organization(orgId, "Acme")));
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        OrganizationResult result = organizationService.renameOrganization(
                new RenameOrganizationCommand(orgId.value(), "Acme Updated", requesterId.value()));

        assertThat(result.name()).isEqualTo("Acme Updated");
    }

    @DisplayName("Rename organization not found throws an exception")
    @Test
    void renameOrganizationNotFoundThrows() {
        MemberId requesterId = MemberId.newId();
        OrganizationId orgId = OrganizationId.newId();

        when(groupMembershipRepository.findByMemberId(requesterId))
                .thenReturn(List.of(new GroupMembership(GroupId.newId(), requesterId, Role.ADMIN)));
        when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> organizationService.renameOrganization(
                new RenameOrganizationCommand(orgId.value(), "Acme Updated", requesterId.value())))
                .isInstanceOf(OrganizationNotFoundException.class);
    }

    @DisplayName("Delete organization blocks when members exist")
    @Test
    void deleteOrganizationBlocksWhenMembersExist() {
        MemberId requesterId = MemberId.newId();
        OrganizationId orgId = OrganizationId.newId();

        when(groupMembershipRepository.findByMemberId(requesterId))
                .thenReturn(List.of(new GroupMembership(GroupId.newId(), requesterId, Role.ADMIN)));
        when(organizationRepository.findById(orgId))
                .thenReturn(Optional.of(new Organization(orgId, "Acme")));
        when(memberRepository.findByOrganizationId(orgId))
                .thenReturn(List.of(new Member(MemberId.newId(), orgId, "alice", "alice@example.com")));

        assertThatThrownBy(() -> organizationService.deleteOrganization(orgId.value(), requesterId.value()))
                .isInstanceOf(IllegalStateException.class);
        verify(organizationRepository, never()).deleteById(any());
    }

    @DisplayName("Delete organization blocks when groups exist")
    @Test
    void deleteOrganizationBlocksWhenGroupsExist() {
        MemberId requesterId = MemberId.newId();
        OrganizationId orgId = OrganizationId.newId();

        when(groupMembershipRepository.findByMemberId(requesterId))
                .thenReturn(List.of(new GroupMembership(GroupId.newId(), requesterId, Role.ADMIN)));
        when(organizationRepository.findById(orgId))
                .thenReturn(Optional.of(new Organization(orgId, "Acme")));
        when(memberRepository.findByOrganizationId(orgId)).thenReturn(List.of());
        when(groupRepository.findAll())
                .thenReturn(List.of(new Group(GroupId.newId(), orgId, "Support")));

        assertThatThrownBy(() -> organizationService.deleteOrganization(orgId.value(), requesterId.value()))
                .isInstanceOf(IllegalStateException.class);
        verify(organizationRepository, never()).deleteById(any());
    }

    @DisplayName("Delete organization deletes empty organization for admin")
    @Test
    void deleteOrganizationDeletesEmptyOrganizationForAdmin() {
        MemberId requesterId = MemberId.newId();
        OrganizationId orgId = OrganizationId.newId();

        when(groupMembershipRepository.findByMemberId(requesterId))
                .thenReturn(List.of(new GroupMembership(GroupId.newId(), requesterId, Role.ADMIN)));
        when(organizationRepository.findById(orgId))
                .thenReturn(Optional.of(new Organization(orgId, "Acme")));
        when(memberRepository.findByOrganizationId(orgId)).thenReturn(List.of());
        when(groupRepository.findAll()).thenReturn(List.of());

        organizationService.deleteOrganization(orgId.value(), requesterId.value());

        verify(organizationRepository).deleteById(orgId);
    }
}
