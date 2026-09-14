# SRS > Use Case Suite

## Release Information

**Project:** prompt-db  
**Internal Release Number:** 1.0.0  
**Related Documents:**
- [`http-client.env.json`](http-client.env.json) — environment variable seed values
- [`00-setup.http`](00-setup.http) — bootstrap: create org and register members
- [`01-auth.http`](01-auth.http) — authentication
- [`02-members.http`](02-members.http) — member profile and listing
- [`03-groups.http`](03-groups.http) — group CRUD
- [`04-group-members.http`](04-group-members.http) — group membership CRUD
- [`05-prompts.http`](05-prompts.http) — prompt CRUD
- [`06-error-cases.http`](06-error-cases.http) — negative paths and error contract
- [`07-organizations.http`](07-organizations.http) — organization CRUD

> **Process impact:** A use case suite is a table of contents for the individual use cases. Organizing by priority, feature area, actor, business object, and source file helps identify coverage gaps and trace every requirement back to executable tests. Each use case below is traced directly to the `*.http` file and request that exercises it.

---

## Actors and Roles

| Actor | Description |
|---|---|
| **Unauthenticated** | Any client with no `Authorization` header. May only access public bootstrap endpoints. |
| **Member** | Any authenticated user holding a valid JWT. Base access to read own profile, list groups, and manage prompts within joined groups. |
| **GROUP_LEAD** | Member with elevated role inside one group. May manage that group's membership and prompts. |
| **ADMIN** | Member with full administrative rights inside one group. Implicitly also has ADMIN rights at the organization level while that membership exists. |

---

## Use Case Diagram

```
                  ┌─────────────────────────────────────────────────────────┐
                  │                   prompt-db API                          │
                  │                                                          │
  ┌─────────────┐ │  ┌──────────────────────────────────────────────────┐   │
  │Unauthenticated│ │  │ Public                                           │   │
  └──────┬──────┘ │  │  UC-10 Register member   UC-51 Create org         │   │
         │        │  │  UC-01 Login                                      │   │
         │        │  └──────────────────────────────────────────────────┘   │
         │        │                                                          │
  ┌──────┴──────┐ │  ┌──────────────────────────────────────────────────┐   │
  │   Member    │─┼──│ Authenticated (any member)                        │   │
  └──────┬──────┘ │  │  UC-11 Get own profile    UC-20 List groups       │   │
         │        │  │  UC-12 List members        UC-21 Create group      │   │
         │        │  │  UC-22 Get group           UC-41 List prompts      │   │
         │        │  │  UC-40 Create prompt       UC-42 Get prompt        │   │
         │        │  └──────────────────────────────────────────────────┘   │
         │        │                                                          │
  ┌──────┴──────┐ │  ┌──────────────────────────────────────────────────┐   │
  │GROUP_LEAD / │─┼──│ Group-scoped elevated                             │   │
  │   ADMIN     │ │  │  UC-23 Rename group        UC-31 Add member       │   │
  └──────┬──────┘ │  │  UC-32 Change member role  UC-33 Remove member    │   │
         │        │  │  UC-43 Update prompt        UC-44 Delete prompt    │   │
         │        │  └──────────────────────────────────────────────────┘   │
         │        │                                                          │
  ┌──────┴──────┐ │  ┌──────────────────────────────────────────────────┐   │
  │    ADMIN    │─┼──│ ADMIN-only                                        │   │
  └─────────────┘ │  │  UC-24 Delete group        UC-50 List orgs        │   │
                  │  │  UC-52 Rename org           UC-53 Delete empty org │   │
                  │  └──────────────────────────────────────────────────┘   │
                  └─────────────────────────────────────────────────────────┘
```

---

## Use Cases by Feature Area

### Authentication

- **UC-01** Login as a member → [`01-auth.http [1/2]`](01-auth.http), [`[2/2]`](01-auth.http)

### Member Management

- **UC-10** Register as a new member → [`00-setup.http [2/3]`](00-setup.http), [`[3/3]`](00-setup.http)
- **UC-11** Get own member profile → [`02-members.http [1/3]`](02-members.http), [`[2/3]`](02-members.http)
- **UC-12** List all members in organization → [`02-members.http [3/3]`](02-members.http), [`04-group-members.http [4/7]`](04-group-members.http)

### Group Management

- **UC-20** List groups → [`03-groups.http [1/5]`](03-groups.http), [`[3/5]`](03-groups.http)
- **UC-21** Create a group → [`03-groups.http [2/5]`](03-groups.http)
- **UC-22** Get a group by ID → [`03-groups.http [4/5]`](03-groups.http)
- **UC-23** Rename a group → [`03-groups.http [5/5]`](03-groups.http)
- **UC-24** Delete a group → [`03-groups.http` teardown comment](03-groups.http)

### Group Membership Management

- **UC-30** List members of a group → [`04-group-members.http [1/7]`](04-group-members.http), [`[3/7]`](04-group-members.http)
- **UC-31** Add a member to a group → [`04-group-members.http [2/7]`](04-group-members.http)
- **UC-32** Change a member's role in a group → [`04-group-members.http [6/7]`](04-group-members.http)
- **UC-33** Remove a member from a group → [`04-group-members.http [7/7]`](04-group-members.http)

### Prompt Management

- **UC-40** Create a prompt → [`05-prompts.http [1/8]`](05-prompts.http), [`[6/8]`](05-prompts.http)
- **UC-41** List prompts in a group → [`05-prompts.http [2/8]`](05-prompts.http), [`[7/8]`](05-prompts.http)
- **UC-42** Get a prompt by ID → [`05-prompts.http [3/8]`](05-prompts.http), [`[5/8]`](05-prompts.http)
- **UC-43** Update a prompt → [`05-prompts.http [4/8]`](05-prompts.http)
- **UC-44** Delete a prompt → [`05-prompts.http [8/8]`](05-prompts.http)

### Organization Management

- **UC-50** List organizations → [`07-organizations.http [1/5]`](07-organizations.http)
- **UC-51** Create an organization → [`00-setup.http [1/3]`](00-setup.http), [`07-organizations.http [2/5]`](07-organizations.http)
- **UC-52** Rename an organization → [`07-organizations.http [3/5]`](07-organizations.http)
- **UC-53** Delete an empty organization → [`07-organizations.http [4/5]`](07-organizations.http)

### Error and Validation Handling

- **UC-60** Reject unauthenticated requests → [`06-error-cases.http [401-1]`](06-error-cases.http) through [`[401-4]`](06-error-cases.http)
- **UC-61** Reject requests with invalid or malformed input → [`06-error-cases.http [400-1]`](06-error-cases.http) through [`[400-12]`](06-error-cases.http)
- **UC-62** Return not-found errors for missing resources → [`06-error-cases.http [404-1]`](06-error-cases.http) through [`[404-5]`](06-error-cases.http)
- **UC-63** Return conflict errors for constraint violations → [`06-error-cases.http [409-1]`](06-error-cases.http), [`07-organizations.http [5/5]`](07-organizations.http)

---

## Use Cases by Stakeholder

> The set of stakeholders comes from the roles defined in the system. Each use case is linked to the `.http` file that covers it.

### Unauthenticated (public endpoints — no `Authorization` header required)

- **UC-10** Register as a new member — `POST /api/members`
- **UC-51** Create an organization — `POST /api/organizations`
- **UC-01** Login as a member — `POST /api/auth/login`

### Any Authenticated Member

- **UC-11** Get own member profile — `GET /api/members/me`
- **UC-12** List all members in organization — `GET /api/members`
- **UC-20** List groups — `GET /api/groups`
- **UC-21** Create a group (creator is automatically granted ADMIN) — `POST /api/groups`
- **UC-22** Get a group by ID — `GET /api/groups/{groupId}`
- **UC-40** Create a prompt — `POST /api/groups/{groupId}/prompts`
- **UC-41** List prompts in a group — `GET /api/groups/{groupId}/prompts`
- **UC-42** Get a prompt by ID — `GET /api/groups/{groupId}/prompts/{promptId}`

### Group ADMIN or GROUP_LEAD

- **UC-23** Rename a group — `PUT /api/groups/{groupId}`
- **UC-31** Add a member to a group — `POST /api/groups/{groupId}/members`
- **UC-32** Change a member's role in a group — `PUT /api/groups/{groupId}/members/{memberId}`
- **UC-33** Remove a member from a group — `DELETE /api/groups/{groupId}/members/{memberId}`
- **UC-43** Update a prompt — `PUT /api/groups/{groupId}/prompts/{promptId}`
- **UC-44** Delete a prompt — `DELETE /api/groups/{groupId}/prompts/{promptId}`

### Group ADMIN (exclusive)

- **UC-24** Delete a group — `DELETE /api/groups/{groupId}`

### Organization ADMIN (member holding ADMIN role in at least one group)

- **UC-50** List organizations — `GET /api/organizations`
- **UC-52** Rename an organization — `PUT /api/organizations/{organizationId}`
- **UC-53** Delete an empty organization — `DELETE /api/organizations/{organizationId}`

---

## Use Cases by Priority

### Essential

- **UC-01** Login as a member
- **UC-10** Register as a new member
- **UC-11** Get own member profile
- **UC-20** List groups
- **UC-21** Create a group
- **UC-22** Get a group by ID
- **UC-30** List members of a group
- **UC-31** Add a member to a group
- **UC-40** Create a prompt
- **UC-41** List prompts in a group
- **UC-42** Get a prompt by ID
- **UC-51** Create an organization

### Expected

- **UC-12** List all members in organization
- **UC-23** Rename a group
- **UC-24** Delete a group
- **UC-32** Change a member's role in a group
- **UC-33** Remove a member from a group
- **UC-43** Update a prompt
- **UC-44** Delete a prompt
- **UC-50** List organizations
- **UC-52** Rename an organization
- **UC-53** Delete an empty organization
- **UC-60** Reject unauthenticated requests
- **UC-61** Reject requests with invalid or malformed input
- **UC-62** Return not-found errors for missing resources
- **UC-63** Return conflict errors for constraint violations

### Desired

N/A: There are no use cases with Priority = Desired

### Optional

N/A: All identified use cases are Essential or Expected

---

## Use Cases by Business Object and Actor

| BO \ Actor | Unauthenticated | Any Member | GROUP\_LEAD / ADMIN | Organization ADMIN |
|---|---|---|---|---|
| **Organization** | Create organization | — | — | List orgs · Rename org · Delete empty org |
| **Member** | Register · Login | Get own profile · List members | — | — |
| **Group** | — | List groups · Create group · Get group by ID | Rename group · Delete group | — |
| **Group Membership** | — | — | List members · Add member · Change role · Remove member | — |
| **Prompt** | — | Create prompt · List prompts · Get prompt by ID | Update prompt · Delete prompt | — |

---

## Step-by-Step Scenarios

### Scenario A — New tenant bootstrap (UC-51 → UC-10 → UC-01)

This scenario runs against a clean database and seeds the environment for all downstream tests.
Traced to: [`00-setup.http`](00-setup.http), [`01-auth.http`](01-auth.http).

```
Actor: Unauthenticated client (setup script)

1. POST /api/organizations  { name: "Smoke Test Org" }
   → 201 Created  { organizationId, name }
   → Save organizationId for subsequent requests.

2. POST /api/members  { username: "brian", password, email, organizationId }
   → 201 Created  { memberId, organizationId, username, email, role }
   → Save primaryMemberId.

3. POST /api/members  { username: "bob", password, email, organizationId }
   → 201 Created  { memberId, ... }
   → Save secondMemberId.

4. POST /api/auth/login  { username: "brian", password }
   → 200 OK  { token }
   → Save authToken for protected requests.

5. POST /api/auth/login  { username: "bob", password }
   → 200 OK  { token }
   → Save secondAuthToken.
```

### Scenario B — Group lifecycle (UC-21 → UC-22 → UC-23 → UC-24)

Traced to: [`03-groups.http`](03-groups.http).

```
Actor: Authenticated member (brian / authToken)

1. GET  /api/groups
   → 200 OK  []   (empty baseline)

2. POST /api/groups  { name: "Smoke Test Group" }
   → 201 Created  { groupId, organizationId, name }
   → brian is automatically granted ADMIN membership.
   → Save groupId.

3. GET  /api/groups/{groupId}
   → 200 OK  { groupId, organizationId, name: "Smoke Test Group" }

4. PUT  /api/groups/{groupId}  { name: "Smoke Test Group (renamed)" }
   → 200 OK  { groupId, organizationId, name: "Smoke Test Group (renamed)" }

5. GET  /api/groups
   → 200 OK  [ { groupId, name: "Smoke Test Group (renamed)" } ]

── Optional teardown (run last, breaks 04 and 05 if run early) ──
6. DELETE /api/groups/{groupId}
   → 204 No Content
```

### Scenario C — Members page role-assignment flow (UC-31 → UC-30 → UC-32 → UC-12 → UC-33)

This mirrors the front-end Members page: register a member, then select their group and role.
Traced to: [`04-group-members.http`](04-group-members.http).

```
Actor: brian (ADMIN), bob (MEMBER being assigned)

Prerequisites: groupId set from Scenario B; secondMemberId set from Scenario A.

1. GET  /api/groups/{groupId}/members
   → 200 OK  [ { memberId: primaryMemberId, role: "ADMIN" } ]
   (only the creator at this point)

2. POST /api/groups/{groupId}/members  { memberId: secondMemberId, role: "MEMBER" }
   → 201 Created  { groupId, memberId, role: "MEMBER" }

3. GET  /api/groups/{groupId}/members
   → 200 OK  [ { memberId: primaryMemberId, role: "ADMIN" },
               { memberId: secondMemberId,  role: "MEMBER" } ]

4. GET  /api/members                           (brian's token)
   → 200 OK — bob's entry shows role: "MEMBER" (effective role from group)

5. GET  /api/groups                            (bob's token / secondAuthToken)
   → 200 OK — response array contains groupId with name "Smoke Test Group (renamed)"

6. PUT  /api/groups/{groupId}/members/{secondMemberId}  { role: "GROUP_LEAD" }
   → 200 OK  { groupId, memberId, role: "GROUP_LEAD" }

7. DELETE /api/groups/{groupId}/members/{secondMemberId}
   → 204 No Content
```

### Scenario D — Prompt CRUD lifecycle (UC-40 → UC-41 → UC-42 → UC-43 → UC-44)

Traced to: [`05-prompts.http`](05-prompts.http).

```
Actor: brian (ADMIN of groupId)

Prerequisites: groupId set from Scenario B.

1. POST /api/groups/{groupId}/prompts
   { title: "Hello World Prompt", content: "..." }
   → 201 Created  { id, groupId, createdBy, title, content, createdAt, updatedAt }
   → Save promptId.

2. GET  /api/groups/{groupId}/prompts
   → 200 OK  [ { id: promptId, title: "Hello World Prompt", ... } ]

3. GET  /api/groups/{groupId}/prompts/{promptId}
   → 200 OK  { id: promptId, title: "Hello World Prompt", ... }

4. PUT  /api/groups/{groupId}/prompts/{promptId}
   { title: "Hello World Prompt (revised)", content: "..." }
   → 200 OK  { id, title: "Hello World Prompt (revised)", ... }

5. GET  /api/groups/{groupId}/prompts/{promptId}    (verify update persisted)
   → 200 OK  { title: "Hello World Prompt (revised)", ... }

6. POST /api/groups/{groupId}/prompts
   { title: "Code Review Prompt", content: "..." }
   → 201 Created

7. GET  /api/groups/{groupId}/prompts
   → 200 OK  [ "Hello World Prompt (revised)", "Code Review Prompt" ]

8. DELETE /api/groups/{groupId}/prompts/{promptId}
   → 204 No Content
```

### Scenario E — Organization lifecycle (UC-51 → UC-52 → UC-53)

Traced to: [`07-organizations.http`](07-organizations.http).

```
Actor: brian (Organization ADMIN via group membership)

1. GET  /api/organizations
   → 200 OK — array includes { organizationId, name: "Smoke Test Org" }

2. POST /api/organizations  { name: "Empty Organization For CRUD" }
   → 201 Created  { organizationId, name }
   → Save emptyOrganizationId.

3. PUT  /api/organizations/{emptyOrganizationId}
   { name: "Empty Organization For CRUD (renamed)" }
   → 200 OK  { organizationId, name: "Empty Organization For CRUD (renamed)" }

4. DELETE /api/organizations/{emptyOrganizationId}
   → 204 No Content

5. DELETE /api/organizations/{organizationId}    (the org still has members + groups)
   → 409 Conflict  { status: 409, title: "Conflict", detail: "..." }
```

### Scenario F — Error contract (UC-60 through UC-63)

All error responses follow [RFC 9457 Problem Details](https://www.rfc-editor.org/rfc/rfc9457).
Traced to: [`06-error-cases.http`](06-error-cases.http).

#### 401 Unauthenticated

```
Missing or invalid Authorization header on any protected endpoint.

GET /api/groups                        (no header)        → 401
GET /api/groups                        Bearer bad-token   → 401
GET /api/members/me                    (no header)        → 401
GET /api/groups/{groupId}/prompts      (no header)        → 401
```

#### 400 Validation failures — bean constraints

All return `Content-Type: application/problem+json` with `title: "Validation Failed"` and a
field-level `errors` map.

| Test case | Endpoint | Violated field |
|---|---|---|
| 400-1 | `POST /api/auth/login` | `password` (min 8 chars) |
| 400-2 | `POST /api/auth/login` | `username` (required) |
| 400-3 | `POST /api/members` | `email` (blank) |
| 400-4 | `POST /api/members` | `email` (not a valid address) |
| 400-5 | `POST /api/members` | `organizationId` (required) |
| 400-6 | `POST /api/organizations` | `name` (blank) |
| 400-7 | `POST /api/groups` | `name` (blank) |
| 400-8 | `POST /api/groups/{id}/prompts` | `title` (blank) |
| 400-9 | `POST /api/groups/{id}/prompts` | `content` (required) |
| 400-10 | `POST /api/groups/{id}/members` | `role` (required) |

#### 400 Type / format mismatch

Return `title: "Invalid Parameter"`.

| Test case | Endpoint | Bad value |
|---|---|---|
| 400-11 | `GET /api/groups/{groupId}` | `groupId` is not a UUID |
| 400-12 | `GET /api/groups/{groupId}/prompts/{promptId}` | `promptId` is not a UUID |

#### 404 Not Found

Return `Content-Type: application/problem+json`.

| Test case | Endpoint | Expected title |
|---|---|---|
| 404-1 | `GET /api/groups/00000000-…` | `Group Not Found` |
| 404-2 | `GET /api/groups/00000000-…/members` | `Group Not Found` |
| 404-3 | `GET /api/groups/{id}/prompts/00000000-…` | `Prompt Not Found` |
| 404-4 | `PUT /api/groups/{id}/prompts/00000000-…` | `Prompt Not Found` |
| 404-5 | `DELETE /api/groups/{id}/prompts/00000000-…` | `Prompt Not Found` |

#### 409 Conflict

Return `Content-Type: application/problem+json` with `title: "Conflict"`.

| Test case | Endpoint | Condition |
|---|---|---|
| 409-1 (`06`) | `DELETE /api/organizations/{organizationId}` | Org still has members or groups |
| [5/5] (`07`) | `DELETE /api/organizations/{organizationId}` | Same constraint, from full org suite |

---

## Use Cases by Business Object and Actor (extended)

| BO \ Actor | Unauthenticated | Any Member | GROUP\_LEAD / ADMIN | Organization ADMIN |
|---|---|---|---|---|
| **Organization** | UC-51 Create org | — | — | UC-50 List orgs · UC-52 Rename · UC-53 Delete empty |
| **Member** | UC-10 Register · UC-01 Login | UC-11 Get own profile · UC-12 List members | — | — |
| **Group** | — | UC-20 List · UC-21 Create · UC-22 Get by ID | UC-23 Rename · UC-24 Delete | — |
| **Group Membership** | — | UC-30 List (own groups only after added) | UC-30 List · UC-31 Add · UC-32 Change role · UC-33 Remove | — |
| **Prompt** | — | UC-40 Create · UC-41 List · UC-42 Get | UC-43 Update · UC-44 Delete | — |
| **Auth** | UC-01 Login | — | — | — |
| **Error contract** | UC-60 Unauth→401 | UC-61 Validation→400 · UC-62 Missing→404 | UC-63 Conflict→409 | UC-63 Conflict→409 |

---

## Running the Suite

```bash
# Full lifecycle: rebuild DB, start API, run all *.http files in order
just test-http

# Run a single file manually
npx httpyac send api/src/test/resources/http/05-prompts.http --all \
  --var baseUrl=http://localhost:8080 \
  --var authToken=<token>
```

Environment variables are resolved in order: `client.global` (set by prior requests in the run)
then `http-client.env.json` (static seed values). The `$global.X || X` pattern in each request
allows the file to be run standalone with env-file values when global state has not been seeded
by an earlier file in the same run.
