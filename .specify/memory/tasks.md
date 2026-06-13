# Tasks: StayNest — Property Management Platform

**Input**: `.specify/memory/plan.md` + `.specify/memory/spec.md` + `.specify/memory/constitution.md`
**Date**: 2026-06-13 | **Status**: Near Complete — bonus modules & E2E verification pending

## Format: `[ID] [P?] [Story] Description`
- **[P]**: Can run in parallel (different files, no dependencies)
- **[USn]**: User Story reference from spec.md

---

## Phase 0: Project Scaffold & Monorepo Setup

- [x] T001 Create monorepo root directory structure
- [x] T002 Initialize Spring Boot 4 Maven project in `services/api/`
- [x] T003 [P] Configure `pom.xml` with all required dependencies
- [x] T004 [P] Create `application.yml`, `application-dev.yml`
- [x] T005 [P] Create `Dockerfile` for `services/api/`
- [x] T006 Create `docker-compose.yml` at repo root
- [x] T007 [P] Create `.env.example`
- [x] T008 [P] Initialize `apps/customer/` as Vite + React 19 app
- [x] T009 [P] Initialize `apps/admin/` as Vite + React 19 app
- [x] T010 Write `README.md`

---

## Phase 1: Backend Foundation

### 1A — Package Skeleton & Cross-Cutting Utilities

- [x] T011 Create Java package structure
- [x] T012 [P] Create `BaseEntity.java`
- [x] T013 [P] Create `ApiResponse<T>.java`
- [x] T014 [P] Create `ApiErrorResponse.java`
- [x] T015 [P] Create exception hierarchy
- [x] T016 Create `GlobalExceptionHandler.java`
- [x] T017 [P] Create `JpaConfig.java`
- [x] T018 [P] Create `OpenApiConfig.java`
- [x] T019 [P] Create `CorsConfig.java`

### 1B — Liquibase Master Setup

- [x] T020 Create `db.changelog-master.xml`
- [x] T021 [P] Create `V001__create_users.sql`
- [x] T022 [P] Create all enum classes in `enums/`

### 1C — Security Foundation

- [x] T023 Create `User.java` entity
- [x] T024 Create `RefreshToken.java` entity
- [x] T025 Create `UserRepository.java`
- [x] T026 Create `RefreshTokenRepository.java`
- [x] T027 Create `UserDetailsServiceImpl.java`
- [x] T028 Create `JwtService.java`
- [x] T029 Create `JwtAuthenticationFilter.java`
- [x] T030 Create `SecurityConfig.java`

---

## Phase 2: Auth Module (US1)

- [x] T031 [P] [US1] Create `AuthServiceImplTest.java`
- [x] T032 [P] [US1] Create `RegisterRequest.java`
- [x] T033 [P] [US1] Create `LoginRequest.java`
- [x] T034 [P] [US1] Create `RefreshTokenRequest.java`, `ForgotPasswordRequest.java`, `ResetPasswordRequest.java`
- [x] T035 [P] [US1] Create `AuthResponse.java`
- [x] T036 [P] [US1] Create `UserResponse.java`
- [x] T037 [P] [US1] Create `UserMapper.java`
- [x] T038 [P] [US1] Create `UserFactory.java`
- [x] T039 [P] [US1] Create `LoginAttemptService.java` + `LoginAttemptServiceImpl.java`
- [x] T040 [P] [US1] Create `EmailService.java` + `EmailServiceImpl.java`
- [x] T041 [US1] Create `AuthService.java` + `AuthServiceImpl.java`
- [x] T042 [US1] Create `AuthController.java`
- [x] T043 [US1] Add V001 seed in `V012__seed_data.sql`

---

## Phase 3: Property Listings (US2)

- [x] T044 [P] [US2] Create `PropertyServiceImplTest.java`
- [x] T045 [P] [US2] Create `V002__create_properties.sql`
- [x] T046 [P] [US2] Create `V003__create_amenities.sql`
- [x] T047 [P] [US2] Create `Property.java` entity
- [x] T048 [P] [US2] Create `PropertyPhoto.java` entity
- [x] T049 [P] [US2] Create `Amenity.java` entity
- [x] T050 [P] [US2] Create `PropertyAvailability.java` entity
- [x] T051 [P] [US2] Create `PropertyRepository.java`
- [x] T052 [P] [US2] Create `AmenityRepository.java`, `PropertyPhotoRepository.java`, `PropertyAvailabilityRepository.java`
- [x] T053 [P] [US2] Create request DTOs: `CreatePropertyRequest.java`, `UpdatePropertyRequest.java`, `BlockAvailabilityRequest.java`
- [x] T054 [P] [US2] Create response DTOs: `PropertyResponse.java`, `PropertySummaryResponse.java`, `PropertyAvailabilityResponse.java`, `AmenityResponse.java`
- [x] T055 [P] [US2] Create `PropertyMapper.java`
- [x] T056 [P] [US2] Create `PropertyFactory.java`
- [x] T057 [US2] Create `PropertyService.java` + `PropertyServiceImpl.java`
- [x] T058 [P] [US2] Create `AmenityService.java` + `AmenityServiceImpl.java`
- [x] T059 [US2] Create `PropertyController.java`
- [x] T060 [P] [US2] Create `AmenityController.java`

---

## Phase 4: Property Search (US3)

- [x] T061 [P] [US3] Create `PropertySearchServiceImplTest.java`
- [x] T062 [P] [US3] Create `PropertySearchRequest.java`
- [x] T063 [US3] Create `PropertySpecification.java`
- [x] T064 [US3] Add `searchProperties` to `PropertyServiceImpl`
- [x] T065 [US3] Add `GET /api/v1/properties` endpoint
- [x] T066 [P] [US3] Add `GET /api/v1/properties/{propertyId}` endpoint

---

## Phase 5: Booking Module (US4)

- [x] T067 [P] [US4] Create `BookingServiceImplTest.java`
- [x] T068 [P] [US4] Create `V004__create_bookings.sql`
- [x] T069 [P] [US4] Create `Booking.java` entity
- [x] T070 [P] [US4] Create `BookingRepository.java`
- [x] T071 [P] [US4] Create `CreateBookingRequest.java`
- [x] T072 [P] [US4] Create `BookingResponse.java`
- [x] T073 [P] [US4] Create `BookingMapper.java`
- [x] T074 [P] [US4] Create `PriceCalculationService.java` + `PriceCalculationServiceImpl.java`
- [x] T075 [P] [US4] Create `BookingFactory.java`
- [x] T076 [US4] Create `BookingService.java` + `BookingServiceImpl.java`
- [x] T077 [US4] Create `BookingController.java`
- [x] T078 [P] [US4] Create `AdminBookingController.java`

---

## Phase 6: Listing Moderation & Admin (US5, US6, US9)

- [x] T079 [US5] Create `AdminListingController.java` + `AdminListingServiceImpl.java`
- [x] T080 [US6] Create `AdminUserController.java` + `AdminUserServiceImpl.java`
- [x] T081 [US9] Create `HostApplicationController.java` + `HostApplicationServiceImpl.java`
- [x] T082 [US7] Create `NotificationController.java` + `NotificationServiceImpl.java`
- [x] T083 Create `UserController.java` + `UserServiceImpl.java`
- [x] T084 [P] Create `AdminListingServiceImplTest.java`
- [x] T085 [P] Create `AdminUserServiceImplTest.java`
- [x] T086 [P] Create `NotificationServiceImplTest.java`
- [x] T087 Create `AuditLog` entity + service (V011 migration exists)

---

## Phase 7: Host Dashboard, Notifications, User Profile (US7)

- [x] T094 Notification module (entity, service, controller)
- [x] T095 User profile endpoints (`GET/PUT /api/v1/users/me`)
- [x] T096 Host dashboard KPI aggregation endpoint (host-scoped)
- [x] T097 Host calendar UI (admin portal multi-step listing form)

---

## Phase 8: Platform Config & Analytics (US10, US11)

- [x] T102 [US11] Create `PlatformConfig` entity + repository + service + `AdminPlatformConfigController`
- [x] T103 [US10] Create `AdminAnalyticsService` + `AdminAnalyticsController` (KPIs, monthly revenue, payouts CSV)
- [x] T104 [P] Create `AdminAnalyticsServiceImplTest.java`
- [x] T105 [P] Create `PlatformConfigServiceImplTest.java`

---

## Phase 9: Customer Frontend (React 19)

- [x] T108 TanStack Query provider wired in `main.tsx`
- [x] T109 JWT interceptor (`apiFetch.ts`) + token storage
- [x] T110 Login page (React Hook Form + Zod)
- [x] T111 Register page
- [x] T112 Search page with filters + pagination
- [x] T113 Property detail page + booking flow
- [x] T114 My Trips page
- [x] T115 Leaflet map view on search results
- [x] T116 Forgot/reset password pages
- [x] T117 Email verification landing page

---

## Phase 10: Admin Frontend (React 19)

- [x] T118 Admin shell layout + sidebar navigation
- [x] T119 Admin login page
- [x] T120 Dashboard with live KPI cards
- [x] T121 Listings moderation queue (approve/reject)
- [x] T122 Bookings management table
- [x] T123 Users management list
- [x] T124 Host applications review
- [x] T125 Platform config editor
- [x] T126 Multi-step host listing creation form
- [x] T127 Host calendar management UI
- [x] T128 Revenue chart (monthly trend visualization)

---

## Phases 11-14: Bonus & Polish

- [ ] T133 Reviews module (V005 migration exists — no Java entities yet)
- [ ] T134 Messages module (V006 migration exists)
- [ ] T135 Payments / Razorpay integration (V008 migration exists)
- [ ] T136 Redis cache for search results
- [ ] T137 WebSocket/STOMP real-time notifications
- [ ] T138 GitHub Actions CI/CD pipeline
- [x] T139 `.dockerignore` created at repo root
- [ ] T140 Run `mvn test` — 70%+ service coverage gate
- [ ] T141 Run `docker-compose up --build` end-to-end verification
