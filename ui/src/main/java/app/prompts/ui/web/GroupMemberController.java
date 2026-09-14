package app.prompts.ui.web;

import app.prompts.ui.api.ApiValidationException;
import app.prompts.ui.api.GroupApiClient;
import app.prompts.ui.api.GroupMembershipApiClient;
import app.prompts.ui.api.MemberApiClient;
import app.prompts.ui.api.dto.AddMemberRequest;
import app.prompts.ui.api.dto.ChangeMemberRoleRequest;
import app.prompts.ui.form.ChangeRoleForm;
import app.prompts.ui.form.GroupMemberForm;
import app.prompts.ui.service.GroupContextService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Group membership management under /groups/{groupId}/members. */
@Controller
public class GroupMemberController {

    private final GroupMembershipApiClient groupMembershipApiClient;
    private final GroupApiClient groupApiClient;
    private final MemberApiClient memberApiClient;
    private final GroupContextService groupContextService;

    public GroupMemberController(
            GroupMembershipApiClient groupMembershipApiClient,
            GroupApiClient groupApiClient,
            MemberApiClient memberApiClient,
            GroupContextService groupContextService
    ) {
        this.groupMembershipApiClient = groupMembershipApiClient;
        this.groupApiClient = groupApiClient;
        this.memberApiClient = memberApiClient;
        this.groupContextService = groupContextService;
    }

    @GetMapping("/groups/{groupId}/members")
    String list(@PathVariable UUID groupId, Model model) {
        model.addAttribute("group", groupApiClient.getGroup(groupId));
        model.addAttribute("memberships", groupMembershipApiClient.listGroupMembers(groupId));
        model.addAttribute("permissions", groupContextService.permissions(groupId));
        return "groups/members/list";
    }

    @GetMapping("/groups/{groupId}/members/new")
    String addForm(@PathVariable UUID groupId, Model model) {
        model.addAttribute("group", groupApiClient.getGroup(groupId));
        model.addAttribute("candidateMembers", memberApiClient.listMembers());
        model.addAttribute("groupMemberForm", new GroupMemberForm());
        return "groups/members/form";
    }

    @PostMapping("/groups/{groupId}/members")
    String add(
            @PathVariable UUID groupId,
            @Valid @ModelAttribute("groupMemberForm") GroupMemberForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("group", groupApiClient.getGroup(groupId));
            model.addAttribute("candidateMembers", memberApiClient.listMembers());
            return "groups/members/form";
        }

        try {
            groupMembershipApiClient.addMember(
                    groupId,
                    new AddMemberRequest(UUID.fromString(form.getMemberId()), form.getRole())
            );
        } catch (ApiValidationException ex) {
            model.addAttribute("group", groupApiClient.getGroup(groupId));
            model.addAttribute("candidateMembers", memberApiClient.listMembers());
            model.addAttribute("errorMessage", "The API rejected this membership: " + ex.getMessage());
            return "groups/members/form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Member added to group.");
        return "redirect:/groups/" + groupId + "/members";
    }

    @GetMapping("/groups/{groupId}/members/{memberId}/edit")
    String editForm(@PathVariable UUID groupId, @PathVariable UUID memberId, Model model) {
        var membership = groupMembershipApiClient.listGroupMembers(groupId).stream()
                .filter(m -> memberId.equals(m.memberId()))
                .findFirst()
                .orElseThrow(() -> new app.prompts.ui.api.ApiNotFoundException(
                        "Member " + memberId + " is not part of group " + groupId + "."));

        ChangeRoleForm form = new ChangeRoleForm();
        form.setRole(membership.role());

        model.addAttribute("group", groupApiClient.getGroup(groupId));
        model.addAttribute("memberId", memberId);
        model.addAttribute("changeRoleForm", form);
        return "groups/members/edit-role";
    }

    @PostMapping("/groups/{groupId}/members/{memberId}")
    String updateRole(
            @PathVariable UUID groupId,
            @PathVariable UUID memberId,
            @Valid @ModelAttribute("changeRoleForm") ChangeRoleForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("group", groupApiClient.getGroup(groupId));
            model.addAttribute("memberId", memberId);
            return "groups/members/edit-role";
        }

        try {
            groupMembershipApiClient.changeMemberRole(groupId, memberId, new ChangeMemberRoleRequest(form.getRole()));
        } catch (ApiValidationException ex) {
            model.addAttribute("group", groupApiClient.getGroup(groupId));
            model.addAttribute("memberId", memberId);
            model.addAttribute("errorMessage", "The API rejected this role change: " + ex.getMessage());
            return "groups/members/edit-role";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Membership role updated.");
        return "redirect:/groups/" + groupId + "/members";
    }

    @PostMapping("/groups/{groupId}/members/{memberId}/delete")
    String remove(@PathVariable UUID groupId, @PathVariable UUID memberId, RedirectAttributes redirectAttributes) {
        groupMembershipApiClient.removeMember(groupId, memberId);
        redirectAttributes.addFlashAttribute("successMessage", "Member removed from group.");
        return "redirect:/groups/" + groupId + "/members";
    }
}
