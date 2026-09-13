package app.prompts.application.port;

import java.util.List;
import java.util.Optional;
import app.prompts.domain.model.Group;
import app.prompts.domain.model.GroupId;
import app.prompts.domain.model.OrganizationId;

public interface GroupRepository {
    Group save(Group group);

    List<Group> findAll();

    Optional<Group> findById(GroupId id);

    /**
     * Finds a group by id scoped to the given organization.
     * Returns empty if the group does not exist or belongs to a different organization,
     * preventing cross-org IDOR leakage.
     */
    Optional<Group> findByIdAndOrganizationId(GroupId id, OrganizationId organizationId);

    void deleteById(GroupId id);
}
