# prompt-db

A multi-tenant REST API for storing and managing AI prompts, built with Spring Boot following Onion Architecture principles. Members belong to organizations, organize into groups, and collaboratively manage a library of reusable prompts.

## Contents

- [Requirements](#requirements)
- [Quick start](#quick-start)
- [Configuration](#configuration)
- [Project structure](#project-structure)
- [Architecture](#architecture)
- [API reference](#api-reference)
- [Data model](#data-model)
- [Sample seed data](#sample-seed-data)
- [Backup and restore](#backup-and-restore)
- [Authorization rules](#authorization-rules)
- [Development](#development)
- [Testing](#testing)

---

## Requirements

| Tool | Version | Purpose |
|---|---|---|
| Java | 21+ | Runtime and compilation |
| Maven wrapper | included (`./mvnw`) | Build |
| Docker | any recent | MariaDB container |
| just | 1.x (`brew install just`) | Task runner |
| jq | any (`brew install jq`) | Smoke test JSON parsing |
| Node.js / npx | any recent | HTTP request tests via `httpyac` |

---

## Quick start

```bash
# 1. Copy environment template and review defaults
cp .env.example .env

# 2. Start the database, wait for it to be ready, then start the application
just start
```

The application starts on `http://localhost:8080`.

To run the HTTP request workflow against a running instance:

```bash
just test-http
```

To run the full local lifecycle gate from a clean database:

```bash
just ci
```

---

## Configuration

Environment variables are loaded from `.env` in the project root. Copy `.env.example` to get started:

```bash
cp .env.example .env
```

| Variable | Default (dev) | Description |
|---|---|---|
| `DB_URL` | `jdbc:mariadb://localhost:3306/sandboxdb` | JDBC connection URL |
| `DB_USERNAME` | `root` | Database username |
| `DB_PASSWORD` | `toor` | Database password |
| `JWT_SECRET` | `promptdb-dev-secret-32-chars-2026` | HMAC-SHA signing key (min 32 chars) |
| `JWT_EXPIRATION_SECONDS` | `3600` | Token lifetime in seconds |

> **Note:** The default `JWT_SECRET` is intentionally insecure. Set a strong random value for any shared or production environment.

`.env` is listed in `.gitignore` and will not be committed. `.env.example` is tracked and shows the required variable names.

### Database

The MariaDB container is defined in `docker-compose.yml`. Data is persisted in a named volume (`db_data`) so it survives container restarts. Use `just db-destroy` to wipe the volume and start fresh.

Schema migrations are managed by [Flyway](https://flywaydb.org/) and run automatically on application startup (`spring.flyway.enabled=true`). Migration files live in `src/main/resources/db/migration/`.

To create or update the schema manually without starting the application, start the database and run Flyway through Maven:

```bash
just db-up
just db-wait
just db-migrate
```

`just db-migrate` loads `.env` through the justfile and runs `./mvnw flyway:migrate` using `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. The Maven plugin is configured with Flyway's MySQL/MariaDB support and MariaDB Connector/J for the default `jdbc:mariadb://...` URL.

Sample data is kept out of Flyway so it never runs automatically in production-like environments. Use `scripts/seed-sample-data.sql` or `just db-seed-sample` only against a local or disposable database.

---

## Project structure

```
src/
  main/
    java/org/sandbox/prompts/
      domain/          # Pure domain model — no framework dependencies
        model/         # Entities, value objects, enums (Member, Group, Prompt, Role, …)
        service/       # Domain policy interfaces and default implementations
      application/     # Use-case orchestration and port definitions
        dto/           # Request/response/command/result records
        port/          # Inbound use-case interfaces and outbound repository interfaces
        service/       # Use-case service implementations, exception types
      infrastructure/  # Framework adapters — Spring, JPA, JWT
        config/        # SecurityConfig, PromptsInfrastructureConfig
        persistence/   # JPA entities, Spring Data repositories, persistence adapters
        security/      # JwtService, JwtAuthFilter, LoginService, MemberUserDetails
      presentation/    # HTTP layer
        rest/          # Controllers, PromptsExceptionHandler
    resources/
      db/migration/    # Flyway SQL migrations (V1–V3)
      application.properties
      static/          # Browser UI (index.html, app.html, app.js, style.css)
  test/
    java/org/sandbox/
      ArchitectureComplianceTest.java   # ArchUnit ring-dependency rules
      prompts/
        domain/        # Domain model and policy unit tests
        application/   # Service unit tests (Mockito)
        presentation/  # Controller MockMvc tests (standalone + JWT filter)
        infrastructure/security/  # JwtService and JwtAuthFilter unit tests
scripts/
  seed-sample-data.sql  # Manual local/dev sample dataset
```

---

## Architecture

The project enforces [Onion Architecture](https://jeffreypalermo.com/2008/07/the-onion-architecture-part-1/) with four rings. Dependency flow is strictly inward — outer rings may depend on inner rings, but inner rings have zero knowledge of outer rings.

```
┌───────────────────────────────────────┐
│  presentation  (controllers, DTOs)    │
│  ┌─────────────────────────────────┐  │
│  │  infrastructure  (JPA, JWT, …)  │  │
│  │  ┌───────────────────────────┐  │  │
│  │  │  application  (use cases, │  │  │
│  │  │  ports, services)         │  │  │
│  │  │  ┌─────────────────────┐  │  │  │
│  │  │  │  domain  (pure Java) │  │  │  │
│  │  │  └─────────────────────┘  │  │  │
│  │  └───────────────────────────┘  │  │
│  └─────────────────────────────────┘  │
└───────────────────────────────────────┘
```

Ring rules are enforced at build time by `ArchitectureComplianceTest` using [ArchUnit](https://www.archunit.org/). The test will fail if any class imports a type from an outer ring.

### Authentication

The API is stateless. All protected endpoints require a JWT passed as `Authorization: Bearer <token>`. Tokens are issued by `POST /api/auth/login` and expire after `JWT_EXPIRATION_SECONDS` seconds (default 1 hour).

The JWT payload includes:

| Claim | Value |
|---|---|
| `sub` | Member UUID |
| `memberId` | Member UUID (redundant, for convenience) |
| `organizationId` | Organization UUID |
| `username` | Username string |
| `authorities` | Array of role strings, e.g. `["ADMIN"]` |

### Multi-tenancy

Every authenticated operation is scoped to the caller's organization. Groups and members from other organizations are never returned, and cross-organization access attempts return 404 (not 403) to avoid leaking whether a resource exists.

---

## API reference

### Public endpoints (no authentication required)

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/organizations` | Create an organization |
| `POST` | `/api/members` | Register a new member |
| `POST` | `/api/auth/login` | Authenticate and receive a JWT |

### Authenticated endpoints (requires `Authorization: Bearer <token>`)

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/organizations` | List organizations (ADMIN only) |
| `PUT` | `/api/organizations/{organizationId}` | Rename an organization (ADMIN only) |
| `DELETE` | `/api/organizations/{organizationId}` | Delete an empty organization (ADMIN only) |
| `GET` | `/api/members` | List members in your organization |
| `GET` | `/api/members/me` | Get your own member profile |
| `GET` | `/api/groups` | List groups you belong to |
| `POST` | `/api/groups` | Create a group |
| `GET` | `/api/groups/{groupId}` | Get a group by ID |
| `PUT` | `/api/groups/{groupId}` | Rename a group |
| `DELETE` | `/api/groups/{groupId}` | Delete a group and all its contents |
| `GET` | `/api/groups/{groupId}/members` | List members of a group |
| `POST` | `/api/groups/{groupId}/members` | Add a member to a group |
| `PUT` | `/api/groups/{groupId}/members/{memberId}` | Change a member's role |
| `DELETE` | `/api/groups/{groupId}/members/{memberId}` | Remove a member from a group |
| `GET` | `/api/groups/{groupId}/prompts` | List prompts in a group |
| `POST` | `/api/groups/{groupId}/prompts` | Create a prompt |
| `GET` | `/api/groups/{groupId}/prompts/{promptId}` | Get a prompt by ID |
| `PUT` | `/api/groups/{groupId}/prompts/{promptId}` | Update a prompt |
| `DELETE` | `/api/groups/{groupId}/prompts/{promptId}` | Delete a prompt |

### Request and response shapes

**Create organization**
```json
POST /api/organizations
{"name": "Acme Corp"}

201 Created
{"organizationId": "uuid", "name": "Acme Corp"}
```

**List organizations**
```json
GET /api/organizations
Authorization: Bearer <admin-token>

200 OK
[{"organizationId": "uuid", "name": "Acme Corp"}]
```

**Rename organization**
```json
PUT /api/organizations/{organizationId}
Authorization: Bearer <admin-token>
{"name": "Acme Corp Updated"}

200 OK
{"organizationId": "uuid", "name": "Acme Corp Updated"}
```

**Delete organization**
```json
DELETE /api/organizations/{organizationId}
Authorization: Bearer <admin-token>

204 No Content
```

Organizations can only be deleted when they contain no members and no groups. A non-empty organization returns `409 Conflict`.

**Register member**
```json
POST /api/members
{
  "username": "alice",
  "email": "alice@example.com",
  "password": "hunter2!!",
  "organizationId": "uuid"
}

201 Created
{"memberId": "uuid", "organizationId": "uuid", "username": "alice", "email": "alice@example.com"}
```

**Login**
```json
POST /api/auth/login
{"username": "alice", "password": "hunter2!!"}

200 OK
{"token": "<jwt>"}
```

**Create group**
```json
POST /api/groups
{"name": "my-group"}

201 Created
{"groupId": "uuid", "organizationId": "uuid", "name": "my-group"}
```

**Add member to group**
```json
POST /api/groups/{groupId}/members
{"memberId": "uuid", "role": "MEMBER"}

201 Created
{"groupId": "uuid", "memberId": "uuid", "role": "MEMBER"}
```

Valid `role` values: `MEMBER`, `GROUP_LEAD`, `ADMIN`

**Create prompt**
```json
POST /api/groups/{groupId}/prompts
{"title": "My prompt", "content": "You are a helpful assistant..."}

201 Created
{
  "id": "uuid",
  "groupId": "uuid",
  "createdBy": "uuid",
  "title": "My prompt",
  "content": "You are a helpful assistant...",
  "createdAt": "2026-09-10T12:00:00Z",
  "updatedAt": "2026-09-10T12:00:00Z"
}
```

### Error responses

Errors follow [RFC 9457 Problem Details](https://www.rfc-editor.org/rfc/rfc9457):

```json
{
  "type": "about:blank",
  "title": "Validation Failed",
  "status": 400,
  "detail": "One or more fields failed validation.",
  "instance": "/api/members",
  "errors": {
    "username": "size must be between 3 and 50"
  }
}
```

| Status | Condition |
|---|---|
| `400` | Validation failure or malformed UUID path parameter |
| `401` | Missing or expired JWT |
| `403` | Authenticated but not authorized for the operation |
| `404` | Resource not found (also returned for cross-org access to prevent enumeration) |
| `409` | Conflict, such as deleting an organization that still has members or groups |

---

## Data model

```
organizations
  id            CHAR(36)    PK
  name          VARCHAR(255) NOT NULL  UNIQUE

members
  id            CHAR(36)    PK
  organization_id CHAR(36)  FK → organizations(id)  NOT NULL
  username      VARCHAR(100) NOT NULL  UNIQUE
  email         VARCHAR(255) NOT NULL  UNIQUE
  password_hash VARCHAR(255) NOT NULL
  created_at    TIMESTAMP   NOT NULL

groups
  id            CHAR(36)    PK
  organization_id CHAR(36)  FK → organizations(id)  NOT NULL
  name          VARCHAR(255) NOT NULL
  created_at    TIMESTAMP   NOT NULL
  UNIQUE (organization_id, name)

group_memberships
  id            CHAR(36)    PK
  group_id      CHAR(36)    FK → groups(id)   NOT NULL
  member_id     CHAR(36)    FK → members(id)  NOT NULL
  role          VARCHAR(20)  NOT NULL          -- ADMIN | GROUP_LEAD | MEMBER
  UNIQUE (group_id, member_id)

prompts
  id                   CHAR(36)    PK
  group_id             CHAR(36)    FK → groups(id)    NOT NULL
  created_by_member_id CHAR(36)    FK → members(id)   NOT NULL
  title                VARCHAR(500) NOT NULL
  content              TEXT         NOT NULL
  created_at           TIMESTAMP    NOT NULL
  updated_at           TIMESTAMP    NOT NULL
```

---

## Sample seed data

The repository includes a manual seed script at `scripts/seed-sample-data.sql`. It is not a Flyway migration and is not loaded by application startup.

Run it after the database is up and Flyway has created the schema:

```bash
just db-up
just db-wait
just db-migrate
just db-seed-sample
```

`just db-seed-sample` also applies Flyway migrations before loading the sample data, so it is safe to use as the single seeding step once the container is running.

For a clean local database reset with migrations and sample data in one command:

```bash
just db-rebuild
```

The seed is safe to re-run because it uses fixed IDs with `ON DUPLICATE KEY UPDATE`.

Seeded organization and group:

| Entity | Value |
|---|---|
| Organization | `Acme Labs` |
| Group | `platform` |

Seeded users:

| Username | Email | Role | Password |
|---|---|---|---|
| `admin` | `admin@example.com` | `ADMIN` | `password123` |
| `alice` | `alice@example.com` | `MEMBER` | `password123` |

The script creates 2 prompts in the shared group. This exercises the existing relationship from `prompts.created_by_member_id` to `members.id`, plus the role relationship through `group_memberships`.

---

## Backup and restore

There are currently no dedicated `just` recipes for backup and restore. Use MariaDB tooling directly if you need a local data export/import workflow, or add explicit recipes for that workflow before documenting them for the team.

---

## Authorization rules

| Operation | Required role |
|---|---|
| List / rename / delete organizations | Must hold `ADMIN` in at least one group |
| Create an organization | Public bootstrap endpoint |
| Create a group (first group ever for member) | Any authenticated member |
| Create additional groups | Must hold `ADMIN` in at least one existing group |
| Rename a group | `ADMIN` or `GROUP_LEAD` in that group |
| Delete a group | `ADMIN` in that group |
| Add / remove group members | `ADMIN` or `GROUP_LEAD` in that group |
| Change a member's role | `ADMIN` or `GROUP_LEAD` in that group |
| Create / read prompts | Any group member |
| Update / delete a prompt | Prompt owner, or `ADMIN` or `GROUP_LEAD` in the group |

---

## Development

### All available tasks

```
just              # list all recipes
```

| Recipe | Description |
|---|---|
| `just ci` | Full local lifecycle: stop the app, rebuild the DB, verify the API, package the JAR, start it in the background, wait for readiness, then run the documented HTTP workflow |
| `just db-start` | Start the MariaDB container and wait until it is ready |
| `just app-run` | Run the application (database must already be up) |
| `just app-up` | Start the packaged application in the background and write logs to `target/app.log` |
| `just app-stop` | Stop any app process listening on `localhost:8080` |
| `just app-wait` | Wait until the app accepts HTTP connections on `localhost:8080` |
| `just db-up` | Start the MariaDB container |
| `just db-wait` | Block until the database is accepting connections |
| `just db-down` | Stop the container (data persists) |
| `just db-destroy` | Stop the container and delete the data volume |
| `just db-logs` | Follow database container logs |
| `just db-shell` | Open an interactive `mariadb` shell |
| `just db-migrate` | Run Flyway migrations manually using `.env` database settings |
| `just db-seed-sample` | Load the manual local sample dataset |
| `just db-rebuild` | Destroy, recreate, migrate, and seed the local database |
| `just build` | Build all Maven modules through the root reactor |
| `just package` | Build the fat JAR (`target/api-<version>.jar`) |
| `just coverage` | Open the HTML coverage report in the browser |
| `just app-verify` | Run the full Maven verification gate, including the JaCoCo coverage check |
| `just test-class <Name>` | Run a single test class, e.g. `just test-class MemberServiceTest` |
| `just test-unit` | Run the full unit and MockMvc test suite |
| `just test-http` | Run all HTTP request tests under `src/test/resources/http` with the `dev` environment |

### First-time setup

```bash
# Install prerequisites (macOS)
brew install just jq

# Clone and configure
git clone <repo-url>
cd prompt-db
cp .env.example .env   # edit if needed

# Start the database only
just db-start
```

### Running only the application (database already running)

```bash
just app-run
```

To run the packaged JAR in the background and wait for it to accept HTTP connections:

```bash
just app-up
just app-wait
```

To stop a running local app on `localhost:8080`:

```bash
just app-stop
```

### Resetting the database

```bash
just db-rebuild   # destroys volume, recreates DB, applies migrations, loads sample data
```

Equivalent manual steps:

```bash
just db-destroy
just db-up
just db-wait
just db-migrate
just db-seed-sample
```

---

## Testing

### Unit and MockMvc tests

```bash
just test-unit
```

Runs the test suite across four layers:

| Layer | Test classes |
|---|---|
| Domain model | `GroupTest`, `GroupMembershipTest`, `MemberTest`, `OrganizationTest`, `PromptTest` |
| Domain policy | `DefaultGroupAccessPolicyTest`, `DefaultGroupMembershipManagementPolicyTest`, `DefaultPromptAuthorizationPolicyTest` |
| Application services | `GroupServiceTest`, `MemberServiceTest`, `OrganizationServiceTest`, `PromptServiceTest` |
| Controllers (MockMvc) | `AuthControllerTest`, `GroupControllerTest`, `MemberControllerTest`, `OrganizationControllerTest`, `PromptControllerTest` |
| Infrastructure / security | `JwtServiceTest`, `JwtAuthFilterTest`, `LoginServiceTest`, `MemberUserDetailsServiceTest` |
| Architecture compliance | `ArchitectureComplianceTest` (ArchUnit ring-dependency rules) |

### Coverage gate

```bash
just app-verify
```

Enforces the JaCoCo coverage gate configured in `api/pom.xml`. The HTML report is generated at `target/site/jacoco/index.html` after `just test-unit` and `just app-verify`.

```bash
just coverage   # open the report in the browser
```

### Running a single test class

```bash
just test-class PromptServiceTest
just test-class MemberControllerTest
```

### HTTP request tests

```bash
# Prerequisites: application running, npx available
just test-http
```

Runs every `.http` file in `src/test/resources/http` through `httpyac` with the `dev` environment from `src/test/resources/http/http-client.env.json`. The files are ordered and documented for manual progression through setup, auth, members, groups, prompts, organizations, and negative-path coverage.

### Full lifecycle validation

```bash
just ci
```

Runs the full local lifecycle gate: stops any app on `localhost:8080`, destroys and recreates the database volume, applies migrations and sample data, runs `just app-verify`, packages the JAR, starts it in the background, waits for HTTP readiness, runs `just test-http`, and stops the app on exit.
