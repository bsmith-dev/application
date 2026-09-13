package app.prompts.domain.service;

import app.prompts.domain.model.MemberId;
import app.prompts.domain.model.Prompt;
import app.prompts.domain.model.Role;

public class DefaultPromptAuthorizationPolicy implements PromptAuthorizationPolicy {
    @Override
    public boolean canModify(Prompt prompt, MemberId requesterId, Role role) {
        if (role == Role.ADMIN || role == Role.GROUP_LEAD) {
            return true;
        }
        if (role == Role.MEMBER) {
            return prompt.createdByMemberId().equals(requesterId);
        }
        return false;
    }
}
