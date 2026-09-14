package app.prompts.api.application.service;

import java.util.List;
import java.util.UUID;
import app.prompts.api.application.dto.CreateOrganizationCommand;
import app.prompts.api.application.dto.OrganizationResult;
import app.prompts.api.application.dto.RenameOrganizationCommand;
import app.prompts.api.application.port.GroupMembershipRepository;
import app.prompts.api.application.port.GroupRepository;
import app.prompts.api.application.port.ManageOrganizationUseCase;
import app.prompts.api.application.port.MemberRepository;
import app.prompts.api.application.port.OrganizationRepository;
import app.prompts.api.domain.model.GroupMembership;
import app.prompts.api.domain.model.MemberId;
import app.prompts.api.domain.model.Organization;
import app.prompts.api.domain.model.OrganizationId;
import app.prompts.api.domain.model.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationService implements ManageOrganizationUseCase {

    private final OrganizationRepository organizationRepository;
    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;
    private final GroupMembershipRepository groupMembershipRepository;

    public OrganizationService(OrganizationRepository organizationRepository,
                               MemberRepository memberRepository,
                               GroupRepository groupRepository,
                               GroupMembershipRepository groupMembershipRepository) {
        this.organizationRepository = organizationRepository;
        this.memberRepository = memberRepository;
        this.groupRepository = groupRepository;
        this.groupMembershipRepository = groupMembershipRepository;
    }

    @Transactional
    @Override
    public OrganizationResult createOrganization(CreateOrganizationCommand command) {
        OrganizationId id = command.organizationId() != null
                ? new OrganizationId(command.organizationId())
                : OrganizationId.newId();
        Organization saved = organizationRepository.save(new Organization(id, command.name()));
        return new OrganizationResult(saved.id().value(), saved.name());
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrganizationResult> listOrganizations(UUID requesterId) {
        assertAdmin(requesterId, "Only ADMIN can list organizations");
        return organizationRepository.findAll().stream()
                .map(this::toOrganizationResult)
                .toList();
    }

    @Transactional
    @Override
    public OrganizationResult renameOrganization(RenameOrganizationCommand command) {
        assertAdmin(command.requesterId(), "Only ADMIN can rename organizations");
        OrganizationId organizationId = new OrganizationId(command.organizationId());
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException(
                        "Organization not found: " + organizationId.value()));
        organization.rename(command.name());
        return toOrganizationResult(organizationRepository.save(organization));
    }

    @Transactional
    @Override
    public void deleteOrganization(UUID rawOrganizationId, UUID requesterId) {
        assertAdmin(requesterId, "Only ADMIN can delete organizations");
        OrganizationId organizationId = new OrganizationId(rawOrganizationId);
        organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException(
                        "Organization not found: " + organizationId.value()));
        if (!memberRepository.findByOrganizationId(organizationId).isEmpty()
                || groupRepository.findAll().stream().anyMatch(group -> organizationId.equals(group.organizationId()))) {
            throw new IllegalStateException("Organization must not contain members or groups before deletion");
        }
        organizationRepository.deleteById(organizationId);
    }

    private void assertAdmin(UUID requesterId, String message) {
        List<GroupMembership> memberships = groupMembershipRepository.findByMemberId(new MemberId(requesterId));
        boolean isAdmin = memberships.stream().anyMatch(membership -> membership.role() == Role.ADMIN);
        if (!isAdmin) {
            throw new AccessDeniedException(message);
        }
    }

    private OrganizationResult toOrganizationResult(Organization organization) {
        return new OrganizationResult(organization.id().value(), organization.name());
    }
}
