package app.prompts.api.application.port;

import java.util.List;
import java.util.UUID;

public interface TokenService {
    String generateToken(UUID memberId, UUID organizationId, String username, List<String> authorities);
}
