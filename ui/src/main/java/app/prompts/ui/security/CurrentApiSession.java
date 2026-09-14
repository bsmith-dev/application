package app.prompts.ui.security;

import app.prompts.ui.api.dto.MemberResponse;
import java.io.Serializable;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/**
 * Holds the bearer token and current member profile for the logged-in browser session.
 * Session-scoped so each HTTP session gets its own instance; injected as a scoped proxy
 * into singleton beans such as the API clients and the bearer-token interceptor.
 */
@Component
@SessionScope
public class CurrentApiSession implements Serializable {

    private String accessToken;
    private MemberResponse currentMember;

    public Optional<String> accessToken() {
        return Optional.ofNullable(accessToken);
    }

    public Optional<MemberResponse> currentMember() {
        return Optional.ofNullable(currentMember);
    }

    public boolean isAuthenticated() {
        return accessToken != null && currentMember != null;
    }

    public void authenticate(String accessToken, MemberResponse currentMember) {
        this.accessToken = accessToken;
        this.currentMember = currentMember;
    }

    public void updateCurrentMember(MemberResponse currentMember) {
        this.currentMember = currentMember;
    }

    public void clear() {
        this.accessToken = null;
        this.currentMember = null;
    }
}
