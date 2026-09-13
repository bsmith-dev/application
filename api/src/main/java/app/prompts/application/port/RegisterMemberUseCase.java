package app.prompts.application.port;

import app.prompts.application.dto.MemberResult;
import app.prompts.application.dto.RegisterMemberCommand;

/**
 * Inbound use-case port: member self-registration.
 */
public interface RegisterMemberUseCase {

    MemberResult registerMember(RegisterMemberCommand command);
}
