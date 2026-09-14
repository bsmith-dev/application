package app.prompts.api.domain.service;

import app.prompts.api.domain.model.MemberId;
import app.prompts.api.domain.model.Prompt;
import app.prompts.api.domain.model.Role;

public interface PromptAuthorizationPolicy {
    boolean canModify(Prompt prompt, MemberId requesterId, Role role);
}
