package com.example.promptdb.service;

import com.example.promptdb.api.AuthApiClient;
import com.example.promptdb.api.MemberApiClient;
import com.example.promptdb.api.dto.LoginRequest;
import com.example.promptdb.api.dto.LoginResponse;
import com.example.promptdb.api.dto.MemberResponse;
import com.example.promptdb.security.CurrentApiSession;
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
