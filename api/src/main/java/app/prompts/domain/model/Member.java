package app.prompts.domain.model;

import java.util.Objects;

public class Member {
    private final MemberId id;
    private final OrganizationId organizationId;
    private String username;
    private String email;

    public Member(MemberId id, OrganizationId organizationId, String username, String email) {
        this.id = Objects.requireNonNull(id, "Member id is required");
        this.organizationId = Objects.requireNonNull(organizationId, "Organization id is required");
        changeUsername(username);
        changeEmail(email);
    }

    public void changeUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        this.username = username;
    }

    public void changeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        this.email = email;
    }

    public MemberId id() {
        return id;
    }

    public OrganizationId organizationId() {
        return organizationId;
    }

    public String username() {
        return username;
    }

    public String email() {
        return email;
    }
}
