# Kitchensink Modernized (Spring Boot + React)

## 1) Project Overview

This repository contains a modernization of the legacy JBoss EAP Kitchensink quickstart application.

The goal of the migration is to preserve the original business behavior (member registration, validation, and listing) while moving to a modern, maintainable stack suitable for current development practices.

### Technology Stack

- **Backend:** Java 21, Spring Boot 3.5.13, Spring Web, Spring Data JPA, Jakarta Validation
- **Database:** H2 (in-memory)
- **Frontend:** Vite 8 + React 19
- **Testing:** JUnit 5, Mockito, MockMvc, Vitest, Testing Library

### Main Features

- Register members via `POST /rest/members`
- View all members via `GET /rest/members`
- Lookup member by ID via `GET /rest/members/{id}`
- Validation and conflict handling aligned with legacy behavior:
  - Field validation errors -> `400` with field/message map
  - Duplicate email -> `409` with `{ "email": "Email taken" }`

---

## 2) Migration Approach

Legacy source reference:
`https://github.com/jboss-developer/jboss-eap-quickstarts/tree/8.0.x/kitchensink`

The migration was executed in incremental layers to reduce risk and preserve parity:

1. **Controller/API migration**
   - JAX-RS endpoints were migrated to Spring MVC REST endpoints in `com.iskren.controller`.
2. **Service migration**
   - Legacy EJB logic was moved into a Spring `@Service` (`MemberService`) using constructor injection.
   - Legacy event behavior was preserved using Spring `ApplicationEventPublisher`.
3. **Persistence migration**
   - Legacy persistence configuration was replaced with Spring Boot configuration in `application.properties`.
   - Seed data behavior was preserved with `import.sql`.
4. **Frontend modernization**
   - JSF/CND-style frontend workflow was replaced with modular Vite + React architecture.
   - React bundle is built into Spring Boot static resources for production serving.
5. **Verification**
   - Unit/integration tests were added for backend and frontend to validate behavior and regression safety.

---

## 3) Key Changes / Modernization

- Replaced JAX-RS + CDI patterns with Spring Boot conventions:
  - `@Path/@GET/@POST` -> `@RequestMapping/@GetMapping/@PostMapping`
  - `@Inject` -> constructor injection
  - EJB `@Stateless` service -> Spring `@Service`
- Introduced clear package boundaries under `com.iskren`:
  - `controller`, `service`, `repository`, `model`
- Replaced persistence bootstrap with Spring Data JPA + H2 configuration.
- Preserved and formalized API error contract for validation/conflict scenarios.
- Upgraded frontend to componentized React structure with dedicated API service module.
- Added modern, automated tests across backend and frontend.

---

## 4) Architecture Overview

### Project Structure

```text
kitchensink-modernized/
├─ src/
│  ├─ main/
│  │  ├─ java/com/iskren/
│  │  │  ├─ controller/                 # REST API controllers
│  │  │  ├─ service/                    # Business logic
│  │  │  ├─ repository/                 # Spring Data JPA repositories
│  │  │  ├─ model/                      # Entities + event model
│  │  │  └─ kitchensink_modernized/     # Spring Boot application entrypoint
│  │  └─ resources/
│  │     ├─ application.properties      # Runtime config
│  │     ├─ import.sql                  # Seed data
│  │     └─ static/                     # Static resources served by Spring Boot
│  └─ test/java/com/iskren/             # Backend tests
├─ frontend/
│  └─ src/
│     ├─ App.jsx                        # Root component/state orchestration
│     ├─ components/                    # UI components
│     ├─ services/memberApi.js          # REST client layer
│     └─ __tests__/                     # Frontend tests
├─ documentation/                       # Migration and testing documentation
├─ pom.xml
├─ package.json
├─ vite.config.js
└─ vitest.config.js
```

### Backend/Frontend Communication

- Frontend calls relative REST endpoints (`/rest/members`) from `frontend/src/services/memberApi.js`.
- In development, Vite proxies `/rest`, `/css`, `/gfx` to Spring Boot (`http://localhost:8081`).
- In production mode, Spring Boot serves the built React bundle from `src/main/resources/static/js/app.js`.

---

## 5) Configuration & Environment

### Default Runtime Configuration

From `src/main/resources/application.properties`:

- `server.port=8081`
- `spring.datasource.url=jdbc:h2:mem:kitchensink;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
- `spring.datasource.username=sa`
- `spring.datasource.password=`
- `spring.jpa.hibernate.ddl-auto=create-drop`

### Database Setup

- Database is **H2 in-memory** for local development/testing.
- Initial seed row is loaded from `src/main/resources/import.sql`.
- Schema is recreated at startup (`create-drop`).

### Environment Variables (Optional Overrides)

No custom environment variables are required to run the app.

You can override standard Spring properties when needed, for example:

- `SERVER_PORT`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_JPA_HIBERNATE_DDL_AUTO`

### Ports

- Spring Boot backend: `8081`
- Vite dev server: `5173`

---

## 6) Build and Run Instructions

### Prerequisites

- Java 21
- Node.js (compatible with Vite 8; recommended modern LTS)
- npm
- Maven (or use included Maven wrapper)

### A) Production Mode (single Spring Boot server)

This mode serves backend + static frontend from Spring Boot.

1. Install frontend dependencies (first time only):
   ```bash
   npm install
   ```
2. Build frontend bundle:
   ```bash
   npm run build
   ```
3. Run Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   ```
   On Windows PowerShell:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
4. Open:
   - `http://localhost:8081`

### B) Development Mode (hot reload for frontend)

Run backend and Vite dev server together.

Terminal 1 (backend):
```bash
./mvnw spring-boot:run
```
On Windows PowerShell:
```powershell
.\mvnw.cmd spring-boot:run
```

Terminal 2 (frontend):
```bash
npm install
npm run dev
```

Open:
- `http://localhost:5173`

### Build Artifacts

- Frontend build output is written to:
  - `src/main/resources/static/js/app.js`

---

## 7) Testing Strategy

The project uses layered tests for both backend and frontend.

### Backend Testing

Frameworks and tools:

- JUnit 5
- Mockito
- Spring Boot Test + MockMvc

Coverage includes:

- Model validation constraints (`MemberValidationTest`)
- Service business rules and event behavior (`MemberServiceTest`)
- Repository queries and persistence (`MemberRepositoryTest`)
- REST API contract and error handling (`MemberResourceRESTControllerTest`)

Run backend tests:

```bash
mvn test
```

### Frontend Testing

Frameworks and tools:

- Vitest
- `@testing-library/react`
- `@testing-library/user-event`
- `@testing-library/jest-dom`
- jsdom

Coverage includes:

- API service layer (`memberApi.test.js`)
- UI components (`MemberForm`, `MemberTable`, `Sidebar`)
- App integration flow (`App.test.jsx`) including success/error paths

Run frontend tests:

```bash
npm test
```

Additional commands:

```bash
npm run test:watch
npm run test:coverage
```

Current verified status:

- **Backend:** 51 tests passing
- **Frontend:** 60 tests passing
- **Total:** 111 tests passing

---

## 8) Known Limitations / Trade-offs

- Current database is in-memory H2 with `create-drop` (not persistent across restarts).
- No end-to-end browser test suite yet (unit/integration coverage is strong, but E2E is not included).
- Frontend production bundle is generated and committed as a static artifact, which is convenient for demo parity but can increase diff size.

---

## 9) Future Improvements

- Add better database configuration by using MongoDB and production-like environments.
- Add CI pipeline (build + tests + coverage reports + quality gates).
- Add E2E tests (e.g., Playwright) for full browser-to-backend verification.
- Add observability enhancements (structured logs, metrics, health dashboards).
- Add containerization (`Dockerfile` + Compose) for reproducible local setup.

---

## 10) References

- Legacy JBoss Kitchensink source used for migration:
  - `C:\Users\user\Desktop\MongoDBLearning\KitchensinkModernized\kitchensink`
- Red Hat JBoss EAP Quickstarts repository:
  - https://github.com/jboss-developer/jboss-eap-quickstarts
- Spring Boot documentation:
  - https://docs.spring.io/spring-boot/docs/current/reference/html/
- Spring Data JPA documentation:
  - https://docs.spring.io/spring-data/jpa/reference/
- Vite documentation:
  - https://vite.dev/
- React documentation:
  - https://react.dev/
- Project migration documentation in this repository:
  - `documentation/01-controllers-migration.md`
  - `documentation/02-service-migration.md`
  - `documentation/03-persistence-migration.md`
  - `documentation/04-frontend-integration.md`
  - `documentation/05-testing.md`
