package app.prompts.application.port;

import java.util.List;
import java.util.Optional;
import app.prompts.domain.model.Organization;
import app.prompts.domain.model.OrganizationId;

public interface OrganizationRepository {
    Organization save(Organization organization);

    List<Organization> findAll();

    Optional<Organization> findById(OrganizationId id);

    void deleteById(OrganizationId id);
}
