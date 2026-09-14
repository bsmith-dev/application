package app.prompts.api.domain.service;

import app.prompts.api.domain.model.MemberId;
import app.prompts.api.domain.model.Prompt;
import app.prompts.api.domain.model.Role;

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
