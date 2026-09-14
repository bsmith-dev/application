package app.prompts.api.application.port;

import app.prompts.api.application.dto.MemberResult;
import app.prompts.api.application.dto.RegisterMemberCommand;

/**
 * Inbound use-case port: member self-registration.
 */
public interface RegisterMemberUseCase {

    MemberResult registerMember(RegisterMemberCommand command);
}
