# Controllers Migration: MemberResourceRESTService

## What was done
- Located legacy controller: `org.jboss.as.quickstarts.kitchensink.rest.MemberResourceRESTService`
- Migrated it to Spring Boot controller: `com.iskren.controller.MemberResourceRESTController`
- Preserved the REST base path and HTTP methods:
  - `GET /rest/members`
  - `GET /rest/members/{id}`
  - `POST /rest/members`
- Implemented missing member stack required by the controller:
  - `com.iskren.model.Member`
  - `com.iskren.repository.MemberRepository`
  - `com.iskren.service.MemberService`

## Key changes
- Replaced JAX-RS annotations with Spring MVC annotations:
  - `@Path` -> `@RequestMapping`
  - `@GET` -> `@GetMapping`
  - `@POST` -> `@PostMapping`
- Replaced CDI field injection (`@Inject`) with constructor injection.
- Removed business logic from controller and delegated behavior to `MemberService`.
- Replaced JAX-RS `Response` usage with Spring `ResponseEntity`.
- Kept JSON request/response behavior via Spring Boot defaults.

## Added logic after parity review
- Added explicit JSON media type mappings for all endpoints:
  - `@GetMapping(... produces = application/json)`
  - `@PostMapping(... consumes = application/json, produces = application/json)`
- Added numeric-only ID path constraint to match legacy behavior:
  - `GET /rest/members/{id:[0-9][0-9]*}`
- Added controller-level validation/error response translation to match legacy response style while keeping business logic in service:
  - `MethodArgumentNotValidException` -> `400` with `{ field: message }`
  - `ConstraintViolationException` -> `400` with `{ field: message }`
  - `ValidationException` -> `409` with `{ "email": "Email taken" }`
  - `IllegalArgumentException` -> `400` with `{ "error": "..." }`

## Service and persistence migration added
- Migrated legacy repository behavior to Spring Data JPA interface methods:
  - `findByEmail(String email)`
  - `findAllByOrderByNameAsc()`
- Moved legacy registration and validation behavior into `MemberService`:
  - bean validation through `Validator`
  - duplicate email check before save
  - registration via `memberRepository.save(member)`
- Added transaction boundaries in service:
  - read-only transactions for list/read
  - write transaction for create
- Kept controller thin by delegating all business decisions to `MemberService`.

## Build/runtime alignment updates
- Added `spring-boot-starter-validation` to `pom.xml` so `jakarta.validation` types resolve and execute.
- Updated `KitchensinkModernizedApplication` with `@SpringBootApplication(scanBasePackages = "com.iskren")` so all migrated packages are discovered at runtime.

## Notes
- Original legacy controller included validation and duplicate-email checks directly in the resource class.
- In the Spring migration, those concerns are expected to be handled by `MemberService` (and service-layer validation/exception handling).
- The migrated controller assumes `MemberService` exists with methods used by the controller.
- The controller now preserves legacy API contract details (path pattern and validation-oriented response payload shape) without moving core business rules into the controller.

## Other important information
- New controller file:
  - `src/main/java/com/iskren/controller/MemberResourceRESTController.java`
- Added files:
  - `src/main/java/com/iskren/model/Member.java`
  - `src/main/java/com/iskren/repository/MemberRepository.java`
  - `src/main/java/com/iskren/service/MemberService.java`
- Updated files:
  - `src/main/java/com/iskren/kitchensink_modernized/KitchensinkModernizedApplication.java`
  - `pom.xml`

## Suggested Commit Message
```text
feat: migrate JAX-RS endpoints to Spring REST Controllers

- Converted MemberResourceRESTService to MemberResourceRESTController
- Implemented MemberService, MemberRepository, and Member model
- Preserved legacy API contract, path constraints, and validation error responses
- Added spring-boot-starter-validation dependency
- Updated component scanning in main application class
```
