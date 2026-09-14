package app.prompts.ui.web;

import app.prompts.ui.api.ApiValidationException;
import app.prompts.ui.api.GroupApiClient;
import app.prompts.ui.api.PromptApiClient;
import app.prompts.ui.api.dto.CreatePromptRequest;
import app.prompts.ui.api.dto.PromptResponse;
import app.prompts.ui.api.dto.UpdatePromptRequest;
import app.prompts.ui.form.PromptForm;
import app.prompts.ui.security.CurrentApiSession;
import app.prompts.ui.service.GroupContextService;
import app.prompts.ui.support.GroupPermissions;
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

/** Prompt CRUD under /groups/{groupId}/prompts. */
@Controller
public class PromptController {

    private final PromptApiClient promptApiClient;
    private final GroupApiClient groupApiClient;
    private final GroupContextService groupContextService;
    private final CurrentApiSession currentApiSession;

    public PromptController(
            PromptApiClient promptApiClient,
            GroupApiClient groupApiClient,
            GroupContextService groupContextService,
            CurrentApiSession currentApiSession
    ) {
        this.promptApiClient = promptApiClient;
        this.groupApiClient = groupApiClient;
        this.groupContextService = groupContextService;
        this.currentApiSession = currentApiSession;
    }

    @GetMapping("/groups/{groupId}/prompts")
    String list(@PathVariable UUID groupId, Model model) {
        model.addAttribute("group", groupApiClient.getGroup(groupId));
        model.addAttribute("prompts", promptApiClient.listPrompts(groupId));
        model.addAttribute("permissions", groupContextService.permissions(groupId));
        return "groups/prompts/list";
    }

    @GetMapping("/groups/{groupId}/prompts/new")
    String createForm(@PathVariable UUID groupId, @ModelAttribute("promptForm") PromptForm form, Model model) {
        model.addAttribute("group", groupApiClient.getGroup(groupId));
        return "groups/prompts/form";
    }

    @PostMapping("/groups/{groupId}/prompts")
    String create(
            @PathVariable UUID groupId,
            @Valid @ModelAttribute("promptForm") PromptForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("group", groupApiClient.getGroup(groupId));
            return "groups/prompts/form";
        }

        PromptResponse created;
        try {
            created = promptApiClient.createPrompt(groupId, new CreatePromptRequest(form.getTitle(), form.getContent()));
        } catch (ApiValidationException ex) {
            model.addAttribute("group", groupApiClient.getGroup(groupId));
            model.addAttribute("errorMessage", "The API rejected this prompt: " + ex.getMessage());
            return "groups/prompts/form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Prompt created.");
        return "redirect:/groups/" + groupId + "/prompts/" + created.id();
    }

    @GetMapping("/groups/{groupId}/prompts/{promptId}")
    String detail(@PathVariable UUID groupId, @PathVariable UUID promptId, Model model) {
        PromptResponse prompt = promptApiClient.getPrompt(groupId, promptId);
        model.addAttribute("group", groupApiClient.getGroup(groupId));
        model.addAttribute("prompt", prompt);
        model.addAttribute("canEdit", canEdit(groupId, prompt));
        return "groups/prompts/detail";
    }

    @GetMapping("/groups/{groupId}/prompts/{promptId}/edit")
    String editForm(@PathVariable UUID groupId, @PathVariable UUID promptId, Model model) {
        PromptResponse prompt = promptApiClient.getPrompt(groupId, promptId);
        PromptForm form = new PromptForm();
        form.setTitle(prompt.title());
        form.setContent(prompt.content());

        model.addAttribute("group", groupApiClient.getGroup(groupId));
        model.addAttribute("promptId", promptId);
        model.addAttribute("promptForm", form);
        model.addAttribute("editing", true);
        return "groups/prompts/form";
    }

    @PostMapping("/groups/{groupId}/prompts/{promptId}")
    String update(
            @PathVariable UUID groupId,
            @PathVariable UUID promptId,
            @Valid @ModelAttribute("promptForm") PromptForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("group", groupApiClient.getGroup(groupId));
            model.addAttribute("promptId", promptId);
            model.addAttribute("editing", true);
            return "groups/prompts/form";
        }

        try {
            promptApiClient.updatePrompt(groupId, promptId, new UpdatePromptRequest(form.getTitle(), form.getContent()));
        } catch (ApiValidationException ex) {
            model.addAttribute("group", groupApiClient.getGroup(groupId));
            model.addAttribute("promptId", promptId);
            model.addAttribute("editing", true);
            model.addAttribute("errorMessage", "The API rejected this update: " + ex.getMessage());
            return "groups/prompts/form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Prompt updated.");
        return "redirect:/groups/" + groupId + "/prompts/" + promptId;
    }

    @GetMapping("/groups/{groupId}/prompts/{promptId}/delete")
    String deleteConfirm(@PathVariable UUID groupId, @PathVariable UUID promptId, Model model) {
        model.addAttribute("group", groupApiClient.getGroup(groupId));
        model.addAttribute("prompt", promptApiClient.getPrompt(groupId, promptId));
        return "groups/prompts/delete-confirm";
    }

    @PostMapping("/groups/{groupId}/prompts/{promptId}/delete")
    String delete(@PathVariable UUID groupId, @PathVariable UUID promptId, RedirectAttributes redirectAttributes) {
        promptApiClient.deletePrompt(groupId, promptId);
        redirectAttributes.addFlashAttribute("successMessage", "Prompt deleted.");
        return "redirect:/groups/" + groupId + "/prompts";
    }

    private boolean canEdit(UUID groupId, PromptResponse prompt) {
        var currentMember = currentApiSession.currentMember().orElse(null);
        var membershipRole = groupContextService.membershipRole(groupId);
        return GroupPermissions.canEditPrompt(currentMember, membershipRole, prompt.createdBy());
    }
}
