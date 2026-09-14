# prompt-db-ui

A standalone, server-rendered CRUD web application for the `prompt_db` API. Built with
Spring Boot 3, Spring MVC, and Thymeleaf. The app is a backend-for-frontend: your browser
only ever talks to this app over a session cookie, and this app makes the authenticated
calls to the `prompt_db` REST API on your behalf.

It implements every operation in the provided OpenAPI specification: organization
bootstrap, member self-registration and login, member directory, group management, group
membership management, and group prompt management (create/read/update/delete).

## Prerequisites

- Java 21 or later (JDK, not just JRE)
- Maven 3.9 or later
- A reachable instance of the `prompt_db` REST API (the one described by the attached
  OpenAPI spec) that you can point this UI at

Check your versions:

```bash
java -version
mvn -version
```

## Project layout

```
src/main/java/app/prompts/ui/
├── api/          REST clients + DTOs for every prompt_db endpoint
├── config/       API base URL / timeout configuration, RestClient bean
├── security/     Session-backed "current user" state, login/JWT plumbing, auth interceptor
├── support/      Group permission helpers (ADMIN / GROUP_LEAD / owner checks)
├── service/      Application services orchestrating API calls
├── form/         Bean Validation-backed web forms
└── web/          MVC controllers (one per resource) + centralized error handling
src/main/resources/
├── templates/    Thymeleaf pages (login, register, dashboard, organizations, members, groups, prompts, errors)
├── static/css/   Self-contained stylesheet (no CDN dependency)
└── application.yml
```

## Configuring the API base URL

The UI never hardcodes the API location. Set it via an environment variable before
starting the app:

```bash
export API_BASE_URL=https://your-prompt-db-api.example.com
```

If you don't set it, it defaults to `http://localhost:8081`, which is convenient for
local development if you run the API on that port.

Other environment variables you can override:

| Variable | Default | Purpose |
|---|---|---|
| `API_BASE_URL` | `http://localhost:8081` | Base URL of the prompt_db REST API |
| `SERVER_PORT` | `8080` | Port this UI application listens on |

Connect/read timeouts (5s / 15s) are set in `src/main/resources/application.yml` under
`app.api.connect-timeout` / `app.api.read-timeout` if you need to adjust them for a
slower network.

## Build

```bash
mvn clean package
```

This produces `target/prompt-db-ui.jar`. Tests run as part of `package`; use
`-DskipTests` to skip them if you're iterating quickly.

## Run

Either of these works:

```bash
# Option 1: Spring Boot Maven plugin (good for development)
API_BASE_URL=https://your-prompt-db-api.example.com mvn spring-boot:run

# Option 2: run the packaged jar (good for deployment)
API_BASE_URL=https://your-prompt-db-api.example.com java -jar target/prompt-db-ui.jar
```

Then open [http://localhost:8080](http://localhost:8080) in a browser.

## First-time walkthrough

The API has no seed data of its own, so the UI provides an onboarding path that mirrors
the two unauthenticated write endpoints in the spec:

1. **Create an organization** — go to `/organizations/bootstrap` (linked from the login
   page) and submit an organization name. This calls `POST /api/organizations`, the only
   way to create the first organization in a fresh system.
2. **Register the first member** — go to `/register` and create a member under that
   organization. This calls `POST /api/members`.
3. **Log in** — go to `/login` with the credentials you just registered. This calls
   `POST /api/auth/login`, stores the returned JWT in your server-side session (never
   exposed to the browser as JavaScript-visible storage), and loads your profile via
   `GET /api/members/me`.
4. From the dashboard you can manage groups, group members, and group prompts. Only
   organization administrators can see the full member directory and organization
   settings; everyone can see the groups they belong to.

## Authorization model (documented assumption)

The OpenAPI specification supplied for this project declares no `securitySchemes` and
attaches no per-operation security requirements — only the request/response shapes are
formally specified. The role and JWT rules enforced by this UI are carried over from the
endpoint inventory reviewed earlier in this project and are **assumptions**, not something
verified against the live API's actual authorization code:

- `POST /api/auth/login`, `POST /api/organizations`, and `POST /api/members` are treated
  as reachable without an existing session.
- Every other endpoint requires a valid session (JWT stored server-side after login).
- Organization administrators (`ADMIN`) can manage all organizations, members, and groups.
- A `GROUP_LEAD`'s elevated rights are scoped to the specific group(s) where their
  per-group membership role (`GET /api/groups/{id}/members`) is `GROUP_LEAD` — not
  organization-wide.
- A member can edit or delete a prompt only if the API's `createdBy` field on that prompt
  matches their own member id, or they are an `ADMIN`/`GROUP_LEAD` for that group.
- Deleting an organization is only offered in the UI when it currently has zero members,
  matching the "empty org" requirement from the endpoint inventory; the API is still the
  actual enforcement point and can reject the call regardless of what the UI shows.

The UI hides buttons/links a user isn't expected to have access to, but this is a
convenience layer only — the `prompt_db` API is the authoritative point of enforcement,
and every mutating action here is dispatched as a real HTTP call the API can still reject
based on its own rules. If the real API's authorization rules differ from the assumptions
above, update `app.prompts.ui.support.GroupPermissions` and the `@PreAuthorize`-style
checks in the `web` controllers accordingly.

## Error handling

- Validation errors (blank/too-long fields, etc.) are shown inline on the form, and the
  page re-renders with the values you already typed.
- If the API is unreachable (connection refused, DNS failure, timeout), the current page
  shows a plain-language "Could not reach the API" message instead of a stack trace.
- 4xx responses from the API (403, 404, 409, etc.) are translated to matching UI error
  pages (`error/403.html`, `error/404.html`, `error/409.html`) rather than being passed
  through as raw JSON.
- Any other unexpected server-side exception is caught centrally and shown as a generic
  500 page; details are written to the application log, not to the browser.
- List and detail pages show a distinct "nothing here yet" empty state (e.g. "No prompts
  in this group yet") instead of an empty table with no explanation.

## Verifying this build yourself

This project was verified end-to-end before packaging:

```bash
mvn clean package            # compiles + packages successfully
java -jar target/prompt-db-ui.jar --server.port=8091
```

against both an unreachable backend (to confirm graceful error pages) and a minimal stub
implementation of the API (to confirm the full login → dashboard → groups → group detail →
group members → prompts → create prompt → logout path renders and redirects correctly).

## Notes on the OpenAPI spec

- `Instant`-typed fields (`createdAt`, `updatedAt` on prompts) are declared in the spec
  with an empty schema (`{}`), i.e. the generator that produced the spec could not infer
  their wire format. This UI assumes the standard Jackson/Spring Boot default: ISO-8601
  strings (e.g. `2026-09-12T05:00:00Z`), which Spring Boot deserializes into
  `java.time.Instant` automatically via `jackson-datatype-jsr310`. If your API serializes
  these fields differently (e.g. epoch millis), adjust `PromptResponse` accordingly.
- There is no `GET /api/organizations/{id}` endpoint in the spec, so the organization
  detail/edit pages fetch the full list (`GET /api/organizations`) and filter client-side.
