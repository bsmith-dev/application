package app.prompts.api.domain.model;

import java.util.Objects;

public class Organization {
    private final OrganizationId id;
    private String name;

    public Organization(OrganizationId id, String name) {
        this.id = Objects.requireNonNull(id, "Organization id is required");
        rename(name);
    }

    public void rename(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Organization name is required");
        }
        this.name = name;
    }

    public OrganizationId id() {
        return id;
    }

    public String name() {
        return name;
    }
}
