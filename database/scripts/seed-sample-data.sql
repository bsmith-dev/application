INSERT INTO organizations (id, name) VALUES
    ('11111111-1111-1111-1111-111111111111', 'Acme Labs')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO members (id, organization_id, username, password_hash, email) VALUES
    ('22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'admin', '$2a$10$devsamplepasswordhash', 'admin@example.com'),
    ('33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111', 'alice', '$2a$10$devsamplepasswordhash', 'alice@example.com')
ON DUPLICATE KEY UPDATE
    organization_id = VALUES(organization_id),
    username = VALUES(username),
    password_hash = VALUES(password_hash),
    email = VALUES(email);

INSERT INTO groups (id, organization_id, name) VALUES
    ('44444444-4444-4444-4444-444444444444', '11111111-1111-1111-1111-111111111111', 'platform')
ON DUPLICATE KEY UPDATE
    organization_id = VALUES(organization_id),
    name = VALUES(name);

INSERT INTO group_memberships (id, group_id, member_id, role) VALUES
    ('55555555-5555-5555-5555-555555555555', '44444444-4444-4444-4444-444444444444', '22222222-2222-2222-2222-222222222222', 'ADMIN'),
    ('66666666-6666-6666-6666-666666666666', '44444444-4444-4444-4444-444444444444', '33333333-3333-3333-3333-333333333333', 'MEMBER')
ON DUPLICATE KEY UPDATE
    group_id = VALUES(group_id),
    member_id = VALUES(member_id),
    role = VALUES(role);

INSERT INTO prompts (id, group_id, created_by_member_id, title, content) VALUES
    ('77777777-7777-7777-7777-777777777777', '44444444-4444-4444-4444-444444444444', '22222222-2222-2222-2222-222222222222', 'Welcome prompt', 'Summarize the purpose of this project for a new teammate.'),
    ('88888888-8888-8888-8888-888888888888', '44444444-4444-4444-4444-444444444444', '33333333-3333-3333-3333-333333333333', 'Release checklist', 'List the checks required before releasing the application.')
ON DUPLICATE KEY UPDATE
    group_id = VALUES(group_id),
    created_by_member_id = VALUES(created_by_member_id),
    title = VALUES(title),
    content = VALUES(content);
