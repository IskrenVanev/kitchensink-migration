# Persistence Migration: Jakarta EE to Spring Data JPA

## What was done
- Compared legacy persistence setup (`persistence.xml` + JBoss datasource) with the Spring Boot project.
- Migrated persistence configuration into `src/main/resources/application.properties`.
- Migrated legacy seed script into `src/main/resources/import.sql`.
- Verified the project compiles successfully after persistence migration.

## Key changes

### Legacy to Spring mapping
| Feature | Legacy (Jakarta EE) | Modern (Spring Boot) |
| :--- | :--- | :--- |
| Configuration | `META-INF/persistence.xml` | `application.properties` |
| Data source | JBoss-managed JTA datasource | Spring-managed datasource |
| DDL strategy | `hibernate.hbm2ddl.auto=create-drop` | `spring.jpa.hibernate.ddl-auto=create-drop` |
| SQL seed | `import.sql` | `import.sql` |

### Applied configuration
Added the following properties to `src/main/resources/application.properties`:
- `spring.datasource.url=jdbc:h2:mem:kitchensink;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
- `spring.datasource.driver-class-name=org.h2.Driver`
- `spring.datasource.username=sa`
- `spring.datasource.password=`
- `spring.jpa.hibernate.ddl-auto=create-drop`
- `spring.jpa.show-sql=false`
- `spring.jpa.properties.hibernate.format_sql=false`
- `spring.jpa.open-in-view=false`

### Startup issue identified and fixed
During runtime verification, the application initially failed with:
- `No qualifying bean of type 'com.iskren.repository.MemberRepository' available`

Root cause:
- The application class package (`com.iskren.kitchensink_modernized`) did not align with Spring Data JPA auto-configuration package discovery for repositories/entities.

Fix applied in `src/main/java/com/iskren/kitchensink_modernized/KitchensinkModernizedApplication.java`:
- Added `@EntityScan(basePackages = "com.iskren.model")`
- Added `@EnableJpaRepositories(basePackages = "com.iskren.repository")`

### Seed data migration
Created `src/main/resources/import.sql` with the legacy initial data row:
- `insert into Member (id, name, email, phone_number) values (0, 'John Smith', 'john.smith@mailinator.com', '2125551212')`

## Notes
- This keeps dev-time behavior aligned with legacy quickstart behavior: schema recreated per run.
- Existing `Member` mapping already matches seed SQL (`phoneNumber` mapped to `phone_number`).
- Persistence remains Spring Data JPA-based (`JpaRepository`) per project rules; no `EntityManager` was introduced.
- `spring.jpa.open-in-view=false` is explicitly set to follow REST-focused Spring best practices and prevent session usage during web view rendering.

## Other important information
- Legacy files referenced:
  - `C:\Users\user\Desktop\MongoDBLearning\KitchensinkModernized\kitchensink\src\main\resources\META-INF\persistence.xml`
  - `C:\Users\user\Desktop\MongoDBLearning\KitchensinkModernized\kitchensink\src\main\resources\import.sql`
- Modern files updated:
  - `src/main/java/com/iskren/kitchensink_modernized/KitchensinkModernizedApplication.java`
  - `src/main/resources/application.properties`
  - `src/main/resources/import.sql`
- Verification:
  - `mvnw.cmd -DskipTests compile` → `BUILD SUCCESS`
  - Runtime: app started on `8082` (because `8081` was occupied locally)
  - API check: `GET http://localhost:8082/rest/members` returned seeded member `John Smith`

## Suggested Commit Message
```text
feat: migrate persistence layer to Spring Data JPA configuration

- Replaced legacy persistence.xml setup with Spring Boot datasource/JPA properties
- Added H2 in-memory datasource configuration for local runtime parity
- Set hibernate ddl-auto to create-drop to match legacy behavior
- Migrated legacy import.sql seed data (John Smith)
- Fixed JPA bootstrap scanning via @EntityScan and @EnableJpaRepositories
- Disabled open-in-view for REST best practice
- Verified runtime via GET /rest/members returning seeded member
```
