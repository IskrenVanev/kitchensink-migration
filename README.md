# Kitchensink Modernized (Spring Boot + React)

## 1) Project Overview

This repository contains a modernization of the legacy JBoss EAP Kitchensink quickstart application.

The goal of the migration is to preserve the original business behavior (member registration, validation, and listing) while moving to a modern, maintainable stack suitable for current development practices.

### Technology Stack

- **Backend:** Java 21, Spring Boot 3.5.13, Spring Web, Spring Data MongoDB, Jakarta Validation
- **Database:** MongoDB 8.2 (Railway Cloud / Local)
- **Frontend:** Vite 8 + React 19
- **Testing:** JUnit 6, Mockito, MockMvc, Vitest, Testing Library, Flapdoodle Embedded MongoDB

### Deployment Status

- **Live URL:** [https://kitchensink-migration-production.up.railway.app/](https://kitchensink-migration-production.up.railway.app/)
- **Platform:** Railway.app
- **Infrastructure:** Spring Boot (Java 21) + Managed MongoDB Service

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
   - Business validation and duplicate-email rules were centralized in the service layer.
3. **Persistence migration**
   - Legacy JPA/H2 persistence replaced with Spring Data MongoDB (`application.properties`).
   - Seed data migrated from `import.sql` (Hibernate DDL) to `DataSeeder.java` (`CommandLineRunner`).
   - Stretch goal fulfilled: MongoDB is the persistence target (latest stable MongoDB 8.2).
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
- Replaced persistence bootstrap with Spring Data MongoDB configuration.
- Fulfilled optional stretch goal: MongoDB 8.2 is the persistence layer.
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
│  │  │  ├─ config/                     # DataSeeder (seed data on startup)
│  │  │  ├─ repository/                 # Spring Data MongoDB repositories
│  │  │  ├─ model/                      # MongoDB document models
│  │  │  └─ kitchensink_modernized/     # Spring Boot application entrypoint
│  │  └─ resources/
│  │     ├─ application.properties      # Runtime config (MongoDB host/port/database)
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

The application is configured to run both locally and on **Railway** automatically using environment variable fallbacks in `src/main/resources/application.properties`:

```properties
# Railway uses SERVER_PORT (or PORT), local falls back to 8081
server.port=${SERVER_PORT:${PORT:8081}}

# Railway uses MONGO_URL, local falls back to localhost MongoDB
spring.data.mongodb.uri=${MONGO_URL:mongodb://localhost:27017/kitchensink}
```

### Database Setup

- **Local:** MongoDB running on `localhost:27017`.
- **Cloud:** Railway Managed MongoDB (via `MONGO_URL` environment variable).
- The database `kitchensink` (local) or `railway` (cloud) is created automatically.
- Initial seed data (John Smith) is inserted by `DataSeeder.java` at startup if the collection is empty.
- Data **persists across restarts**.
- To reset the local database:
  ```bash
  mongosh
  use kitchensink
  db.dropDatabase()
  ```

### Railway Deployment (Production)

The project is configured for seamless deployment on Railway:
1. **Service:** Spring Boot app (Java 21)
2. **Database:** Attached MongoDB service
3. **Domain:** [kitchensink-migration-production.up.railway.app](https://kitchensink-migration-production.up.railway.app/)

### Ports

- Local Backend: `8081` 
- Local Frontend (Vite): `5173` 
- Railway (Production): Managed by `${PORT}` (usually `8080`)

---

## 6) Build and Run Instructions

### Prerequisites

- Java 21
- **MongoDB 8.2** running locally on port `27017` ([Download MongoDB Community](https://www.mongodb.com/try/download/community))
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

- JUnit 6
- Mockito
- Spring Boot Test + MockMvc

Coverage includes:

- Model validation constraints (`MemberValidationTest`)
- Service business rules and validation flow (`MemberServiceTest`)
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

- **Backend:** 50 tests passing
- **Frontend:** 60 tests passing
- **Total:** 110 tests passing

---

## 8) Known Limitations / Trade-offs

- MongoDB must be running locally before starting the application (`mongod` on port `27017`).
- MongoDB multi-document transactions require a replica set; the current single-node setup does not support them (not required for this application).
- Member IDs are MongoDB ObjectId hex strings rather than small integers; links bookmarked to `/rest/members/1` will not resolve on a fresh database.
- No end-to-end browser test suite yet (unit/integration coverage is strong, but E2E is not included).
- Frontend production bundle is generated and committed as a static artifact, which is convenient for demo parity but can increase diff size.
- First backend test run downloads a MongoDB 8.2.0 binary via Flapdoodle (embedded, `de.flapdoodle.embed.mongo.spring3x:4.24.0`); this requires an internet connection and adds ~10–30 seconds on first use. Subsequent runs use the cached binary.

---

## 9) Future Improvements

- Add a MongoDB replica set profile for transactional support and production-like environments.
- Add CI pipeline (build + tests + coverage reports + quality gates).
- Add E2E tests (e.g., Playwright) for full browser-to-backend verification.
- Add observability enhancements (structured logs, metrics, health dashboards).
- Add containerization (`Dockerfile` + Docker Compose with MongoDB service) for reproducible local setup.

---

## 10) References

- Legacy JBoss Kitchensink source used for migration:
  - `https://github.com/jboss-developer/jboss-eap-quickstarts/tree/8.0.x/kitchensink`
- Red Hat JBoss EAP Quickstarts repository:
  - https://github.com/jboss-developer/jboss-eap-quickstarts
- Spring Boot documentation:
  - https://docs.spring.io/spring-boot/docs/current/reference/html/
- Spring Data MongoDB documentation:
  - https://docs.spring.io/spring-data/mongodb/reference/
- MongoDB 8.2 release notes:
  - https://www.mongodb.com/docs/manual/release-notes/8.2/
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
  - `documentation/06-mongodb-migration.md`
  - `documentation/07-junit6-migration.md`
