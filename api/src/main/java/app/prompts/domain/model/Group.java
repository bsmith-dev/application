package app.prompts.domain.model;

import java.util.Objects;

public class Group {
    private final GroupId id;
    private final OrganizationId organizationId;
    private String name;

    public Group(GroupId id, OrganizationId organizationId, String name) {
        this.id = Objects.requireNonNull(id, "Group id is required");
        this.organizationId = Objects.requireNonNull(organizationId, "Organization id is required");
        rename(name);
    }

    public void rename(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Group name is required");
        }
        this.name = name;
    }

    public GroupId id() {
        return id;
    }

    public OrganizationId organizationId() {
        return organizationId;
    }

    public String name() {
        return name;
    }
}
