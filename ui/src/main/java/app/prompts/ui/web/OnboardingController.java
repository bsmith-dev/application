package app.prompts.ui.web;

import app.prompts.ui.api.ApiUnavailableException;
import app.prompts.ui.api.ApiValidationException;
import app.prompts.ui.api.MemberApiClient;
import app.prompts.ui.api.OrganizationApiClient;
import app.prompts.ui.api.dto.CreateOrganizationRequest;
import app.prompts.ui.api.dto.OrganizationResponse;
import app.prompts.ui.api.dto.RegisterMemberRequest;
import app.prompts.ui.form.OrganizationForm;
import app.prompts.ui.form.RegisterMemberForm;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Public, unauthenticated routes for getting started: creating the first organization and
 * registering a member into it. Both {@code POST /api/organizations} and {@code POST
 * /api/members} require no auth per the API's access rules, so these screens are reachable
 * before login.
 */
@Controller
public class OnboardingController {

    private final OrganizationApiClient organizationApiClient;
    private final MemberApiClient memberApiClient;

    public OnboardingController(OrganizationApiClient organizationApiClient, MemberApiClient memberApiClient) {
        this.organizationApiClient = organizationApiClient;
        this.memberApiClient = memberApiClient;
    }

    @GetMapping("/organizations/bootstrap")
    String bootstrapForm(@ModelAttribute("organizationForm") OrganizationForm form) {
        return "organizations/bootstrap";
    }

    @PostMapping("/organizations/bootstrap")
    String bootstrap(
            @Valid @ModelAttribute("organizationForm") OrganizationForm form,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "organizations/bootstrap";
        }

        try {
            OrganizationResponse created = organizationApiClient.createOrganization(new CreateOrganizationRequest(form.getName()));
            model.addAttribute("createdOrganization", created);
            model.addAttribute("registerForm", new RegisterMemberForm());
            return "organizations/bootstrap";
        } catch (ApiValidationException ex) {
            model.addAttribute("errorMessage", "The API rejected this organization: " + ex.getMessage());
            return "organizations/bootstrap";
        } catch (ApiUnavailableException | ResourceAccessException ex) {
            model.addAttribute("errorMessage",
                    "Could not reach the API. Check that it is running and that app.api.base-url is correct.");
            return "organizations/bootstrap";
        }
    }

    @GetMapping("/register")
    String registerForm(
            @RequestParam(value = "organizationId", required = false) String organizationId,
            Model model
    ) {
        RegisterMemberForm form = new RegisterMemberForm();
        if (organizationId != null) {
            form.setOrganizationId(organizationId);
        }
        model.addAttribute("registerForm", form);
        return "register";
    }

    @PostMapping("/register")
    String register(
            @Valid @ModelAttribute("registerForm") RegisterMemberForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            memberApiClient.register(new RegisterMemberRequest(
                    form.getUsername(),
                    form.getPassword(),
                    form.getEmail(),
                    UUID.fromString(form.getOrganizationId())
            ));
        } catch (ApiValidationException ex) {
            model.addAttribute("errorMessage", "Registration was rejected: " + ex.getMessage());
            return "register";
        } catch (ApiUnavailableException | ResourceAccessException ex) {
            model.addAttribute("errorMessage",
                    "Could not reach the API. Check that it is running and that app.api.base-url is correct.");
            return "register";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Account created. Sign in to continue.");
        return "redirect:/login";
    }
}
