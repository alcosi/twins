---
project_name: 'twins'
user_name: 'Nikit'
date: '2026-06-16'
sections_completed:
  ['technology_stack', 'language_rules', 'framework_rules', 'testing_rules', 'code_quality_rules', 'workflow_rules', 'critical_rules']
status: 'complete'
rule_count: 120
optimized_for_llm: true
existing_patterns_found: 12
---

# Project Context for AI Agents

_This file contains critical rules and patterns that AI agents must follow when implementing code in this project. Focus on unobvious details that agents might otherwise miss._

---

## Technology Stack & Versions

### Core Platform
- **Java**: 21 (with `--enable-preview` enabled on `JavaCompile`, `Test`, and `JavaExec` tasks)
- **Build**: Gradle 8.2 (Groovy DSL, NOT Kotlin DSL)
- **Spring Boot**: 3.5.3 (Spring Framework 6.x under the hood)
- **Web server**: Undertow — Tomcat, Jetty, and `tomcat-embed-el` are explicitly excluded in `core/build.gradle`

### Persistence
- **JPA / ORM**: Hibernate 6.4.1.Final + `hypersistence-utils-hibernate-63` 3.9.0
- **Database**: PostgreSQL, driver `org.postgresql:postgresql:42.7.9`
- **Migrations**: Flyway 11.0.0 — 400+ migrations in `core/src/main/resources/db/migration/`, naming `V1.x.y.z__TICKET_description.sql`; backlog in `db/migration_backlog/`
- **Connection pool**: NOT HikariCP — explicitly excluded; uses Spring Boot JDBC default

### Caching
- Caffeine (Spring Cache starter)
- cache2k 2.6.1.Final (API + core + Spring integration)

### API surface
- **REST**: Spring MVC (via Undertow), OpenAPI via `springdoc-openapi-starter-webmvc-ui` 2.8.6 + `swagger-core-jakarta` 2.2.25
- **gRPC**: 1.76.0 (netty-shaded runtime) + Protobuf Java 4.32.1 + `protoc` 3.25.5
- **GraphQL**: resolved under `controller/graphql/` and `dto/graphql/`

### Security & resilience
- `spring-security-crypto`
- Jasypt 1.9.3 (encryption)
- Resilience4j 2.3.0 (`resilience4j-spring-boot3`)

### Observability
- `spring-boot-starter-actuator`
- `micrometer-registry-prometheus`
- `log4j2-masking-factory` 1.0.3 + `log-time-aspect-starter` 1.0.1 (PII masking + timed aspects)
- Logging via log4j2 (Spring Boot default logging excluded)

### Code generation
- Lombok 1.18.38 (`compileOnly` + `annotationProcessor`)
- JavaParser 3.26.2 (buildscript classpath — used by custom Gradle tasks)
- Custom Gradle tasks: `generateRelatedObjectsDTO`, `generateInitRelatedObjectsDTO`, `generateTypeScriptDTOsTask`

### Frameworks & libs
- **Cambium** (`org.cambium.*`) — internal framework providing `EntitySecureFindServiceImpl`, `Featurer`, `Kit`, `ServiceException`. Entity scanning covers both `org.twins.core.dao` and `org.cambium.*`.
- Apache Commons: `commons-lang3` 3.18.0, `commons-collections4` 4.4, `commons-codec` 1.17.1
- Guava 32.1.3-jre
- UUID: `com.github.f4b6a3:uuid-creator` 6.1.1
- Validation: `spring-boot-starter-validation` + `commons-validator` 1.10.1
- Messaging: Spring AMQP (`spring-rabbit`, `spring-amqp`)
- Storage: MinIO 8.6.0
- HTTP client: Apache `httpclient5` 5.4.3 (replaces excluded `httpclient`)

### Test stack
- JUnit 5 (Jupiter) via `spring-boot-starter-test`
- Testcontainers: `postgresql`, `junit-jupiter`, `spring-boot-testcontainers`
- JUnit Platform runner (`useJUnitPlatform()`)

### Deployment
- Container base image: `bellsoft/liberica-openjre-alpine:21.0.10`
- Docker port: `8443`
- Docker plugin: `com.bmuschko.docker-remote-api` 9.3.4
- JAR manifest carries `Build-Revision`, `Build-Display`, `Build-Tag` from `net.nemerosa.versioning` 3.1.0

---

## Critical Implementation Rules

### Language-Specific Rules (Java 21)

**Preview features:**
- Project is compiled with `--enable-preview` on every `JavaCompile`, `Test`, and `JavaExec` task. Preview features in Java 21 are available.
- Before using a preview feature (e.g., pattern matching in switch, string templates, scoped values), confirm it does not require additional `@SuppressWarnings("preview")` plumbing that might leak into consumer code.
- Patterns currently used in the codebase: switch expressions over enums, `instanceof` patterns, text blocks.

**Lombok discipline:**
- `@Data` is FORBIDDEN on Hibernate entities — it generates `equals`/`hashCode` over lazy associations, breaking persistence and causing N+1. Use `@Getter`/`@Setter` + manual control over `equals`/`hashCode`/`toString`.
- Entities must use `@EqualsAndHashCode.Exclude` and `@ToString.Exclude` on every `@ManyToOne`, `@OneToMany`, and `@Transient` runtime relation field. Without these, Lombok-generated methods will touch lazy proxies.
- `@Accessors(chain = true)` is the standard — every setter returns `this`, enabling fluent builder-style chains.
- `@FieldNameConstants` (Lombok) is used on entities so `Entity.Fields.someField` gives type-safe column names in Specifications.

**Generics & typing:**
- Services are strongly typed over entity: `TwinsEntitySecureFindService<TwinEntity>`, never raw `EntitySecureFindServiceImpl`.
- Repositories extend BOTH `CrudRepository<E, UUID>` AND `JpaSpecificationExecutor<E>` — ID type is always `UUID`, never `Long` or `String`.
- Mappers are typed pairs: `RestSimpleDTOMapper<EntityT, DTOT>` with two type params. Never use raw `RestSimpleDTOMapper`.

**Nullness & collections:**
- DTO collections are initialized at the service level, not in field declarations.
- Use `org.apache.commons.collections4.CollectionUtils`/`MapUtils` for empty checks (`CollectionUtils.isEmpty`, `MapUtils.isEmpty`).
- `Kit` (Cambium) and `KitGrouped` are the canonical collection wrappers in the service layer for batch operations — never roll your own grouping.

**Exceptions:**
- Business errors throw `ServiceException` (from `org.cambium.*`), never `IllegalArgumentException` or `RuntimeException`.
- Controllers catch `ServiceException` and generic `Exception` separately and route through `ApiController.createErrorRs(...)`.

**Equality & UUID:**
- Entities are identified by `UUID`. Use `java.util.UUID`, never `String` representations.
- For UUID generation, use `org.cambium.common.util.UuidUtils.generate()` (already on classpath) — not `UUID.randomUUID()` when sequential/time-based UUIDs are needed for index locality.

**Date / time:**
- Use `java.time.*` types. For DB columns, store `Instant` (UTC) or `LocalDateTime`.
- Never use `java.util.Date` or `java.sql.Timestamp` in new code.

### Framework-Specific Rules

**Spring Boot architecture:**
- Main class is `org.twins.core.Application`. Single Gradle submodule: `core` (artifact `twins-core`).
- Layered flow enforced: `controller/rest/{priv,pub,auth}` → `service/{domain}/` → `dao/{domain}/` → DB. DTOs live in `dto/rest/{domain}`, mappers in `mappers/rest/{domain}`. **DTOs MUST NOT depend on entities.**
- Web server is Undertow. Never assume Tomcat APIs (e.g., `TomcatServletWebServerFactory`) are on the classpath.
- HikariCP is excluded. Do not introduce Hikari-specific configuration (`spring.datasource.hikari.*`).

**Security & multi-tenancy:**
- `AuthService` carries thread-local `ApiUser` context with domain + business account resolution. Read it via `AuthService.getApiUser()`, (in rare cases via custom thread-locals, mostly by background jobs).
- Permission checks live in the SERVICE layer via `isEntityReadDenied()` override — never only at the controller. The controller `@ProtectedBy` is the outer gate; the service is the inner one.
- Multi-tenant isolation is enforced via `domainId`. Every entity with multi-tenant semantics MUST call `checkDomainAccessDenied(entity.getDomainId(), ...)` in `isEntityReadDenied()`.
- Controllers extend `ApiController` — provides `createErrorRs(...)` and standard response wrapping. Do not return raw entities.

**Services — `EntitySecureFindServiceImpl` pattern:**
- All CRUD services extend `TwinsEntitySecureFindService<E>` (which extends `EntitySecureFindServiceImpl<E>` from Cambium).
- Four methods MUST be implemented on every CRUD service:
  1. `entityRepository()` — return the Spring Data repository
  2. `entityGetIdFunction()` — return `Entity::getId`
  3. `isEntityReadDenied(E entity, ReadPermissionCheckMode mode)` — domain/permission gate
  4. `validateEntity(E entity, EntityValidateMode mode)` — business validation; return `false` when valid
- Search services extend `EntitySearchService<S, E, SF, GF>` (eight abstract methods — see `api_starter.md`).
- All write operations (`create`, `update`, `delete`, save variants) MUST carry `@Transactional(rollbackFor = Throwable.class)`. The default `@Transactional` rolls back only on `RuntimeException` — that's not enough.

**JPA entities (project-specific):**
- Package: `org.twins.core.dao.<domain>`. Always annotate columns: `@Column(name = "snake_case")` — never rely on Hibernate naming strategy for column names.
- FK fields: keep BOTH the `UUID someId` column AND the `@ManyToOne` relation (with `@JoinColumn(insertable = false, updatable = false)`). Project rule: store the FK id + use `@Transient` runtime field for navigation, while a deprecated `*SpecOnly` field carries the JPA relation for Specification/Criteria API only. See `entity_many_to_one_relations.md` and `transient_vs_many_to_one.md` — full naming convention is non-obvious and critical.
- Every `@ManyToOne` and `@OneToMany` MUST declare `fetch = FetchType.LAZY` explicitly. No bare `@ManyToOne` / `@OneToMany`.
- `@OneToMany` relations get `@JoinColumn(name = "fk_column", insertable = false, updatable = false)` to make them unidirectional-safe.
- Lombok `@Data` is forbidden on entities — see Language Rules.

**Mappers:**
- All mappers extend `RestSimpleDTOMapper<SRC, DST>`. They are TWO-way (forward entity→DTO and reverse DTO→domain command).
- Mappers are context-aware: `map(SRC src, DST dst, MapperContext mapperContext)`. Modes are bound via `@MapperModeBinding(modes = {Entity}Mode.class)` and selected via `mapperContext.getModeOrUse({Entity}Mode.SHORT)`.
- `beforeCollectionConversion(...)` is where batch load methods are called — this is the N+1 prevention point. Every mapper converting a collection with `@Transient` fields MUST implement this method.
- Controllers pass mapper context via `@MapperContextBinding(roots = {Entity}RestDTOMapper.class, response = {Entity}ListRsDTOv1.class)` annotation on the `@RequestBody` parameter.

**Specifications / search:**
- Complex queries use JPA Criteria API via `Specification<E>`. Project uses `CommonSpecification` and `I18nSpecification` helpers — see `api_sorting_architecture.md`.
- Sorting uses enums in `enums/sort/` package. `sortField` and `sortDirection` live on the `SearchRqDTO`, NOT on the `SearchDTO` (so the same `SearchDTO` can be reused in count/grouping APIs).
- For I18n sort fields, use `I18nSpecification.toSortSpecification(ascending, locale, fieldPath...)`. Regular sort fields use `CommonSpecification.toSortSpecification(ascending, fieldPath...)`.
- Always include `getResultType()` guard for Criteria queries (handled by `CommonSpecification`).

**Featurer system:**
- Pluggable features extend `FeaturerTwins` (from Cambium). A `Featurer` is registered, parameterized at runtime, and resolved by the service layer.
- When implementing a new feature, follow the existing `featurer/<domain>/` package layout. Never instantiate featurers directly — resolve via the featurer registry.

**Flyway migrations:**
- File naming: `V1.x.y.z__TICKET_description.sql` in `core/src/main/resources/db/migration/`. Backlog migrations go in `db/migration_backlog/`.
- Define foreign keys inline in `CREATE TABLE`, NOT via `ALTER TABLE`.
- No explicit `public.` schema qualifier — the project relies on `search_path`.
- Use `IF NOT EXISTS` for idempotency.
- Add an index for every FK column and every column that appears in a `WHERE` / `ORDER BY` / `GROUP BY` clause.
- Triggers use the **Wrapper Functions** pattern — wrapper functions delegate to specialized procedures. Never embed business logic directly in trigger bodies. See `db_trigger_functions_convention.md`.

**Caching:**
- Three cache providers are on the classpath: Spring Cache (Caffeine), cache2k, and Hibernate L2 cache.
- Use `@Cacheable` / `@CacheEvict` on service methods only — never on repositories.
- Cache keys are domain-scoped: include `domainId` in the key for multi-tenant entities.

**Async & messaging:**
- Spring `@Async` is configured via `config/`. Async methods return `CompletableFuture<Void>` or `void` — never `Future` (legacy).
- RabbitMQ via Spring AMQP. Producers/consumers live under `service/messaging/`. Messages are JSON-serialized.

### Testing Rules

**Test stack & layout:**
- JUnit 5 (Jupiter) only. `useJUnitPlatform()` is configured in `core/build.gradle`. JUnit 4 (`@org.junit.Test`) is forbidden in new tests.
- Test sources mirror main layout: `core/src/test/java/org/twins/core/{service,mappers,controller,...}/<Domain>Test.java`.
- Pure JUnit / fast unit tests live alongside integration tests in the same module — there is no separate `integration-test` source set.

**Integration test base:**
- Every integration test MUST extend `org.twins.core.base.BaseIntegrationTest` (annotated with `@SpringBootTest` + `@Testcontainers` + `@ImportTestcontainers(PostgresContainers.class)`).
- Postgres container is `postgres:16-alpine`, initialized via `init_db.sql` resource. Container is shared via `@ServiceConnection` — no manual JDBC URL wiring in tests.
- Do NOT start a separate PostgreSQL via Docker Compose for tests — Testcontainers is the canonical path.

**Test database:**
- Hibernate `ddl-auto=validate` (in `application-stage.properties`) — schema is owned by Flyway. Tests run all Flyway migrations on container startup.
- Tests must not rely on data left behind by previous tests. Use `@Transactional` + default rollback for unit-scoped data, or explicit cleanup for cross-transaction tests.
- Never mutate shared reference data (e.g., system enums, lookup tables) in a test — this corrupts sibling tests in the same container.

**Mocking discipline:**
- Prefer **real beans** over mocks for service-layer integration tests. The container spins up fast and exposes real JPA behavior — mocks hide Hibernate-level bugs.
- Use `@MockBean` sparingly, only for external systems (MinIO, RabbitMQ broker, downstream HTTP services). Never `@MockBean` for `Repository`, `EntityManager`, or `MapperContext`.
- For pure unit tests, prefer constructor injection (Lombok `@RequiredArgsConstructor`) and plain Mockito `@ExtendWith(MockitoExtension.class)`.

**Test naming & structure:**
- Class: `<Subject>Test` — e.g., `TwinActionServiceCheckAllowedTest`.
- Method: `should<ExpectedBehavior>When<Condition>` or `<methodName>_<Condition>_<Outcome>` — both styles exist; pick the one that reads naturally.
- One assertion concept per test. If multiple `assertThat` calls are needed, they should verify the same logical outcome from different angles.

**Running tests:**
- Single class: `./gradlew test --tests "org.twins.core.service.twin.TwinActionServiceCheckAllowedTest"`
- Single method: `./gradlew test --tests "org.twins.core.service.twin.TwinActionServiceCheckAllowedTest.testMethodName"`
- All tests: `./gradlew test`

**Testcontainers lifecycle:**
- The container is declared as a `static` field on the `PostgresContainers` interface — it is reused across test classes in the same JVM run.
- For tests that need a fresh DB, use `@DirtiesContext` (expensive) rather than restarting the container.

**Forbidden patterns:**
- `Thread.sleep(...)` to wait for async results. Use `Awaitility` (`await().atMost(...)`) — already available via Spring Boot Test.
- `System.out.println` for debugging — use SLF4J (`@Slf4j` + `log.info(...)`).
- Skipping tests via `@Disabled` without a linked ticket in the comment.

### Code Quality & Style Rules

**Package organization (strict):**
- `controller/rest/priv/` — authenticated endpoints (requires auth)
- `controller/rest/pub/` — public endpoints (no auth)
- `controller/rest/auth/` — authentication endpoints
- `service/<domain>/` — business logic; services extend `EntitySecureFindServiceImpl<T>` via `TwinsEntitySecureFindService`
- `dao/<domain>/` — JPA entities + Spring Data repositories
- `dao/specifications/<domain>/` — JPA Criteria API specifications for complex queries
- `dto/rest/<domain>/` — request/response DTOs (never depend on entities)
- `mappers/rest/<domain>/` — DTO ↔ Entity mapping
- `domain/<domain>/` — domain model objects (NOT JPA entities — command/value objects)
- `featurer/<domain>/` — pluggable feature system extending `FeaturerTwins`
- `config/` — Spring configuration (JPA, caching, Jackson, async, etc.)
- `enums/sort/` — sort & group field enums

**Naming conventions:**
- Entities: `<Domain>Entity` (e.g., `TwinEntity`, `TwinClassEntity`)
- Repositories: `<Domain>Repository`
- Services: `<Domain>Service` (CRUD) + `<Domain>SearchService` (search/count)
- Mappers: `<Domain>RestDTOMapper` (forward) + `<Domain>CreateRestDTOReverseMapper` / `Update` / `Search` (reverse)
- Controllers: split by operation — `<Domain>CreateController`, `<Domain>UpdateController`, etc.
- DTOs follow strict hierarchy: see `docs/dto_code_convention.md` and `docs/api_starter.md`

**DTO suffixes (mandatory):**
- `RqDTOv1` / `RqDTO` — request DTOs (suffix `v1` is used for versioned endpoints; plain `RqDTO` exists for legacy)
- `RsDTOv1` / `RsDTO` — response DTOs
- `DTOv1` — entity representation DTO
- `SaveDTOv1` / `CreateDTOv1` / `UpdateDTOv1` / `SearchDTOv1` / `CountDTOv1` — operation-specific DTOs
- The `id` UUID field appears ONLY in Update DTOs, never in Create DTOs

**Logging:**
- Entity logging uses `entity.logShort()`, `entity.logNormal()`, `entity.logDetailed()` — these return pre-formatted strings safe to pass into `log.info(...)` arguments.
- `entity.easyLog(Level.X)` is FORBIDDEN — it triggers lazy loading and is being phased out.
- Always use SLF4J parameterized messages: `log.info("Action {} for {}", action, entity.logShort())`. Never string-concatenation — parameterized logs avoid the cost when the level is disabled.
- Logger is Lombok `@Slf4j` on the class. Do not declare `private static final Logger LOGGER = ...` manually.
- PII fields are auto-masked via `log4j2-masking-factory` — never roll your own masking logic in log statements.

**DTO annotations (mandatory):**
- `@Schema(name = "...")` on every DTO class.
- `@Schema(description = "...", example = DTOExamples.UUID_ID)` on every public field that appears in API responses.
- `@RelatedObject(type = X.class, name = "x")` on UUID fields that resolve to a related DTO via `RelatedObjectsRestDTOConverter`.
- `@Data @Accessors(chain = true)` on every DTO class.

**Comments policy:**
- Default: NO comments. Only add a comment when the "why" is non-obvious (hidden constraint, subtle invariant, workaround for a specific bug).
- Never comment WHAT the code does — well-named identifiers cover that.
- Never reference the current task, ticket, or PR (`// TWINS-840 ...`, `// added for the new flow ...`) — that belongs in commit messages and rots in code.
- Javadoc is reserved for public API of `featurer` system and framework extension points (`EntitySecureFindServiceImpl` overrides). Private/package-private methods: no Javadoc.

**DTO/Entity separation:**
- DTOs must be flat. Nested DTOs only when there is a clear business necessity.
- DTOs must NOT depend on entities. Mappers translate between them — never expose an entity through a DTO.
- Entities must NOT be serialized to JSON directly (no `@JsonProperty` on entity fields). Use a DTO.

**Field ordering (entity):**
- `@Id` first
- Then columns in same order as DB DDL
- FK id columns next to their `@ManyToOne` SpecOnly relations
- `@Transient` runtime fields grouped at the end

**Validation:**
- Bean Validation (`jakarta.validation.*`) annotations on DTO fields, NOT on entities. Entities are validated via `validateEntity()` in the service.
- `@Valid` on `@RequestBody` parameters in controllers — kicks off DTO validation before the service is called.

**OpenAPI documentation:**
- Every controller method gets `@Operation`, `@ApiResponses`, `@ParametersApiUserHeaders`.
- API tags centralized in `controller/rest/ApiTag.java` — never inline tag strings in `@Tag(name = "...")`.

**Language:**
- All code, comments, identifiers, exception messages: English.
- Communication (chat, PRs, planning): Russian per project `CLAUDE.md`.

### Development Workflow Rules

**OS & tooling:**
- Development OS: differ for each developer. All paths, scripts, and shell commands must work on Windows.
- Shell in this project is bash (Git Bash on Windows). Use Unix syntax: forward slashes in paths, `/dev/null` not `NUL`.
- IDE: IntelliJ IDEA is the team default. Lombok plugin + annotation processing must be enabled.

**Build & run:**
- Build: `./gradlew build`
- Run a single test class: `./gradlew test --tests "org.twins.core.service.twin.TwinActionServiceCheckAllowedTest"`
- Run with a specific profile: `./gradlew build -Pprofile=dev`
- Profiles: `dev`, `devlocal`, `localhost`, `onsdev2`, `onstest`, `stage`. Properties live in `core/src/main/resources/application-<profile>.properties`.
- Default profile when none is passed: `stage`.
- Code generation tasks (run after modifying DTOs that need TypeScript/related-objects helpers):
  - `./gradlew generateRelatedObjectsDTO`
  - `./gradlew generateInitRelatedObjectsDTO`
  - `./gradlew generateTypeScriptDTOsTask`
- Docker image build: `./gradlew :core:buildDockerImage`

**Branches & git:**
- Main branch: `main`. PRs target `main`.
- Branch naming: ticket-based — `TWINS-<number>-<short-slug>` (e.g., `TWINS-840-comments-sort-group`). Follow Jira ticket numbers.
- When Claude generates a new file, the user runs `git add <filename>` immediately (project rule in `CLAUDE.md`). AI does NOT auto-stage or commit.
- AI never runs `git commit` or `git push` — these are explicit user actions. AI may prepare a commit message but the user executes.
- Pre-commit hooks MUST run. Never bypass with `--no-verify` unless the user explicitly requests it. If a hook fails, investigate the root cause.

**Commit messages:**
- Format: `TWINS-<ticket>: <imperative summary>` — e.g., `TWINS-840: comments sort/group`.
- Body (optional): bullet points explaining the "why", not the "what". Reference the ticket but do not duplicate its content.
- Commits are co-authored when AI assists: include `Co-Authored-By: Claude ... <noreply@anthropic.com>` in the trailer.

**Pull requests:**
- Title: same format as commits (`TWINS-<ticket>: <summary>`), under 70 chars.
- Description in body, not title. Include `## Summary` + `## Test plan` (markdown checklist).
- Squash-merge is the team default — the PR title becomes the squash commit message.

**Flyway migration workflow:**
- New migration: create `core/src/main/resources/db/migration/V1.x.y.z__TICKET_description.sql`. The next version number is `MAX(existing) + 1` in the same `x.y.z` family.
- Backlog migrations (not yet ready for production): `db/migration_backlog/`.
- Never edit a migration that has been merged to `main` — Flyway checksums will fail. Create a new migration instead.
- Always test migrations against a fresh Testcontainer Postgres (`./gradlew test`).

**Profile management:**
- Profile is selected at build time via `-Pprofile=<name>`. The selected `application-<profile>.properties` is renamed to `application.properties` in the build output. Other profile files are deleted.
- NEVER commit `application.properties` (the merged output) — only `application-<profile>.properties`.
- Secrets in properties are Jasypt-encrypted. Never commit plaintext credentials.

**Code generation pipeline (DTOs):**
- Modify DTO → run `./gradlew generateRelatedObjectsDTO` → run `./gradlew generateInitRelatedObjectsDTO` → run `./gradlew generateTypeScriptDTOsTask` (if TS clients need to be regenerated).
- Generated code lives under `dto/rest/<domain>/generated/` — never edit by hand.

**AI agent etiquette (from project `CLAUDE.md`):**
- Communication in Russian.
- Code, comments, identifiers, exception messages in English.
- Always run `git add <filename>` immediately for newly created files.
- Never `git commit` / `git push` autonomously.
- For PR code reviews via `expert-panel` skill: save results to `ai/review/PR-<PR-number>.md`.
- For feature planning via `expert-panel` skill: save results to `ai/plans/<feature>.md`.

**Context7 MCP usage:**
- When library/API documentation, code generation, or setup steps are needed, use Context7 MCP — do not ask the user to provide docs manually (project rule).

**Documentation:**
- Architecture docs live in `docs/` (e.g., `dto_code_convention.md`, `api_starter.md`, `entity_many_to_one_relations.md`, `load_method_pattern.md`).
- AI/agent review artifacts: `ai/review/`. AI/agent feature plans: `ai/plans/`. AI memory: `ai/memory/`.
- BMad artifacts: `_bmad-output/{planning,implementation,test}-artifacts/`.

### Critical Don't-Miss Rules

**Anti-patterns to avoid (FORBIDDEN):**

- **`@Data` on Hibernate entities** — generates `equals`/`hashCode` over lazy proxies, triggers N+1. Use `@Getter`/`@Setter`.
- **Bare `@ManyToOne` / `@OneToMany` without `fetch = FetchType.LAZY`** — JPA defaults to EAGER for `@ManyToOne`.
- **Accessing `*SpecOnly` fields outside Specifications** — these are deprecated, getter is `AccessLevel.NONE`, and using them in mappers/services/controllers causes N+1.
- **`entity.getUser().getName()` in business logic** — `getUser()` may trigger lazy load. Use the bulk-loaded `@Transient` runtime field (`entity.getUser()` on the non-SpecOnly field that has been populated by a `load*` method).
- **`open-in-view=true` reliance** — silent lazy loading in the controller layer hides N+1. Always disable in mental model; load explicitly in service / mapper.
- **`@Transactional` without `rollbackFor = Throwable.class`** — default rolls back only on unchecked exceptions. Checked exceptions silently commit.
- **String-concatenated log messages** — `log.info("User " + user.getId())` evaluates even when INFO is disabled. Use `log.info("User {}", user.getId())`.
- **`entity.easyLog(Level.X)`** — being phased out; triggers lazy loading. Use `entity.logShort()` / `logNormal()` / `logDetailed()`.
- **`@MockBean` for `Repository` or `EntityManager`** — masks JPA-level bugs.
- **Manual `UUID.randomUUID()` for index-heavy rows** — causes index fragmentation. Use `uuid-creator` time-ordered variants.
- **Editing a merged Flyway migration** — checksum mismatch breaks production. Add a new migration.
- **Committing `application.properties`** — only commit `application-<profile>.properties`.
- **Committing secrets in plaintext** — use Jasypt encryption.
- **`--no-verify` to bypass pre-commit hooks** — investigate the root cause instead.
- **`@Disabled` on tests without a linked ticket** in the comment.

**N+1 traps (project is paranoid about these):**

- **Mappers in collection mode** — `beforeCollectionConversion(...)` is MANDATORY if the entity has `@Transient` runtime fields. Forgetting this is the #1 source of N+1.
- **`@OneToMany` collection access** (`entity.getTags().size()`, iteration in loops) — full SELECT per owner. Use `loadKit` pattern.
- **Calling `entity.getSomeSpecOnly()` in a stream / lambda** — even a single accidental call is a SELECT.
- **Debugging via IDE expression evaluator** — touching a lazy proxy in the debugger issues SQL in the background. Be careful when inspecting `@Transient` fields.
- **Serializer / `@JsonProperty` over lazy fields** — Jackson will trigger lazy loading. Entities must never be serialized directly.

**Multi-tenant isolation:**

- Every read of an entity with `domainId` MUST pass through `findEntitySafe()` / `findEntitiesSafe()` which calls `isEntityReadDenied()`.
- Bypassing `findEntitySafe` (e.g., `repository.findById(id)`) skips the domain check — this is a SECURITY BUG.
- Cross-domain data leakage is the most severe class of bug for this codebase. When in doubt, route through `findEntitySafe`.

**Performance:**

- Hibernate `batch_size=10` and `order_inserts=true` are configured globally — rely on them; do not disable per-query.
- Pagination is mandatory for any unbounded list. Use `SimplePagination` (`@SimplePaginationParams`) — never return unbounded `List<Entity>` from a controller.
- For high-cardinality aggregates (count of twins by user), do NOT add `userId` as a group field — cardinality kills the count query.
- Sorting by I18n fields uses LEFT JOIN — expensive. Prefer non-I18n sort when possible.

**Edge cases:**

- **`SortDirection` enum has only ASC/DESC.** Invalid values from clients get Jackson-deserialization error → 400 (handled automatically).
- **`SearchDTO` reused across Search and Count APIs.** Do NOT put sort fields here — they belong on the `SearchRqDTO`.
- **`groupFields` is mandatory** on count requests. Empty `Set` returns the total count.
- **`Set<{Entity}GroupField>` is annotated `@Size(max = 2)`** on count requests — exceeding returns 400.
- **Export endpoints return `ResponseEntity<byte[]>`**, not JSON. Do not wrap in `Response`. No `@MapperContextBinding`.
- **`MapperContext` is propagated via `@MapperContextBinding` on the controller method parameter.** Do not construct manually.

**Security:**

- `@ProtectedBy(Permissions.X)` on every controller method — no exceptions. Permissions are centralized in `Permissions` enum.
- Manage-level permission (`{ENTITY}_MANAGE`) is required for export operations, even though other reads accept `{ENTITY}_VIEW`.
- Every endpoint under `/private/*` requires authentication — extend `ApiController`, do not write raw `@RestController` without auth.
- PII fields auto-masked by `log4j2-masking-factory` — never log raw secrets, passwords, tokens, full emails.
- SQL injection: JPA Criteria API and Specifications prevent it. Never construct SQL via string concatenation in repositories.
- Flyway migrations with dynamic SQL inside stored procedures MUST use `quote_ident` / parameter binding — never interpolate.

**Open-in-view:**
- Spring Boot's `spring.jpa.open-in-view` defaults to `true`. The project does NOT rely on this — code MUST work correctly with it set to `false`. Lazy loading in controllers is a bug.

**i18n:**
- Default locale: `en` (`cambium.i18n.defaultLocale=en`).
- I18n fields are stored as `UUID` references to `i18n` table (multi-language key set). NEVER store raw strings in entity columns for translatable fields.
- Use `I18nService.collectI18nIds()` — never manually iterate to collect them.

**Jackson / JSON:**
- `spring.jackson.default-property-inclusion = NON_NULL` — null fields are omitted from JSON. DTOs do not need `@JsonInclude(NON_NULL)`.
- All DTOs use `@Data @Accessors(chain = true)` — setters return `this`, allowing fluent construction in tests.

**Transaction propagation:**
- Default propagation `REQUIRED` is the norm. Use `REQUIRES_NEW` only for audit-log writes that must succeed even if the outer transaction rolls back.
- Never start a transaction in a controller — transactions live on service methods only.

---

## Usage Guidelines

**For AI Agents:**

- Read this file before implementing any code in `twins`.
- Follow ALL rules exactly as documented — they capture non-obvious project invariants.
- When in doubt, prefer the more restrictive option (e.g., route through `findEntitySafe`, use `@Transactional(rollbackFor = Throwable.class)`, exclude `*SpecOnly` from non-Specification code).
- Read the referenced docs in `docs/` for in-depth patterns: `dto_code_convention.md`, `api_starter.md`, `entity_many_to_one_relations.md`, `transient_vs_many_to_one.md`, `load_method_pattern.md`, `api_sorting_architecture.md`, `api_counting_architecture.md`, `api_export_architecture.md`, `db_trigger_functions_convention.md`, `rest_api.md`.
- Update this file if a new pattern or invariant emerges during implementation.

**For Humans:**

- Keep this file lean — focused on rules AI agents would otherwise miss.
- Update when the technology stack changes (Spring Boot upgrades, new libraries, version bumps).
- Review quarterly for outdated rules.
- Remove rules that become obvious over time or that agents reliably follow without prompting.
- The `existing_patterns_found` count and `rule_count` in frontmatter are approximate; update when content meaningfully changes.

Last Updated: 2026-06-16
