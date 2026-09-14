package app.prompts.ui.web;

import app.prompts.ui.api.ApiValidationException;
import app.prompts.ui.api.GroupApiClient;
import app.prompts.ui.api.dto.CreateGroupRequest;
import app.prompts.ui.api.dto.GroupResponse;
import app.prompts.ui.api.dto.RenameGroupRequest;
import app.prompts.ui.form.GroupForm;
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

@Controller
public class GroupController {

    private final GroupApiClient groupApiClient;
    private final GroupContextService groupContextService;

    public GroupController(GroupApiClient groupApiClient, GroupContextService groupContextService) {
        this.groupApiClient = groupApiClient;
        this.groupContextService = groupContextService;
    }

    @GetMapping("/groups")
    String list(Model model) {
        model.addAttribute("groups", groupApiClient.listGroups());
        return "groups/list";
    }

    @GetMapping("/groups/new")
    String createForm(@ModelAttribute("groupForm") GroupForm form, Model model) {
        model.addAttribute("editing", false);
        return "groups/form";
    }

    @PostMapping("/groups")
    String create(
            @Valid @ModelAttribute("groupForm") GroupForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "groups/form";
        }

        GroupResponse created;
        try {
            created = groupApiClient.createGroup(new CreateGroupRequest(form.getName()));
        } catch (ApiValidationException ex) {
            model.addAttribute("errorMessage", "The API rejected this group: " + ex.getMessage());
            return "groups/form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Group created.");
        return "redirect:/groups/" + created.groupId();
    }

    @GetMapping("/groups/{groupId}")
    String detail(@PathVariable UUID groupId, Model model) {
        model.addAttribute("group", groupApiClient.getGroup(groupId));
        model.addAttribute("permissions", groupContextService.permissions(groupId));
        return "groups/detail";
    }

    @GetMapping("/groups/{groupId}/edit")
    String editForm(@PathVariable UUID groupId, Model model) {
        GroupResponse group = groupApiClient.getGroup(groupId);
        model.addAttribute("groupForm", new GroupForm(group.name()));
        model.addAttribute("groupId", groupId);
        model.addAttribute("editing", true);
        return "groups/form";
    }

    @PostMapping("/groups/{groupId}")
    String update(
            @PathVariable UUID groupId,
            @Valid @ModelAttribute("groupForm") GroupForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("groupId", groupId);
            model.addAttribute("editing", true);
            return "groups/form";
        }

        try {
            groupApiClient.renameGroup(groupId, new RenameGroupRequest(form.getName()));
        } catch (ApiValidationException ex) {
            model.addAttribute("errorMessage", "The API rejected this update: " + ex.getMessage());
            model.addAttribute("groupId", groupId);
            model.addAttribute("editing", true);
            return "groups/form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Group updated.");
        return "redirect:/groups/" + groupId;
    }

    @GetMapping("/groups/{groupId}/delete")
    String deleteConfirm(@PathVariable UUID groupId, Model model) {
        model.addAttribute("group", groupApiClient.getGroup(groupId));
        return "groups/delete-confirm";
    }

    @PostMapping("/groups/{groupId}/delete")
    String delete(@PathVariable UUID groupId, RedirectAttributes redirectAttributes) {
        groupApiClient.deleteGroup(groupId);
        redirectAttributes.addFlashAttribute("successMessage", "Group deleted.");
        return "redirect:/groups";
    }
}
