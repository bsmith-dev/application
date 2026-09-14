package app.prompts.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import app.prompts.domain.model.GroupId;
import app.prompts.domain.model.MemberId;
import app.prompts.domain.model.Prompt;
import app.prompts.domain.model.PromptId;
import app.prompts.domain.model.Role;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultPromptAuthorizationPolicyTest {
    private final PromptAuthorizationPolicy policy = new DefaultPromptAuthorizationPolicy();

    @DisplayName("Owner can modify own prompt")
    @Test
    void ownerCanModifyOwnPrompt() {
        MemberId owner = MemberId.newId();
        Prompt prompt = new Prompt(PromptId.newId(), GroupId.newId(), owner, "Title", "Content", Instant.now());

        assertTrue(policy.canModify(prompt, owner, Role.MEMBER));
    }

    @DisplayName("Group lead can modify another member's prompt")
    @Test
    void groupLeadCanModifyOthersPrompt() {
        Prompt prompt = new Prompt(PromptId.newId(), GroupId.newId(), MemberId.newId(), "Title", "Content", Instant.now());

        assertTrue(policy.canModify(prompt, MemberId.newId(), Role.GROUP_LEAD));
    }

    @DisplayName("Admin can modify another member's prompt")
    @Test
    void adminCanModifyOthersPrompt() {
        Prompt prompt = new Prompt(PromptId.newId(), GroupId.newId(), MemberId.newId(), "Title", "Content", Instant.now());

        assertTrue(policy.canModify(prompt, MemberId.newId(), Role.ADMIN));
    }

    @DisplayName("Plain member cannot modify another member's prompt")
    @Test
    void plainMemberCannotModifyOthersPrompt() {
        Prompt prompt = new Prompt(PromptId.newId(), GroupId.newId(), MemberId.newId(), "Title", "Content", Instant.now());

        assertFalse(policy.canModify(prompt, MemberId.newId(), Role.MEMBER));
    }

    @DisplayName("Null role cannot modify prompt")
    @Test
    void nullRoleCannotModifyPrompt() {
        Prompt prompt = new Prompt(PromptId.newId(), GroupId.newId(), MemberId.newId(), "Title", "Content", Instant.now());

        assertFalse(policy.canModify(prompt, MemberId.newId(), null));
    }
}
