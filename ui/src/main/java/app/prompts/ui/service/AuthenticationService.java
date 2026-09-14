package app.prompts.ui.service;

import app.prompts.ui.api.AuthApiClient;
import app.prompts.ui.api.MemberApiClient;
import app.prompts.ui.api.dto.LoginRequest;
import app.prompts.ui.api.dto.LoginResponse;
import app.prompts.ui.api.dto.MemberResponse;
import app.prompts.ui.security.CurrentApiSession;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final AuthApiClient authApiClient;
    private final MemberApiClient memberApiClient;
    private final CurrentApiSession currentApiSession;

    public AuthenticationService(
            AuthApiClient authApiClient,
            MemberApiClient memberApiClient,
            CurrentApiSession currentApiSession
    ) {
        this.authApiClient = authApiClient;
        this.memberApiClient = memberApiClient;
        this.currentApiSession = currentApiSession;
    }

    /**
     * Logs in, then immediately fetches the current member profile so the UI knows the member's
     * id and role for permission checks. The token is stored first so the /me call (made through
     * the authenticated RestClient) already carries the bearer header.
     */
    public MemberResponse login(String username, String password) {
        LoginResponse loginResponse = authApiClient.login(new LoginRequest(username, password));
        currentApiSession.authenticate(loginResponse.token(), null);

        MemberResponse currentMember = memberApiClient.getCurrentMember();
        currentApiSession.updateCurrentMember(currentMember);
        return currentMember;
    }

    public void logout() {
        currentApiSession.clear();
    }
}
