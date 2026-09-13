package app.prompts.domain.service;

import app.prompts.domain.model.MemberId;
import app.prompts.domain.model.Prompt;
import app.prompts.domain.model.Role;

public interface PromptAuthorizationPolicy {
    boolean canModify(Prompt prompt, MemberId requesterId, Role role);
}
