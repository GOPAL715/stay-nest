# Implementation Plan: StayNest — Property Management Platform

**Branch**: `main` | **Date**: 2026-06-12 | **Spec**: `.specify/memory/spec.md`

---

## Summary

StayNest is a full-stack Airbnb-style property rental platform. A single **Spring Boot 4 REST API** (Java 25) powers two **React 19** frontends — a customer-facing web app and an admin/host portal — backed by **PostgreSQL** and managed via **Liquibase `.sql` migrations**. The backend is containerised with Docker Compose. Security uses **Spring Security + JWT + Refresh Tokens** with role-based access (Super Admin, Property Manager, Host, Guest, Support Agent). All architectural constraints from the Constitution are enforced at every layer.

---

## Technical Context

| Concern | Decision |
|---|---|
| **Language** | Java 25 |
| **Framework** | Spring Boot 4 |
| **ORM** | Spring Data JPA / Hibernate |
| **Database** | PostgreSQL 16 |
| **Schema Migration** | Liquibase (`.sql` changelogs only) |
| **Mapping** | MapStruct 1.6+ |
| **Boilerplate** | Lombok |
| **Security** | Spring Security 6 + JJWT (JWT + Refresh Tokens) |
| **Validation** | Bean Validation (Jakarta Validation 3) |
| **API Docs** | SpringDoc OpenAPI 3 (Swagger UI at `/api/v1/docs`) |
| **Testing** | JUnit 5 + Mockito (70%+ service layer coverage) |
| **Build** | Maven |
| **Containerisation** | Docker + Docker Compose (API + PostgreSQL only) |
| **Frontend** | React 19, TanStack Router, TanStack Query, React Hook Form + Zod, Tailwind CSS |
| **Payments (Bonus)** | Razorpay (test mode) |
| **Cache (Bonus)** | Redis |
| **Messaging (Bonus)** | STOMP over SockJS (WebSockets) |
| **Target Platform** | Linux server (Docker) / local dev (Windows/Mac) |
| **Performance** | < 200ms p95 on search API; < 500ms p95 on booking creation |

---

## Constitution Check

- ✅ Java 25 + Spring Boot 4 only
- ✅ Lombok on all classes
- ✅ MapStruct for all entity↔DTO conversions
- ✅ Liquibase `.sql` only — no XML/YAML changelogs
- ✅ Builder Pattern on all DTOs and Entities
- ✅ Factory Pattern for all entity creation
- ✅ Service interface + ServiceImpl for every domain service
- ✅ `JpaSpecificationExecutor` only for Search API (Property search)
- ✅ No entities in controllers — DTOs only
- ✅ No try-catch in controllers — `@RestControllerAdvice` handles all
- ✅ Standard `ApiResponse<T>` wrapper on all endpoints
- ✅ Spring Security + JWT + Refresh Tokens
- ✅ RBAC with `@PreAuthorize`
- ✅ No hardcoded secrets — all from environment variables
- ✅ `BaseEntity` with audit fields on all entities
- ✅ Enums for all status/type/role fields
- ✅ `@Data` forbidden on entities
- ✅ Constructor injection only (`@RequiredArgsConstructor` + `final` fields)
- ✅ Bean Validation on all request DTOs
- ✅ Swagger annotations on all controller methods
- ✅ JUnit 5 + Mockito service layer tests
- ✅ Pagination on all list APIs
- ✅ REST naming conventions
- ✅ `.sql` Liquibase migrations only

---

## Monorepo Project Structure

```text
staynest/                              ← GitHub monorepo root
├── apps/
│   ├── customer/                      ← React 19 customer web app
│   │   ├── src/
│   │   │   ├── components/
│   │   │   ├── pages/
│   │   │   ├── hooks/
│   │   │   ├── services/              ← Fetch API + JWT interceptor
│   │   │   ├── stores/
│   │   │   └── utils/
│   │   ├── package.json
│   │   └── vite.config.ts
│   └── admin/                         ← React 19 admin/host portal
│       ├── src/
│       │   ├── components/
│       │   ├── pages/
│       │   ├── hooks/
│       │   ├── services/
│       │   └── utils/
│       ├── package.json
│       └── vite.config.ts
├── services/
│   └── api/                           ← Spring Boot 4 REST API
│       ├── src/
│       │   ├── main/
│       │   │   ├── java/com/staynest/api/
│       │   │   │   ├── config/
│       │   │   │   ├── controller/
│       │   │   │   ├── dto/
│       │   │   │   │   ├── request/
│       │   │   │   │   └── response/
│       │   │   │   ├── entity/
│       │   │   │   ├── enums/
│       │   │   │   ├── exception/
│       │   │   │   ├── factory/
│       │   │   │   ├── mapper/
│       │   │   │   ├── repository/
│       │   │   │   ├── security/
│       │   │   │   ├── service/
│       │   │   │   │   └── impl/
│       │   │   │   └── util/
│       │   │   └── resources/
│       │   │       ├── db/
│       │   │       │   └── changelog/
│       │   │       │       ├── db.changelog-master.xml   ← master includes only
│       │   │       │       └── migrations/
│       │   │       │           ├── V001__create_users.sql
│       │   │       │           ├── V002__create_properties.sql
│       │   │       │           ├── V003__create_amenities.sql
│       │   │       │           ├── V004__create_bookings.sql
│       │   │       │           ├── V005__create_reviews.sql
│       │   │       │           ├── V006__create_messages.sql
│       │   │       │           ├── V007__create_notifications.sql
│       │   │       │           ├── V008__create_payments.sql
│       │   │       │           ├── V009__create_platform_config.sql
│       │   │       │           ├── V010__create_host_applications.sql
│       │   │       │           ├── V011__create_audit_logs.sql
│       │   │       │           └── V012__seed_data.sql
│       │   │       ├── application.yml
│       │   │       ├── application-dev.yml
│       │   │       └── application-prod.yml
│       │   └── test/java/com/staynest/api/
│       │       └── service/           ← JUnit 5 + Mockito tests
│       ├── pom.xml
│       └── Dockerfile
├── docker-compose.yml
├── .gitignore
└── README.md
```

---

## Database Schema Design

### BaseEntity (abstract — all entities extend this)
```
id          UUID PRIMARY KEY DEFAULT gen_random_uuid()
created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
created_by  VARCHAR(255)
updated_by  VARCHAR(255)
```

### V001 — users
```sql
id, email (UNIQUE), password_hash, first_name, last_name,
phone, profile_picture_url, role (ENUM), status (ENUM),
email_verified, verification_token, verification_token_expiry,
reset_token, reset_token_expiry,
failed_login_attempts, lockout_until,
created_at, updated_at, created_by, updated_by
```
**Enums**: `UserRole {SUPER_ADMIN, PROPERTY_MANAGER, HOST, GUEST, SUPPORT_AGENT}`
**Enums**: `UserStatus {UNVERIFIED, ACTIVE, INACTIVE, DELETED}`

### V002 — properties
```sql
id, host_id (FK users), title, description, property_type (ENUM),
address_line1, address_line2, city, state, country, postal_code,
latitude, longitude,
max_guests, bedrooms, bathrooms, beds,
base_price_per_night (BIGINT paise), cleaning_fee (BIGINT paise),
service_fee_percent (DECIMAL),
booking_mode (ENUM), cancellation_policy (ENUM),
status (ENUM), rejection_reason,
created_at, updated_at, created_by, updated_by
```
**Enums**: `PropertyType {APARTMENT, HOUSE, VILLA, STUDIO, CABIN, HOTEL_ROOM}`
**Enums**: `PropertyStatus {DRAFT, PENDING, ACTIVE, SUSPENDED}`
**Enums**: `BookingMode {INSTANT_BOOK, REQUEST_TO_BOOK}`
**Enums**: `CancellationPolicy {FLEXIBLE, MODERATE, STRICT}`

### V002b — property_photos
```sql
id, property_id (FK), url, caption, display_order, is_cover
```

### V003 — amenities (master)
```sql
id, name (UNIQUE), icon, category
```

### V003b — property_amenities (join table)
```sql
property_id (FK), amenity_id (FK), PRIMARY KEY (property_id, amenity_id)
```

### V003c — property_availability (blocked dates)
```sql
id, property_id (FK), start_date, end_date, reason (ENUM: BLOCKED, BOOKED)
```

### V004 — bookings
```sql
id, property_id (FK), guest_id (FK), host_id (FK),
check_in_date, check_out_date, num_guests,
num_nights, nightly_rate (BIGINT paise), cleaning_fee (BIGINT paise),
platform_fee (BIGINT paise), taxes (BIGINT paise), total_amount (BIGINT paise),
status (ENUM), cancellation_policy (ENUM),
cancellation_reason, cancelled_by (FK users), cancelled_at,
special_requests,
created_at, updated_at, created_by, updated_by
```
**Enums**: `BookingStatus {PENDING, CONFIRMED, COMPLETED, CANCELLED}`

### V005 — reviews
```sql
id, booking_id (FK UNIQUE), property_id (FK), reviewer_id (FK), reviewee_id (FK),
reviewer_type (ENUM: GUEST, HOST),
overall_rating (INT 1-5),
cleanliness_rating, accuracy_rating, checkin_rating,
communication_rating, location_rating, value_rating,
comment, host_response, host_response_at,
is_published, submitted_at,
created_at, updated_at, created_by, updated_by
```

### V006 — messages
```sql
id, booking_id (FK), sender_id (FK), content, is_read, read_at,
created_at, updated_at, created_by, updated_by
```

### V007 — notifications
```sql
id, user_id (FK), title, body, type (ENUM), reference_id, reference_type,
is_read, read_at,
created_at, updated_at, created_by, updated_by
```

### V008 — payments
```sql
id, booking_id (FK UNIQUE), razorpay_order_id, razorpay_payment_id,
razorpay_signature, amount (BIGINT paise), currency,
status (ENUM: CREATED, PAID, FAILED, REFUNDED),
created_at, updated_at, created_by, updated_by
```

### V008b — refunds
```sql
id, payment_id (FK), booking_id (FK), razorpay_refund_id,
amount (BIGINT paise), reason, status (ENUM: INITIATED, PROCESSED, FAILED),
created_at, updated_at, created_by, updated_by
```

### V009 — platform_config
```sql
id, config_key (UNIQUE), config_value, description, updated_by_admin_id (FK),
created_at, updated_at, created_by, updated_by
```

### V010 — host_applications
```sql
id, applicant_id (FK users UNIQUE), status (ENUM: PENDING, APPROVED, REJECTED),
motivation, reviewed_by (FK users), review_notes, reviewed_at,
created_at, updated_at, created_by, updated_by
```

### V011 — audit_logs
```sql
id, actor_id (FK users), action, entity_type, entity_id,
before_state (JSONB), after_state (JSONB), ip_address,
created_at
```

### V012 — seed data
```sql
-- Insert SUPER_ADMIN user (hashed), amenity master list, default platform_config rows
```

---

## API Contract Design

### Base URL: `/api/v1`
### Swagger UI: `/api/v1/docs`

### Standard Response Envelope
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { },
  "timestamp": "2026-06-12T10:00:00Z",
  "path": "/api/v1/properties"
}
```

### Error Response (validation)
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    { "field": "email", "message": "must be a valid email address" }
  ],
  "timestamp": "2026-06-12T10:00:00Z",
  "path": "/api/v1/auth/register"
}
```

---

### Module 1 — Auth (`/api/v1/auth`)

| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/register` | PUBLIC | Register new user |
| POST | `/login` | PUBLIC | Login → JWT + refresh token |
| POST | `/refresh` | PUBLIC | Rotate refresh token |
| POST | `/logout` | Authenticated | Invalidate refresh token |
| GET | `/verify-email` | PUBLIC | Verify email via token `?token=` |
| POST | `/forgot-password` | PUBLIC | Send reset link |
| POST | `/reset-password` | PUBLIC | Reset password via token |

### Module 2 — Users (`/api/v1/users`)

| Method | Path | Role | Description |
|---|---|---|---|
| GET | `/me` | Authenticated | Get own profile |
| PUT | `/me` | Authenticated | Update own profile |
| GET | `/` | ADMIN, SUPPORT | List all users (paginated, filterable) |
| GET | `/{userId}` | ADMIN, SUPPORT | Get user by ID |
| PATCH | `/{userId}/status` | ADMIN | Activate / deactivate user |
| PATCH | `/{userId}/role` | SUPER_ADMIN | Change user role |
| DELETE | `/{userId}` | SUPER_ADMIN | Soft-delete user |

### Module 3 — Host Applications (`/api/v1/host-applications`)

| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/` | GUEST | Submit host application |
| GET | `/` | ADMIN | List all applications (paginated) |
| GET | `/{applicationId}` | ADMIN | Get single application |
| PATCH | `/{applicationId}/approve` | ADMIN | Approve application |
| PATCH | `/{applicationId}/reject` | ADMIN | Reject with justification |

### Module 4 — Properties (`/api/v1/properties`)

| Method | Path | Role | Description |
|---|---|---|---|
| GET | `/` | PUBLIC | Search properties (JpaSpecificationExecutor) |
| GET | `/{propertyId}` | PUBLIC | Get property detail |
| POST | `/` | HOST | Create listing (step 1 — basic info) |
| PUT | `/{propertyId}` | HOST (owner) | Update listing |
| PATCH | `/{propertyId}/submit` | HOST (owner) | Submit listing for review |
| PATCH | `/{propertyId}/photos` | HOST (owner) | Upload photo URLs |
| PATCH | `/{propertyId}/amenities` | HOST (owner) | Update amenities |
| DELETE | `/{propertyId}` | HOST (owner), ADMIN | Delete (soft) listing |
| GET | `/my-listings` | HOST | Get own listings (paginated) |
| GET | `/{propertyId}/availability` | PUBLIC | Get availability calendar |
| POST | `/{propertyId}/availability/block` | HOST (owner) | Block date range |
| DELETE | `/{propertyId}/availability/{blockId}` | HOST (owner) | Unblock dates |

### Module 5 — Listing Moderation (`/api/v1/admin/listings`)

| Method | Path | Role | Description |
|---|---|---|---|
| GET | `/pending` | PROPERTY_MANAGER, SUPER_ADMIN | Moderation queue (paginated) |
| PATCH | `/{propertyId}/approve` | PROPERTY_MANAGER | Approve listing |
| PATCH | `/{propertyId}/reject` | PROPERTY_MANAGER | Reject with reason |
| PATCH | `/{propertyId}/request-changes` | PROPERTY_MANAGER | Request changes |
| PATCH | `/{propertyId}/suspend` | PROPERTY_MANAGER | Suspend active listing |

### Module 6 — Bookings (`/api/v1/bookings`)

| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/` | GUEST | Create booking |
| GET | `/` | ADMIN, SUPPORT | All bookings (paginated, filterable) |
| GET | `/my-trips` | GUEST | Guest's own bookings (paginated) |
| GET | `/host-bookings` | HOST | Bookings for host's properties (paginated) |
| GET | `/{bookingId}` | Authenticated (owner/host/admin) | Get booking detail |
| PATCH | `/{bookingId}/confirm` | HOST | Confirm Request-to-Book |
| PATCH | `/{bookingId}/cancel` | Guest/Host/Admin | Cancel booking |
| PATCH | `/{bookingId}/refund` | ADMIN | Issue refund (full/partial) |

### Module 7 — Amenities (`/api/v1/amenities`)

| Method | Path | Role | Description |
|---|---|---|---|
| GET | `/` | PUBLIC | List all amenities |
| POST | `/` | SUPER_ADMIN | Add amenity |

### Module 8 — Reviews (`/api/v1/reviews`) — Bonus

| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/` | GUEST, HOST | Submit review (post-completed-stay) |
| GET | `/property/{propertyId}` | PUBLIC | Get reviews for a property (paginated) |
| POST | `/{reviewId}/response` | HOST | Post host public response |

### Module 9 — Messages (`/api/v1/messages`) — Bonus

| Method | Path | Role | Description |
|---|---|---|---|
| GET | `/booking/{bookingId}` | Guest/Host (owner) | Get conversation thread |
| POST | `/booking/{bookingId}` | Guest/Host (owner) | Send message |

### Module 10 — Notifications (`/api/v1/notifications`)

| Method | Path | Role | Description |
|---|---|---|---|
| GET | `/` | Authenticated | Get own notifications (paginated) |
| PATCH | `/{notificationId}/read` | Authenticated | Mark as read |
| PATCH | `/read-all` | Authenticated | Mark all as read |

### Module 11 — Admin Analytics (`/api/v1/admin/analytics`) — Bonus

| Method | Path | Role | Description |
|---|---|---|---|
| GET | `/kpis` | ADMIN | KPI card metrics |
| GET | `/revenue/monthly` | ADMIN | Monthly revenue trend |
| GET | `/payouts` | ADMIN | Payout history (paginated) |
| GET | `/payouts/export` | ADMIN | Export CSV |

### Module 12 — Platform Config (`/api/v1/admin/config`) — Super Admin

| Method | Path | Role | Description |
|---|---|---|---|
| GET | `/` | SUPER_ADMIN | List all config entries |
| PUT | `/{configKey}` | SUPER_ADMIN | Update config value |

### Module 13 — Payments (`/api/v1/payments`) — Bonus

| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/orders` | GUEST | Create Razorpay order for booking |
| POST | `/verify` | GUEST | Verify payment signature |
| POST | `/webhook` | PUBLIC (HMAC verified) | Razorpay webhook handler |

---

## Security Architecture

### JWT Flow
```
POST /auth/login
  → Validate credentials
  → Issue accessToken (JWT, 15 min, signed HS256)
  → Issue refreshToken (UUID, 7 days, stored in DB refresh_tokens table)
  → Return both in response body

POST /auth/refresh
  → Validate refreshToken (not expired, not revoked)
  → Issue new accessToken
  → Rotate refreshToken (old one revoked, new one issued)

POST /auth/logout
  → Revoke refreshToken in DB
```

### Security Config Layers
1. **`JwtAuthenticationFilter`** — extracts and validates JWT on every request (extends `OncePerRequestFilter`)
2. **`UserDetailsServiceImpl`** — loads user by email from DB
3. **`SecurityConfig`** — configures endpoint matchers: PUBLIC, authenticated, role-guarded
4. **`@PreAuthorize`** — method-level RBAC on service interfaces
5. **Rate limiting** — custom `LoginAttemptService` tracks failed attempts per email; locks for 15 min after 5 failures

### Public Endpoints (no JWT required)
- `POST /api/v1/auth/**`
- `GET /api/v1/properties` (search)
- `GET /api/v1/properties/{id}` (detail)
- `GET /api/v1/amenities`
- `GET /api/v1/reviews/property/{id}`
- `GET /api/v1/docs/**`, `/swagger-ui/**`, `/v3/api-docs/**`

---

## Core Class Patterns

### BaseEntity (abstract)
```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@SuperBuilder
@NoArgsConstructor
public abstract class BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;
}
```

### ApiResponse<T>
```java
@Builder
@Getter
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private Instant timestamp;
    private String path;

    public static <T> ApiResponse<T> success(T data, String message, String path) { ... }
    public static <T> ApiResponse<T> error(String message, String path) { ... }
}
```

### GlobalExceptionHandler
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class) → 404
    @ExceptionHandler(BusinessRuleException.class)     → 409
    @ExceptionHandler(AccessDeniedException.class)     → 403
    @ExceptionHandler(MethodArgumentNotValidException.class) → 400 (with field errors)
    @ExceptionHandler(Exception.class)                 → 500
}
```

### Exception Hierarchy
```
ApplicationException (base, RuntimeException)
├── ResourceNotFoundException     (404)
├── BusinessRuleException         (409)
├── ValidationException           (400)
├── AuthenticationException       (401)
├── AuthorizationException        (403)
└── PaymentException              (402)
```

### Service Pattern Example
```java
// Interface
public interface PropertyService {
    PropertyResponse createListing(CreatePropertyRequest request, UUID hostId);
    Page<PropertySummaryResponse> searchProperties(PropertySearchRequest request, Pageable pageable);
    // ...
}

// Impl
@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyServiceImpl implements PropertyService {
    private final PropertyRepository propertyRepository;
    private final PropertyFactory propertyFactory;
    private final PropertyMapper propertyMapper;
    private final UserRepository userRepository;
    // ...
}
```

### Factory Pattern Example
```java
@Component
@RequiredArgsConstructor
public class PropertyFactory {
    public Property createDraft(CreatePropertyRequest request, User host) {
        return Property.builder()
            .host(host)
            .title(request.getTitle())
            .status(PropertyStatus.DRAFT)
            // ...
            .build();
    }
}
```

### Repository with JpaSpecificationExecutor (Search only)
```java
public interface PropertyRepository extends JpaRepository<Property, UUID>,
        JpaSpecificationExecutor<Property> {
    // Simple dynamic query methods
    List<Property> findByHostIdAndStatus(UUID hostId, PropertyStatus status);
    boolean existsByIdAndHostId(UUID id, UUID hostId);
}

// PropertySpecification (for Search API only)
public class PropertySpecification {
    public static Specification<Property> hasLocation(String city) { ... }
    public static Specification<Property> isAvailableForDates(LocalDate checkIn, LocalDate checkOut) { ... }
    public static Specification<Property> hasPriceBetween(Long min, Long max) { ... }
    public static Specification<Property> hasAmenities(List<UUID> amenityIds) { ... }
    public static Specification<Property> isActive() { ... }
}
```

---

## Liquibase Strategy

- **Master file**: `db.changelog-master.xml` uses `<include file="...">` to include each `.sql` migration in order.
- **Naming**: `V{NNN}__{description}.sql` (e.g., `V001__create_users.sql`)
- **Each migration file** is a standard SQL file with `--changeset author:id` Liquibase markers.
- **`ddl-auto`**: set to `validate` in `application-prod.yml`; `none` in all other profiles.
- No Flyway. No Hibernate DDL generation.

Example migration structure:
```sql
-- liquibase formatted sql
-- changeset staynest:V001-create-users
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    ...
);
-- rollback DROP TABLE users;
```

---

## Docker Compose Design

```yaml
version: '3.9'
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: ${POSTGRES_DB}
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    ports: ["5432:5432"]
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck: pg_isready

  api:
    build: ./services/api
    ports: ["8080:8080"]
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB}
      SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER}
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      JWT_ACCESS_EXPIRY_MS: 900000
      JWT_REFRESH_EXPIRY_MS: 604800000
      MAIL_HOST: ${MAIL_HOST}
      MAIL_PORT: ${MAIL_PORT}
      MAIL_USERNAME: ${MAIL_USERNAME}
      MAIL_PASSWORD: ${MAIL_PASSWORD}
      RAZORPAY_KEY_ID: ${RAZORPAY_KEY_ID}
      RAZORPAY_KEY_SECRET: ${RAZORPAY_KEY_SECRET}

volumes:
  postgres_data:
```

---

## Maven `pom.xml` — Key Dependencies

```xml
<!-- Spring Boot 4 Parent -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.x.x</version>
</parent>

<!-- Core -->
spring-boot-starter-web
spring-boot-starter-data-jpa
spring-boot-starter-security
spring-boot-starter-validation
spring-boot-starter-mail

<!-- Database -->
postgresql (runtime)
liquibase-core

<!-- JWT -->
jjwt-api, jjwt-impl, jjwt-jackson

<!-- Mapping & Boilerplate -->
lombok (provided)
mapstruct + mapstruct-processor

<!-- API Docs -->
springdoc-openapi-starter-webmvc-ui

<!-- Testing -->
spring-boot-starter-test (JUnit 5 + Mockito)

<!-- Bonus -->
spring-boot-starter-data-redis     (Redis cache)
spring-boot-starter-websocket      (WebSocket/STOMP)
razorpay-java                      (Razorpay SDK)
```

---

## Frontend Architecture (React 19)

Both `apps/customer` and `apps/admin` share the same technical stack:

```
Routing:        TanStack Router (file-based routes)
Server State:   TanStack Query (auto-caching, background refetch)
Forms:          React Hook Form + Zod (schema validation)
HTTP:           Native Fetch API with JWT interceptor (auto-refresh on 401)
Styling:        Tailwind CSS
Map:            Leaflet + React-Leaflet (customer app only)
Build:          Vite
```

### JWT Interceptor Pattern (Fetch wrapper)
```typescript
async function apiFetch(url: string, options: RequestInit = {}) {
  const token = getAccessToken();
  const response = await fetch(url, {
    ...options,
    headers: { Authorization: `Bearer ${token}`, ...options.headers }
  });
  if (response.status === 401) {
    const newToken = await refreshAccessToken();
    return fetch(url, { ...options, headers: { Authorization: `Bearer ${newToken}` } });
  }
  return response;
}
```

---

## Implementation Phases

### Phase 1 — Foundation (Backend)
- Project setup: Spring Boot 4, Maven, Dockerfile, Docker Compose
- `BaseEntity`, `ApiResponse<T>`, exception hierarchy, `GlobalExceptionHandler`
- Security: JWT filter, `SecurityConfig`, `UserDetailsServiceImpl`
- Liquibase: master file + V001 (users) migration
- `AuthController`, `AuthService`, `UserRepository`

### Phase 2 — Property Listings
- V002, V003 migrations (properties, amenities, photos, availability)
- `Property`, `Amenity`, `PropertyPhoto`, `PropertyAvailability` entities
- `PropertyFactory`, `PropertyService` / `PropertyServiceImpl`
- `PropertyRepository` with `JpaSpecificationExecutor` for search
- `PropertySpecification` for dynamic search predicates
- `PropertyController` — CRUD + search + submit-for-review
- `AmenityController`

### Phase 3 — Admin Moderation
- `ListingModerationController` — approve / reject / request-changes / suspend
- `AdminListingService` / `AdminListingServiceImpl`
- Email notification on status transitions (Spring Mail)
- Host Application — `HostApplicationController`, `HostApplicationServiceImpl`

### Phase 4 — Booking
- V004 migration (bookings, availability blocks)
- `Booking` entity, `BookingFactory`, `BookingService` / `BookingServiceImpl`
- Price calculation engine (nightly × nights + cleaning + platform fee + tax)
- Availability conflict detection (optimistic locking)
- `BookingController` — create / confirm / cancel
- `AdminBookingController` — view all / filter / cancel on behalf / refund

### Phase 5 — User Management & Notifications
- V007 migration (notifications)
- `NotificationService`, `NotificationController`
- Admin User Management: `AdminUserController`, `AdminUserServiceImpl`

### Phase 6 — Frontend (Customer App)
- Auth pages (Register, Login, Verify Email, Forgot/Reset Password)
- Search page with filters, map view (Leaflet), pagination
- Property detail page + availability calendar
- Booking flow page (price breakdown + confirm)
- My Trips page

### Phase 7 — Frontend (Admin/Host Portal)
- Host dashboard (KPIs, upcoming check-ins, pending requests)
- Multi-step listing creation form (6 steps)
- Host calendar management
- Admin: Moderation queue, User management, Booking management

### Phase 8 — Bonus Features
- Razorpay payment integration (Orders API, webhook, refunds)
- Reviews (double-blind, 6 sub-categories, host response)
- In-app messaging (per-booking threads)
- Real-time via STOMP/SockJS (WebSockets)
- Redis cache for listing search results
- Admin Analytics (KPI cards, revenue chart, CSV export)
- Platform Configuration (Super Admin)
- CI/CD (GitHub Actions)

---

## Verification Plan

### Automated Tests
- `mvn test` — JUnit 5 + Mockito service tests (70%+ coverage target)
- Test classes: `AuthServiceImplTest`, `PropertyServiceImplTest`, `BookingServiceImplTest`, `AdminListingServiceImplTest`, `AdminUserServiceImplTest`, `NotificationServiceImplTest`

### Manual Verification
1. `docker-compose up --build` → API healthy at `http://localhost:8080/api/v1/docs`
2. Register → verify email → login → receive JWT pair
3. Create property listing (all 6 steps) → submit → approve via admin → visible in search
4. Search properties (filters + pagination) → view detail → book → confirm
5. Admin: User management, booking cancellation, refund
6. All endpoints return `ApiResponse<T>` envelope
7. Unauthenticated requests to secured endpoints → 401; wrong role → 403
8. Liquibase migrations applied on startup (check `databasechangelog` table)

**Version**: 1.0.0 | **Date**: 2026-06-12
