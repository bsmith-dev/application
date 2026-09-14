package app.prompts.ui.web;

import app.prompts.ui.api.dto.MemberResponse;
import app.prompts.ui.api.dto.Role;
import app.prompts.ui.security.CurrentApiSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Makes the current member and their admin flag available to every Thymeleaf template. */
@ControllerAdvice
public class GlobalModelAttributes {

    private final CurrentApiSession currentApiSession;

    public GlobalModelAttributes(CurrentApiSession currentApiSession) {
        this.currentApiSession = currentApiSession;
    }

    @ModelAttribute
    public void populate(Model model) {
        currentApiSession.currentMember().ifPresent(member -> {
            model.addAttribute("currentMember", member);
            model.addAttribute("isAdmin", member.role() == Role.ADMIN);
        });
        model.addAttribute("authenticated", currentApiSession.isAuthenticated());
    }

    @ModelAttribute("currentMemberResponse")
    public MemberResponse currentMemberResponse() {
        return currentApiSession.currentMember().orElse(null);
    }
}
