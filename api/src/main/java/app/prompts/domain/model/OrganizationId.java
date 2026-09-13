package app.prompts.domain.model;

import java.util.UUID;

public record OrganizationId(UUID value) {
    public static OrganizationId newId() {
        return new OrganizationId(UUID.randomUUID());
    }
}
