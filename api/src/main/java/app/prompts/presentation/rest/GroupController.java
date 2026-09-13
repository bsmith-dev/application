package app.prompts.presentation.rest;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import app.prompts.application.dto.AddGroupMemberCommand;
import app.prompts.application.dto.AddMemberRequest;
import app.prompts.application.dto.ChangeMemberRoleRequest;
import app.prompts.application.dto.CreateGroupCommand;
import app.prompts.application.dto.CreateGroupRequest;
import app.prompts.application.dto.GroupMembershipResponse;
import app.prompts.application.dto.GroupMembershipResult;
import app.prompts.application.dto.GroupResponse;
import app.prompts.application.dto.GroupResult;
import app.prompts.application.dto.RenameGroupRequest;
import app.prompts.application.port.AuthenticatedPrincipal;
import app.prompts.application.port.ManageGroupUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final ManageGroupUseCase groupUseCase;

    public GroupController(ManageGroupUseCase groupUseCase) {
        this.groupUseCase = groupUseCase;
    }

    // ── Groups ────────────────────────────────────────────────────────────────

    @GetMapping
    public List<GroupResponse> listGroups(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return groupUseCase.listGroupsForMember(
                        principal.getMemberId(),
                        principal.getOrganizationId()).stream()
                .map(r -> new GroupResponse(r.groupId(), r.organizationId(), r.name()))
                .toList();
    }

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        GroupResult result = groupUseCase.createGroup(
                new CreateGroupCommand(request.name(),
                        principal.getOrganizationId(),
                        principal.getMemberId()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GroupResponse(result.groupId(), result.organizationId(), result.name()));
    }

    @GetMapping("/{groupId}")
    public GroupResponse getGroup(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        GroupResult result = groupUseCase.getGroup(
                groupId,
                principal.getMemberId(),
                principal.getOrganizationId());
        return new GroupResponse(result.groupId(), result.organizationId(), result.name());
    }

    @PutMapping("/{groupId}")
    public GroupResponse renameGroup(
            @PathVariable UUID groupId,
            @Valid @RequestBody RenameGroupRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        GroupResult result = groupUseCase.renameGroup(
                groupId,
                request.name(),
                principal.getMemberId(),
                principal.getOrganizationId());
        return new GroupResponse(result.groupId(), result.organizationId(), result.name());
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        groupUseCase.deleteGroup(groupId, principal.getMemberId(), principal.getOrganizationId());
        return ResponseEntity.noContent().build();
    }

    // ── Memberships ───────────────────────────────────────────────────────────

    @GetMapping("/{groupId}/members")
    public List<GroupMembershipResponse> listMembers(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return groupUseCase.listMembers(
                        groupId,
                        principal.getMemberId(),
                        principal.getOrganizationId()).stream()
                .map(r -> new GroupMembershipResponse(r.groupId(), r.memberId(), r.role()))
                .toList();
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<GroupMembershipResponse> addMember(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddMemberRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        GroupMembershipResult result = groupUseCase.addMember(
                new AddGroupMemberCommand(
                        groupId,
                        principal.getMemberId(),
                        request.memberId(),
                        request.role(),
                        principal.getOrganizationId()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GroupMembershipResponse(result.groupId(), result.memberId(), result.role()));
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID groupId,
            @PathVariable UUID memberId,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        groupUseCase.removeMember(groupId, memberId, principal.getMemberId(), principal.getOrganizationId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{groupId}/members/{memberId}")
    public GroupMembershipResponse changeMemberRole(
            @PathVariable UUID groupId,
            @PathVariable UUID memberId,
            @Valid @RequestBody ChangeMemberRoleRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        GroupMembershipResult result = groupUseCase.changeMemberRole(
                groupId,
                memberId,
                request.role(),
                principal.getMemberId(),
                principal.getOrganizationId());
        return new GroupMembershipResponse(result.groupId(), result.memberId(), result.role());
    }
}
