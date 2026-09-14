package com.example.promptdb.web;

import com.example.promptdb.security.CurrentApiSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final CurrentApiSession currentApiSession;

    public HomeController(CurrentApiSession currentApiSession) {
        this.currentApiSession = currentApiSession;
    }

    @GetMapping("/")
    String home() {
        return currentApiSession.isAuthenticated() ? "redirect:/dashboard" : "redirect:/login";
    }
}
