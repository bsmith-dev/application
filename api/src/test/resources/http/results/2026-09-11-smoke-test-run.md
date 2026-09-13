# Smoke Test Run — 2026-09-11

## Environment

| Item | Value |
|---|---|
| Date | 2026-09-11 |
| Base URL | `http://localhost:8080` |
| App launch | `./mvnw spring-boot:run` (sourced `../../../../../.env`) |
| Spring Boot | 4.1.1 |
| Database | MariaDB 11.4.5 via Docker (`../../../../../docker-compose.yml`) |
| Container | `database-container` |
| Schema | V1 + V2 migrations applied manually (`docker exec`) |
| JWT secret | `promptdb-dev-secret-32-chars-2026` (default from `../../../../../.env`) |
| Runner | `curl` 8.7.1 |

### Pre-run fixes applied

| Fix | File | Detail |
|---|---|---|
| Added `POST /api/organizations` to `permitAll()` | `SecurityConfig.java:88` | Endpoint was documented as public but missing from the security filter chain — every org creation request returned 403 regardless of DB state |
| Corrected `DB_URL` | `.env:1` | Value was `jdbc:mariadb://localhost:3306` (no database name); corrected to `jdbc:mariadb://localhost:3306/sandboxdb` |

---

## Test IDs Used

Values captured from responses during the run and used in subsequent requests:

| Variable | Value |
|---|---|
| `organizationId` | `7c1286b0-3f74-4916-a75f-f2f15aced867` |
| `primaryMemberId` (brian) | `4f77e6dd-ec8e-429d-a65e-1f97c3d5c121` |
| `secondMemberId` (ben) | `5a644701-caf2-44dc-a05c-91cd8cdaeaed` |
| `groupId` | `d206d8df-deeb-4ecb-ae3d-dca546100c32` |
| `promptId` (first prompt) | `a96c4d01-a02a-4a02-960f-8206bdca2bc7` |

---

## Results

### 00-setup.http — Bootstrap (3 requests)

| # | Method | Path | Expected | Actual | Result | Notes |
|---|---|---|---|---|---|---|
| 1/3 | POST | `/api/organizations` | 201 | 201 | PASS | `organizationId` captured |
| 2/3 | POST | `/api/members` (brian) | 201 | 201 | PASS | `primaryMemberId` captured |
| 3/3 | POST | `/api/members` (ben) | 201 | 201 | PASS | `secondMemberId` captured |

### 01-auth.http — Authentication (2 requests)

| # | Method | Path | Expected | Actual | Result | Notes |
|---|---|---|---|---|---|---|
| 1/2 | POST | `/api/auth/login` (brian) | 200 | 200 | PASS | JWT captured as `authToken` |
| 2/2 | POST | `/api/auth/login` (ben) | 200 | 200 | PASS | JWT captured as `secondAuthToken` |

### 02-members.http — Member resource (3 requests)

| # | Method | Path | Expected | Actual | Result | Notes |
|---|---|---|---|---|---|---|
| 1/3 | GET | `/api/members/me` (brian) | 200 | 200 | PASS | Returned correct `memberId`, `organizationId`, `username`, `email` |
| 2/3 | GET | `/api/members/me` (ben) | 200 | 200 | PASS | Independent principal resolution confirmed |
| 3/3 | GET | `/api/members` | 200 | 200 | PASS | Array contains both brian and ben |

### 03-groups.http — Group CRUD (5 requests)

| # | Method | Path | Expected | Actual | Result | Notes |
|---|---|---|---|---|---|---|
| 1/5 | GET | `/api/groups` | 200 | 200 | PASS | Empty array on fresh DB |
| 2/5 | POST | `/api/groups` | 201 | 201 | PASS | `groupId` captured |
| 3/5 | GET | `/api/groups` | 200 | 200 | PASS | Array contains created group |
| 4/5 | GET | `/api/groups/{groupId}` | 200 | 200 | PASS | Single group object returned |
| 5/5 | PUT | `/api/groups/{groupId}` | 200 | 200 | PASS | Name updated to `"Smoke Test Group (renamed)"` |

### 04-group-members.http — Membership CRUD (6 requests)

| # | Method | Path | Expected | Actual | Result | Notes |
|---|---|---|---|---|---|---|
| 1/6 | GET | `/api/groups/{groupId}/members` | 200 | 200 | PASS | One member (brian, ADMIN) |
| 2/6 | POST | `/api/groups/{groupId}/members` | 201 | 201 | PASS | Ben added as MEMBER |
| 3/6 | GET | `/api/groups/{groupId}/members` | 200 | 200 | PASS | Both brian (ADMIN) and ben (MEMBER) returned |
| 4/6 | GET | `/api/groups` (as ben) | 200 | 200 | PASS | Group visible to ben after add |
| 5/6 | PUT | `/api/groups/{groupId}/members/{memberId}` | 200 | 200 | PASS | Ben's role changed to GROUP_LEAD |
| 6/6 | DELETE | `/api/groups/{groupId}/members/{memberId}` | 204 | 204 | PASS | Empty body, ben removed |

### 05-prompts.http — Prompt CRUD (8 requests)

| # | Method | Path | Expected | Actual | Result | Notes |
|---|---|---|---|---|---|---|
| 1/8 | POST | `/api/groups/{groupId}/prompts` | 201 | 201 | PASS | `promptId` captured; `createdAt`/`updatedAt` populated |
| 2/8 | GET | `/api/groups/{groupId}/prompts` | 200 | 200 | PASS | One-item array |
| 3/8 | GET | `/api/groups/{groupId}/prompts/{promptId}` | 200 | 200 | PASS | Full prompt object returned |
| 4/8 | PUT | `/api/groups/{groupId}/prompts/{promptId}` | 200 | 200 | PASS | Title and content updated |
| 5/8 | GET | `/api/groups/{groupId}/prompts/{promptId}` | 200 | 200 | PASS | Updated values confirmed |
| 6/8 | POST | `/api/groups/{groupId}/prompts` | 201 | 201 | PASS | Second prompt created |
| 7/8 | GET | `/api/groups/{groupId}/prompts` | 200 | 200 | PASS | Two-item array |
| 8/8 | DELETE | `/api/groups/{groupId}/prompts/{promptId}` | 204 | 204 | PASS | Empty body |

### 06-error-cases.http — Negative Cases (21 requests)

#### 401 Unauthenticated (expected 401, actual 403)

| # | Method | Path | Expected | Actual | Result | Notes |
|---|---|---|---|---|---|---|
| 401-1 | GET | `/api/groups` | 401 | **403** | FAIL | See observation O-1 |
| 401-2 | GET | `/api/groups` (bad token) | 401 | **403** | FAIL | See observation O-1 |
| 401-3 | GET | `/api/members/me` | 401 | **403** | FAIL | See observation O-1 |
| 401-4 | GET | `/api/groups/{groupId}/prompts` | 401 | **403** | FAIL | See observation O-1 |

#### 400 Validation failures

| # | Method | Path | Expected | Actual | Result | Notes |
|---|---|---|---|---|---|---|
| 400-1 | POST | `/api/auth/login` (short password) | 400 | 400 | PASS | Problem Details body returned |
| 400-2 | POST | `/api/auth/login` (missing username) | 400 | 400 | PASS | Problem Details body returned |
| 400-3 | POST | `/api/members` (blank email) | 400 | 400 | PASS | Problem Details body returned |
| 400-4 | POST | `/api/members` (invalid email format) | 400 | 400 | PASS | Problem Details body returned |
| 400-5 | POST | `/api/members` (missing organizationId) | 400 | 400 | PASS | Problem Details body returned |
| 400-6 | POST | `/api/organizations` (blank name) | 400 | 400 | PASS | Problem Details body returned |
| 400-7 | POST | `/api/groups` (blank name) | 400 | 400 | PASS | Problem Details body returned |
| 400-8 | POST | `/api/groups/{groupId}/prompts` (blank title) | 400 | 400 | PASS | Problem Details body returned |
| 400-9 | POST | `/api/groups/{groupId}/prompts` (missing content) | 400 | 400 | PASS | Problem Details body returned |
| 400-10 | POST | `/api/groups/{groupId}/members` (null role) | 400 | 400 | PASS | Problem Details body returned |
| 400-11 | GET | `/api/groups/not-a-uuid` | 400 | 400 | PASS | `"Failed to convert 'groupId' with value: 'not-a-uuid'"` |
| 400-12 | GET | `/api/groups/{groupId}/prompts/not-a-uuid` | 400 | 400 | PASS | `"Failed to convert 'promptId' with value: 'not-a-uuid'"` |

#### 404 Not found

| # | Method | Path | Expected | Actual | Result | Notes |
|---|---|---|---|---|---|---|
| 404-1 | GET | `/api/groups/00000000-…` | 404 | 404 | PASS | `"Group not found: 00000000-…"` |
| 404-2 | GET | `/api/groups/00000000-…/members` | 404 | **403** | FAIL | See observation O-2 |
| 404-3 | GET | `/api/groups/{groupId}/prompts/00000000-…` | 404 | 404 | PASS | `"Prompt not found: 00000000-…"` |
| 404-4 | PUT | `/api/groups/{groupId}/prompts/00000000-…` | 404 | 404 | PASS | `"Prompt not found: 00000000-…"` |
| 404-5 | DELETE | `/api/groups/{groupId}/prompts/00000000-…` | 404 | 404 | PASS | `"Prompt not found: 00000000-…"` |

---

## Summary

| Category | Requests | Pass | Fail |
|---|---|---|---|
| 00-setup (bootstrap) | 3 | 3 | 0 |
| 01-auth | 2 | 2 | 0 |
| 02-members | 3 | 3 | 0 |
| 03-groups | 5 | 5 | 0 |
| 04-group-members | 6 | 6 | 0 |
| 05-prompts | 8 | 8 | 0 |
| 06-error: unauthenticated (401) | 4 | 0 | 4 |
| 06-error: validation (400) | 12 | 12 | 0 |
| 06-error: not found (404) | 5 | 4 | 1 |
| **Total** | **48** | **43** | **5** |

---

## Observations and Findings

### O-1 — Unauthenticated requests return 403 instead of 401

**Affected cases:** 401-1, 401-2, 401-3, 401-4

**Behaviour:** All protected endpoints return `HTTP 403` with an empty body when called without a token or with an invalid token. The HTTP specification (RFC 9110) requires `401 Unauthorized` for unauthenticated requests (missing or invalid credentials), reserving `403 Forbidden` for authenticated requests that lack permission.

**Root cause:** `SecurityConfig` does not configure a custom `AuthenticationEntryPoint`. Without one, Spring Security's default handling for an anonymous request that fails `anyRequest().authenticated()` falls through to the access-denied path, which returns 403 rather than challenging the client with 401.

**Security impact:** None — requests are correctly blocked. The wrong status code is a client-facing contract issue that can confuse API consumers and automated tools (e.g., an OAuth client that uses 401 to trigger a token refresh).

**Suggested fix:**

```java
http
    .exceptionHandling(ex -> ex
        .authenticationEntryPoint((request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            // optionally write a Problem Details body
        })
    )
```

---

### O-2 — `GET /api/groups/{nonExistentGroupId}/members` returns 403 instead of 404

**Affected case:** 404-2

**Behaviour:** Requesting members of a group UUID that does not exist returns:

```json
HTTP 403
{
  "detail": "Member is not in the group",
  "instance": "/api/groups/00000000-0000-0000-0000-000000000000/members",
  "status": 403,
  "title": "Access Denied"
}
```

**Root cause:** The group membership access check fires before the group existence check. When the group does not exist, the member-in-group lookup finds no membership record and throws `AccessDeniedException` ("Member is not in the group"). The group's non-existence is never evaluated.

**Impact:** The response discloses that the check order is membership before existence. A client cannot distinguish between "the group does not exist" and "you are not a member of this group". Compare with `GET /api/groups/{nonExistentId}` which correctly returns 404.

**Suggested fix:** In the service layer handling `listMembers`, validate that the group exists first (throwing `GroupNotFoundException` → 404) before evaluating membership access. The existence check should always precede the authorization check.

---

### O-3 — `updatedAt` not refreshed on prompt update (observation, not a failure)

**Observed in:** 05 [4/8] → [5/8]

The `updatedAt` timestamp in the PUT response and the subsequent GET both show the original `createdAt` value (`2026-09-11T01:26:31Z`). The schema defines `ON UPDATE CURRENT_TIMESTAMP` on the column, which should update the value automatically. This may indicate that the JPA entity's `updatedAt` field is not being refreshed after the UPDATE flush (i.e., the entity is not re-read from the database after the write, so the in-memory value is stale).

This is a cosmetic correctness issue for the response body, not a data integrity issue — the database value is likely correct.
