# MongoDB Migration: H2 (JPA) to MongoDB

## What was done

Migrated the persistence layer from Spring Data JPA with an H2 in-memory relational database to Spring Data MongoDB (MongoDB 8.x). All REST endpoints, validation rules, and error responses remain unchanged.

---

## Why MongoDB was chosen

The optional stretch goal in the challenge brief specified MongoDB as the target database. Beyond fulfilling the brief, MongoDB is a natural fit for this workload:

- **Document model**: A `Member` is a self-contained document with no joins — it maps cleanly to a MongoDB document without the overhead of a relational schema.
- **Schema flexibility**: Member fields can evolve without DDL migrations or schema version locks.
- **MongoDB + Spring Boot**: First-class support via `spring-boot-starter-data-mongodb`; Spring Data's repository abstraction (`MongoRepository`) mirrors `JpaRepository`, minimising migration surface.
- **Stretch goal alignment**: The project brief and challenge context explicitly mention MongoDB integration as the target.

---

## Key changes

### 1. `pom.xml`

| Before | After |
|---|---|
| `spring-boot-starter-data-jpa` | `spring-boot-starter-data-mongodb` |
| `com.h2database:h2` (runtime) | `de.flapdoodle.embed:de.flapdoodle.embed.mongo.spring3x` (test) |

Flapdoodle provides an embedded MongoDB process for tests, replacing the role H2 played for in-memory JPA tests.

### 2. `Member.java`

| Before (JPA) | After (MongoDB) |
|---|---|
| `@Entity` | `@Document(collection = "members")` |
| `@Table(uniqueConstraints = ...)` | Removed; uniqueness enforced via `@Indexed(unique = true)` on `email` |
| `@Id` (jakarta.persistence) | `@Id` (org.springframework.data.annotation) |
| `@GeneratedValue(IDENTITY)` | Removed; MongoDB auto-generates ObjectId |
| `@Column(name = "phone_number")` | Removed; field stored under Java property name `phoneNumber` |
| `private Long id` | `private String id` |

MongoDB generates IDs as 24-character hex ObjectId strings. The ID type change from `Long` to `String` is the most visible downstream change.

### 3. `MemberRepository.java`

```java
// Before
extends JpaRepository<Member, Long>

// After
extends MongoRepository<Member, String>
```

Custom query methods (`findByEmail`, `findAllByOrderByNameAsc`) are unchanged — Spring Data derives the same queries for MongoDB.

### 4. `MemberService.java`

`lookupMemberById` signature changed from `long id` to `String id` to match the repository's ID type.

### 5. `MemberResourceRESTController.java`

The path variable pattern was changed from `/{id:[0-9][0-9]*}` (numeric-only) to `/{id}` because MongoDB ObjectIds are hex strings, not integers. The `@PathVariable` type changed from `long` to `String`. Observable API behaviour is identical: a non-existent ID returns `404`.

### 6. `KitchensinkModernizedApplication.java`

Removed `@EntityScan` and `@EnableJpaRepositories` — these were JPA bootstrap annotations. Spring Data MongoDB is auto-configured by `@SpringBootApplication(scanBasePackages = "com.iskren")` alone.

### 7. `application.properties`

Removed all `spring.datasource.*` and `spring.jpa.*` properties. Added:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/kitchensink
spring.data.mongodb.auto-index-creation=true
```

`auto-index-creation=true` causes Spring Data to create the unique index on `email` (declared via `@Indexed(unique = true)`) at application startup.

### 8. Seed data: `import.sql` → `DataSeeder.java`

The H2 `import.sql` (which was executed by Hibernate DDL) has no equivalent in MongoDB. A `CommandLineRunner` component (`com.iskren.config.DataSeeder`) replaces it:

```java
@Override
public void run(String... args) {
    if (memberRepository.count() == 0) {
        // insert John Smith
    }
}
```

The check `count() == 0` prevents duplicate seeding across restarts (MongoDB is persistent, unlike H2 `create-drop`).

---

## Key differences vs relational database

| Concern | H2 / JPA | MongoDB |
|---|---|---|
| Schema | Fixed DDL (`create-drop`) | Schemaless documents |
| ID type | `Long` (auto-increment) | `String` (ObjectId hex) |
| Uniqueness | `@UniqueConstraint` on `@Table` | `@Indexed(unique = true)` on field |
| Seed data | `import.sql` via Hibernate DDL | `CommandLineRunner` (`DataSeeder`) |
| Transactions | JPA `@Transactional` with JDBC rollback | MongoDB transactions require replica set |
| Test isolation | `@Transactional` rollback on test method | `deleteAll()` in `@BeforeEach` |
| Connection | JDBC datasource | MongoDB URI |
| Data persistence | Wiped on restart (`create-drop`) | Persists across restarts |

---

## Trade-offs and considerations

### Persistence across restarts

H2 with `create-drop` recreated the schema (and seed data) on every startup. MongoDB persists data across restarts. The `DataSeeder` guards against re-inserting the seed row (`count() == 0`), but any member data registered during a previous run is retained. This is the correct production behaviour; for a fresh-start dev environment, the `kitchensink` database can be dropped manually.

### Transaction support

Standard multi-document transactions in MongoDB require a replica set. For local development with a single standalone node, `@Transactional` on service methods has no effect. The application does not use multi-document transactions (each operation is a single document save/find), so this is not a functional limitation. For production deployments, a replica set is recommended to enable transactional guarantees if needed.

### ID format change

Member IDs are now 24-character hex strings (e.g. `"683abc1234def5678901234a"`) instead of small integers. The REST API path `/rest/members/{id}` still works correctly. Any client that was hard-coding numeric IDs (e.g. bookmarks to `/rest/members/1`) will need to use the IDs returned from `GET /rest/members` instead.

### Test embedded MongoDB (Flapdoodle)

The `de.flapdoodle.embed.mongo.spring3x` dependency downloads a real MongoDB binary the first time tests run. This requires an internet connection on the first run and adds ~10–30 seconds to the initial test execution while the binary is downloaded and cached. Subsequent runs use the cached binary.

The embedded version is pinned to **MongoDB 8.2.0** via `src/test/resources/application.properties`:

```properties
de.flapdoodle.mongodb.embedded.version=8.2.0
```

Verified on Windows x86_64 with `de.flapdoodle.embed.mongo.spring3x:4.24.0`: embedded MongoDB 8.2.0 starts correctly and the full backend test suite passes.

---

## Files changed

| File | Change |
|---|---|
| `pom.xml` | Replaced JPA/H2 with MongoDB + Flapdoodle test dep |
| `src/main/java/com/iskren/model/Member.java` | `@Document`, `String id`, `@Indexed(unique=true)` |
| `src/main/java/com/iskren/repository/MemberRepository.java` | `MongoRepository<Member, String>` |
| `src/main/java/com/iskren/service/MemberService.java` | `lookupMemberById(String id)` |
| `src/main/java/com/iskren/controller/MemberResourceRESTController.java` | `/{id}` path, `String id` parameter |
| `src/main/java/com/iskren/kitchensink_modernized/KitchensinkModernizedApplication.java` | Removed `@EntityScan` / `@EnableJpaRepositories`; added `@EnableMongoRepositories(basePackages = "com.iskren.repository")` |
| `src/main/resources/application.properties` | MongoDB host/port/database + auto-index-creation |
| `src/main/resources/import.sql` | Cleared (superseded by DataSeeder) |
| `src/main/java/com/iskren/config/DataSeeder.java` | **New** — seeds John Smith if collection empty |
| `src/test/java/com/iskren/repository/MemberRepositoryTest.java` | Removed `@Transactional`; `deleteAll()` in `@BeforeEach`; `String` ID |
| `src/test/java/com/iskren/service/MemberServiceTest.java` | `String` ID literals in `lookupMemberById` tests |
| `src/test/java/com/iskren/controller/MemberResourceRESTControllerTest.java` | `String` ID in helper and all assertions |
| `src/test/resources/application.properties` | **New** — pins Flapdoodle embedded MongoDB version to 8.2.0 |

## Files unchanged

| File | Reason |
|---|---|
| `src/test/java/com/iskren/model/MemberValidationTest.java` | Pure Jakarta Validation unit test; no persistence |
| `src/test/java/com/iskren/service/MemberServiceTest.java` (business logic tests) | Mockito-only; only ID-type stubs updated |
| `frontend/` (all) | No frontend changes; REST contract is identical |
| `vite.config.js`, `vitest.config.js`, `package.json` | No frontend changes |
