# Testing Guide

## Overview

The test suite covers both the **Java 21 Spring Boot backend** and the **Vite + React 19 frontend**. Tests verify that the modernized application's behaviour matches the original legacy Kitchensink, ensuring no regressions were introduced during migration.

| Layer | Framework | Tests | Result |
|-------|-----------|-------|--------|
| Backend | JUnit 5 + Mockito + MockMvc | 51 | ✅ All pass |
| Frontend | Vitest 4.1.2 + @testing-library/react 16.3.2 | 60 | ✅ All pass |
| **Total** | | **111** | ✅ |

---

## Running Tests

### Backend

```bash
mvn test
```

### Frontend

```bash
# Install dependencies (first time only)
npm install

# Run all tests once
npm test

# Watch mode (re-runs on file change)
npm run test:watch

# Generate coverage report
npm run test:coverage
```

---

## Backend Tests

All backend test classes live under `src/test/java/com/iskren/`.

### Package Structure Note

The main application class (`KitchensinkModernizedApplication`) resides in `com.iskren.kitchensink_modernized`, which is a sibling package — not an ancestor — of `com.iskren.controller`, `com.iskren.service`, etc. Spring Boot's slice test bootstrapper searches upward for `@SpringBootConfiguration`, so it cannot auto-detect the main class from sibling packages.

**Resolution**: Tests that require a Spring context reference `KitchensinkModernizedApplication.class` explicitly via `@SpringBootTest(classes = ...)`. The placeholder `TestApplication.java` in `com.iskren` documents this decision.

---

### 1. `MemberValidationTest` — Model Constraint Tests

**Location**: `src/test/java/com/iskren/model/MemberValidationTest.java`  
**Type**: Pure unit test (no Spring context)  
**Framework**: Jakarta Validation API (`Validation.buildDefaultValidatorFactory()`)

Validates all Bean Validation constraints defined on `Member`:

| Constraint | Field | Tested |
|------------|-------|--------|
| `@NotNull` + `@Size(min=1, max=25)` | `name` | Null, empty, 26-char name |
| `@Pattern(regexp="[^0-9]*")` | `name` | Digits present, digits only |
| `@NotBlank` + `@Email` | `email` | Null, blank, missing `@`, missing domain |
| `@NotNull` + `@Size(min=10, max=12)` + `@Digits` | `phoneNumber` | Null, 9-char, 13-char, non-digits |
| Valid member | — | No violations |

**Tests**: 14

---

### 2. `MemberServiceTest` — Business Logic Unit Tests

**Location**: `src/test/java/com/iskren/service/MemberServiceTest.java`  
**Type**: Unit test with Mockito (`@ExtendWith(MockitoExtension.class)`)  
**Dependencies mocked**: `MemberRepository`, `Validator`, `ApplicationEventPublisher`

Covers:

- **`listAllMembers()`** — delegates to repository, returns result; empty list
- **`lookupMemberById()`** — found (non-empty Optional); not found (empty Optional)
- **`createMember()` happy path** — saves member, publishes `MemberRegisteredEvent` with correct member reference
- **`createMember()` with violations** — throws `ConstraintViolationException`; does NOT call `save()` or `publishEvent()`
- **`createMember()` duplicate email** — throws `ValidationException`; does NOT call `save()` or `publishEvent()`
- **Ordering** — `validate()` is called before `findByEmail()`

**Tests**: 11

---

### 3. `MemberRepositoryTest` — Repository Integration Tests

**Location**: `src/test/java/com/iskren/repository/MemberRepositoryTest.java`  
**Type**: Integration test (`@SpringBootTest` + Flapdoodle embedded MongoDB 8.2.0)  
**Context**: Full application context, `webEnvironment = NONE`

Each test uses `@BeforeEach` with `deleteAll()` to wipe the collection before running, keeping tests isolated (no transactional rollback — MongoDB on a single node does not support it without a replica set).

Covers:

- **`findByEmail()`** — present (returns member); absent (empty Optional); case-sensitive match
- **`findAllByOrderByNameAsc()`** — alphabetical order for 3 members; empty list; single member
- **`save()` + `findById()`** — persisted member gets an auto-generated ID; not-found returns empty Optional

**Tests**: 8

---

### 4. `MemberResourceRESTControllerTest` — Controller / HTTP Integration Tests

**Location**: `src/test/java/com/iskren/controller/MemberResourceRESTControllerTest.java`  
**Type**: Integration test (`@SpringBootTest` + `@AutoConfigureMockMvc`)  
**Context**: Full application context, `webEnvironment = MOCK`; `MemberService` replaced with `@MockitoBean`

Tests the full HTTP request/response contract of the REST API:

| Endpoint | Scenario | Expected |
|----------|----------|----------|
| `GET /rest/members` | Members exist | `200` + JSON array with all fields |
| `GET /rest/members` | Empty | `200` + `[]` |
| `GET /rest/members/{id}` | Found | `200` + member JSON |
| `GET /rest/members/{id}` | Not found | `404` |
| `POST /rest/members` | Valid payload | `200` + `createMember()` called |
| `POST /rest/members` | Duplicate email (service throws `ValidationException`) | `409` + `{"email":"Email taken"}` |
| `POST /rest/members` | Empty name (Bean Validation) | `400` + `{"name":"..."}` |
| `POST /rest/members` | Bad email format | `400` + `{"email":"..."}` |
| `POST /rest/members` | Phone too short | `400` + `{"phoneNumber":"..."}` |
| `POST /rest/members` | Name with digits | `400` + `{"name":"Must not contain numbers"}` |
| `POST /rest/members` | Service throws `ConstraintViolationException` | `400` |
| `POST /rest/members` | Service throws generic `RuntimeException` | `400` + `{"error":"..."}` |

**Tests**: 12

---

## Frontend Tests

All frontend test files live under `frontend/src/__tests__/`.

### Configuration

| File | Purpose |
|------|-------|
| `vitest.config.js` | Extends Vite config; sets `jsdom` environment, global APIs, setup file, include pattern |
| `frontend/src/__tests__/setup.js` | Imports `@testing-library/jest-dom` 6.9.1 to extend Vitest matchers |

**Frontend devDependency versions (latest stable at time of writing)**:

| Package | Version |
|---|---|
| `vitest` | 4.1.2 |
| `jsdom` | 29.0.1 |
| `@testing-library/react` | 16.3.2 |
| `@testing-library/user-event` | 14.6.1 |
| `@testing-library/jest-dom` | 6.9.1 |

**Path note**: Vitest resolves `include` and `setupFiles` relative to the Vite `root` (`frontend/`), so paths start with `./src/...` rather than `./frontend/src/...`.

---

### 5. `memberApi.test.js` — API Service Unit Tests

**Location**: `frontend/src/__tests__/memberApi.test.js`  
**Type**: Unit test; `fetch` is replaced with `vi.fn()` per test

**`getMembers()`**:
- Returns member array on success
- Returns `[]` when response data is not an array (null, object)
- Throws `"Unable to load members"` when response is not ok
- Calls the correct endpoint (`/rest/members`)

**`createMember()`**:
- Returns `{ ok: true, status: 200 }` on success
- Posts to `/rest/members` with `Content-Type: application/json`
- Serialises payload to JSON body
- Returns `{ ok: false, status: 400, errorPayload: {...} }` on validation failure
- Returns `{ ok: false, status: 409 }` on conflict
- Returns `{ errorPayload: {} }` when JSON parsing fails

**Tests**: 11

---

### 6. `MemberForm.test.jsx` — Registration Form Component Tests

**Location**: `frontend/src/__tests__/MemberForm.test.jsx`

Renders `MemberForm` with controlled props via a `renderForm()` helper.

- All three fields render and reflect `form` prop values
- Register button is present
- `onInputChange` fires for each field
- `onSubmit` fires on button click
- Field error spans appear for `name`, `email`, `phoneNumber` individually and simultaneously
- Global success message and global error message render via `StatusMessages`
- No error spans or messages rendered when props are empty

**Tests**: 16

---

### 7. `MemberTable.test.jsx` — Member List Table Tests

**Location**: `frontend/src/__tests__/MemberTable.test.jsx`

- All five column headers render (`Id`, `Name`, `Email`, `Phone #`, `REST URL`)
- Each member's id, name, email, and phone number render in the correct cells
- Per-member REST URL links have correct `href` (`/rest/members/{id}`)
- All-members URL link renders with correct `href` (`/rest/members`)
- Empty member list renders headers only, no data rows
- Row count equals member count + 1 (header row)

**Tests**: 9

---

### 8. `Sidebar.test.jsx` — Sidebar Component Tests

**Location**: `frontend/src/__tests__/Sidebar.test.jsx`

- Spring Boot logo image renders (`alt="Spring Boot"`)
- All four external links render with correct `href` values:
  - Spring Boot Documentation → `https://spring.io/projects/spring-boot`
  - Spring Guides → `https://spring.io/guides`
  - React Versions → `https://react.dev/versions`
  - Vite Documentation → `https://vite.dev/`
- Introduction text renders
- Link text includes version annotations (`v19.2.4`, `v8.0.3`)

**Tests**: 8

---

### 9. `App.test.jsx` — Integration Tests (Full App Flow)

**Location**: `frontend/src/__tests__/App.test.jsx`  
**Strategy**: `memberApi` module is fully mocked with `vi.mock()`.

| Scenario | Verified |
|----------|----------|
| Mount | Main heading renders |
| Mount | `Members` section heading renders |
| Mount | `getMembers` called once |
| Mount | Loaded members appear in table |
| Mount (empty list) | "No registered members." message |
| Mount (API error) | Global error message shown |
| Render | `MemberForm` section present |
| Render | `Sidebar` logo present |
| Successful registration | Success message shown |
| Successful registration | Form fields cleared |
| Successful registration | `getMembers` called again (list reload) |
| 400 response | Field error `"Must not contain numbers"` shown |
| 400 response | No success message shown |
| 409 response | `"Email taken"` field error shown |
| Network error (throw) | Global error with thrown message |
| 500 / other error | Global error from `errorPayload.error` |

**Tests**: 16

---

## Coverage Summary

| Area | Covered |
|------|---------|
| `Member` model constraints | All 10 constraint annotations |
| `MemberService` business logic | All 3 public methods + both exception paths |
| `MemberRepository` custom queries | `findByEmail`, `findAllByOrderByNameAsc`, `save`, `findById` |
| REST API HTTP contract | All endpoints, all documented status codes (200, 400, 404, 409) |
| `memberApi.js` service layer | Both functions, all branches including parse failure |
| `MemberForm` component | All props, all interactions, all conditional renders |
| `MemberTable` component | Data rendering, link generation, empty state |
| `Sidebar` component | All links, version annotations |
| `App` component | Mount flow, full registration flow, all error paths |

---

## Known Limitations / Improvement Opportunities

- **End-to-end tests** (e.g., Playwright) are not included. Adding E2E tests that start the Spring Boot server and drive a real browser would give the highest confidence in the integration.
- **`act(...)` warnings** appear in some `App` tests for the initial `useEffect` load. These are cosmetic warnings only — tests pass. Wrapping initial render assertions in `waitFor` would silence them.
- **Backend coverage report** is not auto-generated. Add the JaCoCo Maven plugin for HTML/XML coverage reports during CI.
- **`MemberRegisteredEvent` consumer** — there is no listener for this event in the current codebase. If a listener is added, tests for its behaviour should be added to `MemberServiceTest`.
