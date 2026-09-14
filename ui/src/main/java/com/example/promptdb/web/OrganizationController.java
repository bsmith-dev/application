package com.example.promptdb.web;

import com.example.promptdb.api.ApiConflictException;
import com.example.promptdb.api.ApiValidationException;
import com.example.promptdb.api.OrganizationApiClient;
import com.example.promptdb.api.dto.CreateOrganizationRequest;
import com.example.promptdb.api.dto.OrganizationResponse;
import com.example.promptdb.api.dto.RenameOrganizationRequest;
import com.example.promptdb.form.OrganizationForm;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Authenticated organization management: list, create additional organizations, rename, delete.
 * The API has no GET-by-id endpoint, so "edit" resolves the organization from the list response.
 * UI visibility is restricted to ADMIN members (see fragments/navigation.html); the API is the
 * real enforcement point for every mutation here.
 */
@Controller
public class OrganizationController {

    private final OrganizationApiClient organizationApiClient;

    public OrganizationController(OrganizationApiClient organizationApiClient) {
        this.organizationApiClient = organizationApiClient;
    }

    @GetMapping("/organizations")
    String list(Model model) {
        List<OrganizationResponse> organizations = organizationApiClient.listOrganizations();
        model.addAttribute("organizations", organizations);
        return "organizations/list";
    }

    @GetMapping("/organizations/new")
    String createForm(@ModelAttribute("organizationForm") OrganizationForm form, Model model) {
        model.addAttribute("editing", false);
        return "organizations/form";
    }

    @PostMapping("/organizations")
    String create(
            @Valid @ModelAttribute("organizationForm") OrganizationForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("editing", false);
            return "organizations/form";
        }

        try {
            organizationApiClient.createOrganization(new CreateOrganizationRequest(form.getName()));
        } catch (ApiValidationException ex) {
            model.addAttribute("errorMessage", "The API rejected this organization: " + ex.getMessage());
            model.addAttribute("editing", false);
            return "organizations/form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Organization created.");
        return "redirect:/organizations";
    }

    @GetMapping("/organizations/{organizationId}/edit")
    String editForm(@PathVariable UUID organizationId, Model model) {
        OrganizationResponse organization = findOrThrow(organizationId);
        model.addAttribute("organizationForm", new OrganizationForm(organization.name()));
        model.addAttribute("organizationId", organizationId);
        model.addAttribute("editing", true);
        return "organizations/form";
    }

    @PostMapping("/organizations/{organizationId}")
    String update(
            @PathVariable UUID organizationId,
            @Valid @ModelAttribute("organizationForm") OrganizationForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("organizationId", organizationId);
            model.addAttribute("editing", true);
            return "organizations/form";
        }

        try {
            organizationApiClient.renameOrganization(organizationId, new RenameOrganizationRequest(form.getName()));
        } catch (ApiValidationException ex) {
            model.addAttribute("errorMessage", "The API rejected this update: " + ex.getMessage());
            model.addAttribute("organizationId", organizationId);
            model.addAttribute("editing", true);
            return "organizations/form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Organization updated.");
        return "redirect:/organizations";
    }

    @GetMapping("/organizations/{organizationId}/delete")
    String deleteConfirm(@PathVariable UUID organizationId, Model model) {
        model.addAttribute("organization", findOrThrow(organizationId));
        return "organizations/delete-confirm";
    }

    @PostMapping("/organizations/{organizationId}/delete")
    String delete(@PathVariable UUID organizationId, Model model, RedirectAttributes redirectAttributes) {
        try {
            organizationApiClient.deleteOrganization(organizationId);
        } catch (ApiConflictException ex) {
            model.addAttribute("organization", findOrThrow(organizationId));
            model.addAttribute("conflictMessage",
                    "This organization cannot be deleted because it still has members or groups. " + ex.getMessage());
            return "organizations/delete-confirm";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Organization deleted.");
        return "redirect:/organizations";
    }

    private OrganizationResponse findOrThrow(UUID organizationId) {
        return organizationApiClient.listOrganizations().stream()
                .filter(org -> organizationId.equals(org.organizationId()))
                .findFirst()
                .orElseThrow(() -> new com.example.promptdb.api.ApiNotFoundException(
                        "Organization " + organizationId + " was not found in the list returned by the API."));
    }
}
