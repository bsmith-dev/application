package com.example.promptdb.web;

import com.example.promptdb.security.CurrentApiSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final CurrentApiSession currentApiSession;

    public DashboardController(CurrentApiSession currentApiSession) {
        this.currentApiSession = currentApiSession;
    }

    @GetMapping("/dashboard")
    String dashboard(Model model) {
        model.addAttribute("member", currentApiSession.currentMember().orElse(null));
        return "dashboard";
    }
}
