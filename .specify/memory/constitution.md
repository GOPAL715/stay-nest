# Property Rental System Constitution

## Core Principles

### I. Technology Stack (NON-NEGOTIABLE)
- **Java 25** and **Spring Boot 4** are the only permitted versions.
- **Lombok** annotations must be used to eliminate boilerplate (`@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`, etc.).
- **MapStruct** is the sole mapping library between Entities and DTOs. No manual mapping code.
- **Liquibase** manages all schema changes exclusively via `.sql` migration files (no XML, YAML, or JSON changelogs). Migration files live in `src/main/resources/db/changelog/`.
- **Swagger/OpenAPI 3** annotations (`@Tag`, `@Operation`, `@ApiResponse`, `@Parameter`) must be present on every controller method.

### II. Architectural Patterns (NON-NEGOTIABLE)
- **Builder Pattern**: All DTOs and Entities must use `@Builder`. No all-args constructors as the sole construction mechanism.
- **Factory Pattern**: All domain object creation (Entities, complex value objects) must go through a dedicated Factory class. No `new Entity()` scattered across service code.
- **Service Layer Contracts**: Every service must have an **interface** (e.g., `PropertyService`) and a corresponding **implementation** class (e.g., `PropertyServiceImpl`). Controllers depend only on the interface.
- **Repository Layer**: Use Spring Data JPA repositories. Use **dynamic query methods** by convention for simple filters. Use **`JpaSpecificationExecutor`** only for Search/Filter APIs that require dynamic multi-field predicates — never for simple lookups.
- **SOLID Principles**: Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, and Dependency Inversion must be observed at all times. Each class has one reason to change.

### III. API & Controller Rules (NON-NEGOTIABLE)
- **Entities are never exposed from controllers.** Controllers receive and return DTOs only.
- **No try-catch in controllers.** All exceptions propagate to the global exception handler.
- **Standard API Response**: Every endpoint returns a unified `ApiResponse<T>` wrapper:
  ```json
  {
    "success": true,
    "message": "Operation successful",
    "data": { ... },
    "timestamp": "2025-06-12T10:00:00Z",
    "path": "/api/v1/properties"
  }
  ```
- **REST Naming Conventions**: Plural nouns for resources (`/api/v1/properties`), kebab-case for multi-word paths, HTTP verbs for actions (GET/POST/PUT/PATCH/DELETE). No verbs in URIs.
- **Pagination on all list APIs**: Every endpoint returning a collection must use `Pageable` and return `Page<T>` wrapped in `ApiResponse`.

### IV. Security (NON-NEGOTIABLE)
- **Spring Security with JWT**: Stateless authentication using short-lived access tokens and long-lived refresh tokens. No session-based auth.
- **Role-Based Access Control (RBAC)**: All endpoints secured with `@PreAuthorize` or Security config matchers. Roles defined as enums (e.g., `Role.ADMIN`, `Role.TENANT`, `Role.LANDLORD`).
- **No hardcoded secrets**: All secrets (JWT secret, DB credentials, etc.) must come from environment variables or externalized configuration (e.g., `application.yml` referencing `${JWT_SECRET}`). No literal secrets in source code.

### V. Data & Persistence (NON-NEGOTIABLE)
- **`BaseEntity`**: All entities extend a `BaseEntity` that provides audit fields: `id` (UUID), `createdAt`, `updatedAt`, `createdBy`, `updatedBy` using `@EntityListeners(AuditingEntityListener.class)`.
- **Enums over Strings**: All status, type, and category fields must use Java enums (e.g., `PropertyStatus.AVAILABLE`, `LeaseStatus.ACTIVE`). No raw String status values.
- **`@Data` is forbidden on entities.** Use explicit `@Getter`, `@Setter` (where needed), `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` and implement `equals`/`hashCode` based on business keys, not Lombok `@EqualsAndHashCode` on JPA entities.
- **Liquibase `.sql` only**: Every schema change (table creation, column add, index, constraint) must have a corresponding versioned `.sql` changeset. No `spring.jpa.hibernate.ddl-auto=update` in production profiles.

### VI. Code Quality & Injection (NON-NEGOTIABLE)
- **Constructor injection only.** `@Autowired` field injection and setter injection are forbidden. Use `@RequiredArgsConstructor` (Lombok) with `final` fields.
- **No field injection** (`@Autowired` on fields is banned entirely).
- **Bean Validation**: All request DTOs must use JSR-380 annotations (`@NotNull`, `@NotBlank`, `@Size`, `@Email`, `@Min`, `@Max`, etc.) and controllers must have `@Valid` on request bodies.
- **Global Exception Handling**: A single `@RestControllerAdvice` class handles all exceptions. Custom exceptions extend a base hierarchy (e.g., `ApplicationException`, `ResourceNotFoundException`, `BusinessRuleException`, `ValidationException`).

### VII. Testing (NON-NEGOTIABLE)
- **JUnit 5 + Mockito** tests are mandatory for the **service layer**. Every `ServiceImpl` class must have a corresponding `*ServiceImplTest` class.
- Test coverage must cover: happy path, edge cases, and exception scenarios.
- Use `@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks` patterns.
- No Spring context loading in unit tests — pure unit tests for service layer.

---

## Package Structure

```
com.rental.property
├── config/           # Security, Swagger, Audit, Bean configs
├── controller/       # REST Controllers (DTOs only, no entities)
├── dto/
│   ├── request/      # Incoming request DTOs with validation
│   └── response/     # Outgoing response DTOs
├── entity/           # JPA Entities (extend BaseEntity)
├── enums/            # All enum types (Status, Role, Type)
├── exception/        # Custom exceptions + GlobalExceptionHandler
├── factory/          # Factory classes for domain object creation
├── mapper/           # MapStruct mapper interfaces
├── repository/       # Spring Data JPA repositories
├── security/         # JWT filter, provider, user details service
├── service/
│   ├── (interfaces)  # Service interfaces
│   └── impl/         # ServiceImpl classes
└── util/             # Shared utilities (ApiResponse, constants)
```

---

## Technology Constraints

| Concern | Mandated Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4 |
| ORM | Spring Data JPA / Hibernate |
| Schema Migration | Liquibase (`.sql` only) |
| Mapping | MapStruct |
| Boilerplate | Lombok |
| Security | Spring Security + JWT + Refresh Tokens |
| Validation | Bean Validation (JSR-380) |
| API Docs | Swagger / OpenAPI 3 |
| Testing | JUnit 5 + Mockito |
| Build | Maven or Gradle |

---

## Quality Gates

Every feature must satisfy **all** of the following before being considered complete:

- [ ] No entity returned from any controller method
- [ ] No try-catch block inside any controller
- [ ] All request DTOs annotated with Bean Validation constraints
- [ ] All list endpoints paginated
- [ ] Service interface exists alongside ServiceImpl
- [ ] Factory used for all entity creation
- [ ] MapStruct mapper exists for every entity↔DTO conversion
- [ ] Liquibase `.sql` migration created for every schema change
- [ ] Global exception handler covers all thrown custom exceptions
- [ ] JWT + RBAC applied to all secured endpoints
- [ ] No hardcoded secrets anywhere in source
- [ ] Constructor injection used throughout (no `@Autowired` on fields)
- [ ] Enums used for all status/type fields
- [ ] Audit fields present via `BaseEntity`
- [ ] Swagger annotations on all controller methods
- [ ] JUnit + Mockito unit tests written for all service methods
- [ ] `ApiResponse<T>` wrapper used on all endpoints

---

## Governance

- This constitution **supersedes all other development practices and conventions**.
- Any deviation from these rules requires explicit documentation and must be treated as a **technical debt** item.
- No PR or feature is complete unless all Quality Gates above are satisfied.
- All code is expected to be **enterprise-ready, interview-quality, production-grade**.
- YAGNI (You Aren't Gonna Need It) applies — no over-engineering, but no shortcuts on the above rules either.

**Version**: 1.0.0 | **Ratified**: 2026-06-12 | **Last Amended**: 2026-06-12
