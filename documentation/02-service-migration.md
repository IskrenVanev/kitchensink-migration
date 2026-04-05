# Service Migration: MemberRegistration EJB to Spring Service

## What was done
- Located legacy EJB: `org.jboss.as.quickstarts.kitchensink.service.MemberRegistration`
- Migrated and expanded it into: `com.iskren.service.MemberService`
- Added missing parity features from the legacy EJB:
  - Logging on member registration
  - Validation and duplicate-email handling in service layer

## Key changes

### Legacy EJB (`MemberRegistration`)
- `@Stateless` → `@Service`
- `@Inject EntityManager em` → Spring Data MongoDB via `MemberRepository`
- `@Inject Logger log` → `private static final Logger log = LoggerFactory.getLogger(...)` (SLF4J)
- `em.persist(member)` → `memberRepository.save(member)`

### Spring `MemberService` additions
- Added SLF4J `Logger` as a static final field (Spring Boot standard — replaces JUL used in legacy JBoss container)
- Added `log.info("Registering " + member.getName())` before save
- Expanded service beyond legacy EJB to include:
  - `listAllMembers()` — previously in the REST resource directly calling the repository
  - `lookupMemberById(String id)` — updated for MongoDB ObjectId string IDs
  - `validateMember(Member member)` — previously in the REST resource
  - `emailAlreadyExists(String email)` — previously in the REST resource

### Refinements applied during review
- **Logger**: Replaced `java.util.logging.Logger` with SLF4J (`org.slf4j.Logger` / `LoggerFactory`) — Spring Boot routes SLF4J to Logback by default; JUL was a JBoss container concern only
- **Removed redundant null check**: `if (member == null)` in `createMember` was dead code — Spring never passes `null` for `@RequestBody`; missing/malformed body results in `HttpMessageNotReadableException` before reaching the service
- **Generic exception handler**: Replaced `@ExceptionHandler(IllegalArgumentException.class)` with `@ExceptionHandler(Exception.class)` in the controller — the legacy `catch (Exception e)` block was a true catch-all returning `{"error": e.getMessage()}` as 400; the previous handler was too narrow
- **Validation debug log**: Added `log.debug("Validation completed. violations found: " + violations.size())` in `validateMember` — matches legacy `log.fine(...)` in `createViolationResponse`

## Notes
- The legacy CDI event (`Event<Member>`) had no visible listener in the legacy codebase. The modernized service keeps parity through explicit frontend list reload after successful registration.
- All business logic is contained in the service layer, keeping the controller thin.
- Constructor injection is used exclusively — no `@Autowired` on fields.

## Other important information
- Modified files:
  - `src/main/java/com/iskren/service/MemberService.java`
  - `src/main/java/com/iskren/controller/MemberResourceRESTController.java`

## Suggested Commit Message
```text
feat: migrate MemberRegistration EJB to Spring Service

- Replaced @Stateless EJB with @Service
- Added SLF4J logging on member registration (Spring Boot standard)
- Moved validation and registration logic fully into MemberService
- Removed dead null check on member parameter (handled by @RequestBody)
- Widened exception handler to catch all Exception types (matching legacy catch-all)
- Added debug log for constraint violations count (matching legacy log.fine)
```
