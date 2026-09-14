package app.prompts.api.application.port;

import java.util.List;
import java.util.UUID;
import app.prompts.api.application.dto.CreateOrganizationCommand;
import app.prompts.api.application.dto.OrganizationResult;
import app.prompts.api.application.dto.RenameOrganizationCommand;

/**
 * Inbound use-case port: organization management.
 */
public interface ManageOrganizationUseCase {
    OrganizationResult createOrganization(CreateOrganizationCommand command);

    List<OrganizationResult> listOrganizations(UUID requesterId);

    OrganizationResult renameOrganization(RenameOrganizationCommand command);

    void deleteOrganization(UUID organizationId, UUID requesterId);
}
