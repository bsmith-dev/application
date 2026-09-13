-- V1: Baseline schema for prompt-db MVP

CREATE TABLE organizations (
    id         CHAR(36)     PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_organizations_name UNIQUE (name)
);

CREATE TABLE members (
    id              CHAR(36)     PRIMARY KEY,
    organization_id CHAR(36)     NOT NULL,
    username        VARCHAR(100) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_members_username UNIQUE (username),
    CONSTRAINT uq_members_email UNIQUE (email),
    CONSTRAINT fk_members_organization
        FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX idx_members_organization_id ON members (organization_id);

CREATE TABLE groups (
    id              CHAR(36)     PRIMARY KEY,
    organization_id CHAR(36)     NOT NULL,
    name            VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_groups_name_per_org UNIQUE (organization_id, name),
    CONSTRAINT fk_groups_organization
        FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX idx_groups_organization_id ON groups (organization_id);

CREATE TABLE group_memberships (
    id        CHAR(36)    PRIMARY KEY,
    group_id  CHAR(36)    NOT NULL,
    member_id CHAR(36)    NOT NULL,
    role      VARCHAR(20) NOT NULL,
    CONSTRAINT uq_group_memberships_group_member UNIQUE (group_id, member_id),
    CONSTRAINT fk_group_memberships_group
        FOREIGN KEY (group_id) REFERENCES groups(id),
    CONSTRAINT fk_group_memberships_member
        FOREIGN KEY (member_id) REFERENCES members(id)
);

CREATE INDEX idx_group_memberships_group_id ON group_memberships (group_id);
CREATE INDEX idx_group_memberships_member_id ON group_memberships (member_id);

CREATE TABLE prompts (
    id                   CHAR(36)     PRIMARY KEY,
    group_id             CHAR(36)     NOT NULL,
    created_by_member_id CHAR(36)     NOT NULL,
    title                VARCHAR(500) NOT NULL,
    content              TEXT         NOT NULL,
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_prompts_group
        FOREIGN KEY (group_id) REFERENCES groups(id),
    CONSTRAINT fk_prompts_created_by_member
        FOREIGN KEY (created_by_member_id) REFERENCES members(id)
);

CREATE INDEX idx_prompts_group_id ON prompts (group_id);
CREATE INDEX idx_prompts_created_by_member_id ON prompts (created_by_member_id);
