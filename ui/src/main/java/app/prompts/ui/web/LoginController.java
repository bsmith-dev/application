package app.prompts.ui.web;

import app.prompts.ui.api.ApiUnauthorizedException;
import app.prompts.ui.api.ApiUnavailableException;
import app.prompts.ui.api.ApiValidationException;
import app.prompts.ui.form.LoginForm;
import app.prompts.ui.security.CurrentApiSession;
import app.prompts.ui.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.ResourceAccessException;

@Controller
public class LoginController {

    private final AuthenticationService authenticationService;
    private final CurrentApiSession currentApiSession;

    public LoginController(AuthenticationService authenticationService, CurrentApiSession currentApiSession) {
        this.authenticationService = authenticationService;
        this.currentApiSession = currentApiSession;
    }

    @GetMapping("/login")
    String loginPage(
            @RequestParam(value = "redirectTo", required = false) String redirectTo,
            @RequestParam(value = "expired", required = false) String expired,
            @RequestParam(value = "loggedOut", required = false) String loggedOut,
            @ModelAttribute("loginForm") LoginForm loginForm,
            Model model
    ) {
        if (currentApiSession.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        model.addAttribute("redirectTo", redirectTo);
        if (expired != null) {
            model.addAttribute("infoMessage", "Your session expired. Please sign in again.");
        } else if (loggedOut != null) {
            model.addAttribute("infoMessage", "You have been signed out.");
        }
        return "login";
    }

    @PostMapping("/login")
    String login(
            @Valid @ModelAttribute("loginForm") LoginForm loginForm,
            BindingResult bindingResult,
            @RequestParam(value = "redirectTo", required = false) String redirectTo,
            Model model,
            HttpServletRequest request
    ) {
        if (bindingResult.hasErrors()) {
            return "login";
        }

        try {
            authenticationService.login(loginForm.getUsername(), loginForm.getPassword());
            request.changeSessionId();
        } catch (ApiUnauthorizedException ex) {
            bindingResult.reject("login.invalid", "Invalid username or password.");
            return "login";
        } catch (ApiValidationException ex) {
            model.addAttribute("errorMessage", "Login request was rejected: " + ex.getMessage());
            return "login";
        } catch (ApiUnavailableException ex) {
            model.addAttribute("errorMessage", "The API is currently unavailable. Please try again shortly.");
            return "login";
        } catch (ResourceAccessException ex) {
            model.addAttribute("errorMessage",
                    "Could not reach the API. Check that it is running and that app.api.base-url is correct.");
            return "login";
        }

        if (StringUtils.hasText(redirectTo) && redirectTo.startsWith("/") && !redirectTo.startsWith("//")) {
            return "redirect:" + redirectTo;
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    String logout(HttpServletRequest request) {
        authenticationService.logout();
        request.getSession().invalidate();
        return "redirect:/login?loggedOut";
    }
}
