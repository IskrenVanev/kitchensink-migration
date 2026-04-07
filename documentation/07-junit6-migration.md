# JUnit 6 Migration

## What was done

- Migrated backend test dependencies to JUnit 6 (`6.0.3`) in `pom.xml`.
- Added a JUnit BOM import to centralize and lock all JUnit modules to the same version.
- Kept `spring-boot-starter-test` for Spring Boot testing support (`@SpringBootTest`, MockMvc, AssertJ integration).
- Added explicit JUnit/Mockito test dependencies to guarantee JUnit 6 + Mockito extension compatibility:
  - `org.junit.jupiter:junit-jupiter`
  - `org.junit.platform:junit-platform-launcher`
  - `org.mockito:mockito-junit-jupiter`

## Key changes

### `pom.xml`

- Added property:
  - `<junit.version>6.0.3</junit.version>`
- Added dependency management import:
  - `org.junit:junit-bom:${junit.version}`
- Kept:
  - `org.springframework.boot:spring-boot-starter-test` (test scope)
- Added explicit test dependencies:
  - `org.junit.jupiter:junit-jupiter`
  - `org.junit.platform:junit-platform-launcher`
  - `org.mockito:mockito-junit-jupiter`

### Test classes

Existing backend tests already use Jupiter APIs (`org.junit.jupiter.api.*`) and did not require source-level migration because those APIs are still valid with JUnit 6 for this project.

Impacted classes verified with JUnit 6:

- `src/test/java/com/iskren/model/MemberValidationTest.java`
- `src/test/java/com/iskren/service/MemberServiceTest.java`
- `src/test/java/com/iskren/repository/MemberRepositoryTest.java`
- `src/test/java/com/iskren/controller/MemberResourceRESTControllerTest.java`
- `src/test/java/com/iskren/kitchensink_modernized/KitchensinkModernizedApplicationTests.java`

## Notes

- Spring Boot version remains `3.5.13`.
- Mockito integration remains unchanged and functional (`@ExtendWith(MockitoExtension.class)` and Spring `@MockitoBean`).
- No business logic was modified.
- Test semantics were preserved; only dependency-level migration was required.

## Validation

Executed backend test suite after migration:

```bash
.\mvnw.cmd test
```

Result:

- Tests run: 50
- Failures: 0
- Errors: 0
- Build: SUCCESS

## Other important information

- If future tests use additional JUnit modules (for example, advanced params/engine customization), keep them versionless in dependencies so they continue to inherit from the JUnit BOM (`6.0.3`).
- When upgrading JUnit again, update only `<junit.version>` in `pom.xml` to keep versions aligned.
