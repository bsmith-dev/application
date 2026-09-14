package com.example.promptdb.web;

import com.example.promptdb.api.MemberApiClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Read-only member directory backed by GET /api/members (requires a session). */
@Controller
public class MemberController {

    private final MemberApiClient memberApiClient;

    public MemberController(MemberApiClient memberApiClient) {
        this.memberApiClient = memberApiClient;
    }

    @GetMapping("/members")
    String list(Model model) {
        model.addAttribute("members", memberApiClient.listMembers());
        return "members/list";
    }
}
