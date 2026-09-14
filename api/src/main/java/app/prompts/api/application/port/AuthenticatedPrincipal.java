package app.prompts.api.application.port;

import java.security.Principal;
import java.util.UUID;

/**
 * Marker interface for the authenticated principal resolved from the JWT.
 * Lives in the application layer so that presentation controllers can use it
 * without depending on any infrastructure type.
 */
public interface AuthenticatedPrincipal extends Principal {
    UUID getMemberId();
    UUID getOrganizationId();
}
