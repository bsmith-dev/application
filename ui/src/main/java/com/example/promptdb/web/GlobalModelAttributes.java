package com.example.promptdb.web;

import com.example.promptdb.api.dto.MemberResponse;
import com.example.promptdb.api.dto.Role;
import com.example.promptdb.security.CurrentApiSession;
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
