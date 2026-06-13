# Feature Specification: StayNest — Property Management Platform

**Project**: StayNest  
**Feature Branch**: `main`  
**Created**: 2026-06-12  
**Status**: Draft  
**Assignment**: Intern Assignment #2 — Full-Stack Airbnb-Style Property Rental Platform

---

## Overview

StayNest is a full-stack Airbnb-style property rental platform with two surfaces:

1. **Customer Web App** — Guests search, discover, book properties, manage trips, and leave reviews.
2. **Admin / Host Portal** — Platform staff and hosts manage listings, bookings, users, moderation, revenue, and platform configuration.

A single **Spring Boot 4 REST API** serves both frontends. The backend is delivered as a Docker Compose stack (API + PostgreSQL). Both React 19 frontends run locally via `npm run dev`.

---

## User Roles

| Role | Description |
|---|---|
| **Super Admin** | Full platform access including configuration (fees, policies, feature flags) |
| **Property Manager** | Reviews and moderates property listings; manages the approval queue |
| **Host** | Creates and manages property listings; reviews booking requests; sees earnings |
| **Guest** | Searches and books properties; manages trips; writes reviews |
| **Support Agent** | Views user history; manages bookings on behalf of users; handles escalations |

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Guest Registration & Authentication (Priority: P1)

A new user lands on the StayNest customer app and creates an account. They register with email and password, receive an email verification link, confirm their account, and log in. The system issues a short-lived JWT access token and a long-lived refresh token. If the user forgets their password they can trigger a reset via email.

**Why this priority**: Authentication is the gateway to all other user journeys. Nothing else works without it.

**Independent Test**: A new email + password can register, verify email, log in, receive a token pair, and call an authenticated endpoint.

**Acceptance Scenarios**:

1. **Given** a visitor on the registration page, **When** they submit a valid email + password + name, **Then** a verification email is sent and the account is created in `UNVERIFIED` status.
2. **Given** an unverified account, **When** the user clicks the verification link (valid for 24 h), **Then** the account transitions to `ACTIVE` and the user may log in.
3. **Given** valid credentials, **When** the user submits login, **Then** a JWT access token (15 min TTL) and a refresh token (7 days TTL) are returned.
4. **Given** an expired access token, **When** the client sends the refresh token, **Then** a new access token is issued without re-login.
5. **Given** 5 failed login attempts, **When** a 6th attempt is made, **Then** the account is rate-limited for 15 minutes and an appropriate error is returned.
6. **Given** a user clicks "Forgot Password", **When** they submit their registered email, **Then** a one-time reset link (valid 1 h) is sent and the old password is invalidated after successful reset.

---

### User Story 2 — Host: Create a Property Listing (Priority: P1)

A verified Host logs in to the Admin/Host Portal and creates a new property listing using a multi-step form: Basic Info → Location → Amenities → Photos → Pricing → Availability Calendar. The listing is saved as a `DRAFT` until the Host submits it for review, at which point it moves to `PENDING`.

**Why this priority**: Listings are the core supply of the platform. No listings = no bookings.

**Independent Test**: A Host can complete all 6 steps of the listing form, submit for review, and see the listing in `PENDING` status in their dashboard.

**Acceptance Scenarios**:

1. **Given** a Host is on the listing creation form, **When** they complete Step 1 (title, description, type, max guests), **Then** progress is saved and they can navigate to Step 2.
2. **Given** a Host completes all steps, **When** they save as Draft, **Then** the listing is stored with status `DRAFT` and is not visible to Guests.
3. **Given** a listing in `DRAFT`, **When** the Host clicks "Submit for Review", **Then** the listing transitions to `PENDING` and appears in the Property Manager's moderation queue.
4. **Given** a listing in `PENDING`, **When** a Property Manager approves it, **Then** status transitions to `ACTIVE` and the listing becomes searchable by Guests.
5. **Given** a listing in `PENDING`, **When** a Property Manager rejects it with a reason, **Then** the Host receives an email with the rejection reason and the listing returns to `DRAFT`.
6. **Given** a listing in `ACTIVE`, **When** a Property Manager suspends it, **Then** status transitions to `SUSPENDED` and it is removed from search results.

**Listing Status Lifecycle**: `DRAFT` → `PENDING` → `ACTIVE` → `SUSPENDED`

---

### User Story 3 — Guest: Search & Discover Properties (Priority: P1)

A Guest searches for available properties by entering a location, dates, and number of guests. The results page shows a filtered, paginated list of `ACTIVE` listings with map view (Leaflet). The Guest can apply additional filters (price range, amenities, property type) and sort results.

**Why this priority**: Discovery is the entry point of the entire Guest booking journey.

**Independent Test**: A Guest can search by location + dates + guests, see paginated results, apply filters, and click a listing to view its full detail page.

**Acceptance Scenarios**:

1. **Given** a Guest enters location, check-in/check-out dates, and guest count, **When** they submit the search, **Then** only `ACTIVE` listings available for those dates are returned, paginated (default 12 per page).
2. **Given** search results are displayed, **When** the Guest applies a price range filter ($50–$200/night), **Then** only listings within that range are shown.
3. **Given** search results are displayed, **When** the Guest selects amenity filters (WiFi, Pool, Parking), **Then** only listings with all selected amenities are shown.
4. **Given** search results are displayed, **When** the Guest selects "Sort by: Price Low to High", **Then** results are reordered accordingly.
5. **Given** the Guest clicks a listing card, **When** the detail page loads, **Then** it shows full photos, description, amenities, host info, availability calendar, pricing breakdown, and a Book Now CTA.
6. **Given** a search with no results, **When** returned, **Then** a friendly empty-state message with suggestions is displayed (no blank page).

---

### User Story 4 — Guest: Book a Property (Priority: P1)

A Guest selects dates on a listing's detail page and initiates a booking. The system shows an itemised price breakdown (nightly rate × nights, platform fee, taxes). Listings support either **Instant Book** or **Request to Book**. The booking lifecycle is: `PENDING` → `CONFIRMED` → `COMPLETED`.

**Why this priority**: Booking is the core monetisation action of the platform.

**Independent Test**: A Guest can select dates, see a price breakdown, confirm a booking, and see it in their "My Trips" section with correct status.

**Acceptance Scenarios**:

1. **Given** a Guest selects check-in/check-out dates, **When** the Guest clicks "Reserve", **Then** an itemised breakdown is shown: `nightly_rate × nights + platform_fee + taxes = total`.
2. **Given** an Instant Book listing, **When** the Guest confirms, **Then** the booking immediately moves to `CONFIRMED` and both Guest and Host receive a confirmation notification.
3. **Given** a Request to Book listing, **When** the Guest submits a request, **Then** the booking is created in `PENDING` and the Host has 24 h to accept or decline.
4. **Given** a Host accepts a booking request, **Then** the booking transitions to `CONFIRMED` and the Guest is notified.
5. **Given** a confirmed booking, **When** the check-out date passes, **Then** the system automatically transitions the booking to `COMPLETED`.
6. **Given** a Guest tries to book dates that are already booked, **Then** the system returns a conflict error and suggests alternative dates.
7. **Given** a booking is `CONFIRMED`, **When** the Guest cancels, **Then** the cancellation policy (Flexible / Moderate / Strict) determines the refund amount.

---

### User Story 5 — Admin: Listing Moderation Queue (Priority: P2)

A Property Manager logs in to the Admin Portal and sees all listings with `PENDING` status. They can review each listing's details, photos, and pricing, then take one of four actions: **Approve**, **Reject** (with reason), **Request Changes**, or **Suspend** an active listing.

**Why this priority**: Without moderation, unsafe or fraudulent listings could go live.

**Independent Test**: A Property Manager can open the moderation queue, review a pending listing, and approve/reject it with the listing status updating accordingly.

**Acceptance Scenarios**:

1. **Given** a Property Manager opens the moderation queue, **When** the page loads, **Then** all `PENDING` listings are shown, sorted by submission date (oldest first), with pagination.
2. **Given** the Property Manager clicks "Approve", **Then** the listing status transitions to `ACTIVE`, the Host is notified by email, and the listing is removed from the queue.
3. **Given** the Property Manager clicks "Reject" and enters a reason, **Then** the listing status returns to `DRAFT`, the Host receives an email with the reason.
4. **Given** the Property Manager clicks "Request Changes" with a comment, **Then** the Host is notified and can edit and re-submit the listing.
5. **Given** an `ACTIVE` listing, **When** the Property Manager clicks "Suspend", **Then** status becomes `SUSPENDED`, the listing disappears from search results, and any `PENDING` bookings are automatically cancelled.

---

### User Story 6 — Admin: User Management (Priority: P2)

A Super Admin or Support Agent searches for registered users, views their booking/review history, and can activate, deactivate, change roles, or soft-delete accounts.

**Why this priority**: Platform safety requires the ability to manage user accounts and respond to violations.

**Independent Test**: A Super Admin can search for a user by name/email, view their profile + booking history, and deactivate their account.

**Acceptance Scenarios**:

1. **Given** an admin searches for a user by name or email, **Then** a paginated list of matching users is returned.
2. **Given** an admin views a user's profile, **Then** they see booking history, review history, account status, and role.
3. **Given** an admin deactivates an account, **Then** the user cannot log in and receives a notification email.
4. **Given** an admin changes a user's role from `GUEST` to `HOST`, **Then** the user gains host capabilities on next login.
5. **Given** an admin soft-deletes a user, **Then** their data is retained (for audit) but the account is inaccessible. Hard-delete is not supported.

---

### User Story 7 — Host: Dashboard & Calendar Management (Priority: P2)

A Host logs in and sees their dashboard: upcoming check-ins, pending booking requests, monthly earnings summary, and quick links to their listings. They can block dates on their availability calendar.

**Why this priority**: Hosts need operational visibility to run their listings effectively.

**Independent Test**: A Host can view their dashboard with correct booking counts, see their listings, and block a date range on the availability calendar.

**Acceptance Scenarios**:

1. **Given** a Host's dashboard loads, **Then** they see: count of active listings, upcoming check-ins (next 7 days), pending booking requests, and earnings this month.
2. **Given** a Host opens a listing's calendar, **When** they select a date range and mark as Blocked, **Then** those dates are unavailable to Guests in search results.
3. **Given** a Host has a new booking request, **Then** a notification appears in the notification bell and via email.

---

### User Story 8 — Admin: Booking Management (Priority: P2)

Admins (Support Agents, Super Admins) can view all bookings, filter by status/date/property, cancel on behalf of either party, and issue full or partial refunds.

**Why this priority**: Support agents need tools to resolve disputes and assist users.

**Acceptance Scenarios**:

1. **Given** an admin opens Booking Management, **Then** all bookings are shown paginated, filterable by `status`, `date range`, and `property`.
2. **Given** an admin cancels a booking on behalf of a Guest, **Then** the booking moves to `CANCELLED`, refund is calculated per policy, and both parties are notified.
3. **Given** an admin issues a partial refund, **Then** the refund amount is recorded and the payment status updated accordingly.

---

### User Story 9 — Admin: Host Application Review (Priority: P2)

A user can apply to become a Host. Super Admins and Property Managers review these applications, approve or reject them with written justification sent via email.

**Acceptance Scenarios**:

1. **Given** a Guest submits a Host application, **Then** it appears in the Host Application Review queue.
2. **Given** an admin approves the application, **Then** the user's role changes to `HOST`, they receive a confirmation email, and can access the Host Portal.
3. **Given** an admin rejects with justification, **Then** the user receives an email with the reason and their role remains `GUEST`.

---

### User Story 10 — Admin: Revenue & Analytics Dashboard (Priority: P3)

The Admin Portal shows KPI cards (active listings, bookings this month, platform fee revenue), a monthly revenue trend chart, payout history table, and a CSV export button.

**Acceptance Scenarios**:

1. **Given** an admin opens the Revenue dashboard, **Then** KPI cards show real-time counts for active listings, bookings this month, and platform fee collected.
2. **Given** the admin selects a date range, **Then** the revenue trend chart updates to show monthly revenue for that period.
3. **Given** the admin clicks "Export CSV", **Then** a CSV file downloads with payout history data.

---

### User Story 11 — Super Admin: Platform Configuration (Priority: P3)

Super Admins can configure platform-wide settings: service fee %, payout delay days, tax rates per region, cancellation policy terms, and feature flag toggles.

**Acceptance Scenarios**:

1. **Given** a Super Admin opens Platform Configuration, **When** they update the service fee from 10% to 12%, **Then** new bookings use the updated fee immediately.
2. **Given** a Super Admin toggles a feature flag off, **Then** the corresponding feature is hidden from all users immediately.
3. **Given** any other role tries to access Platform Configuration, **Then** a 403 Forbidden response is returned.

---

### User Story 12 — Bonus: Payments via Razorpay (Priority: P4)

On booking confirmation, a Razorpay order is created for the Guest. The Guest completes payment on the checkout page. Razorpay sends a webhook confirming payment, which triggers booking confirmation. Refunds are issued through Razorpay per cancellation policy.

**Acceptance Scenarios**:

1. **Given** a Guest confirms a booking, **Then** a Razorpay order is created and the Guest is redirected to the payment page.
2. **Given** Razorpay sends a payment success webhook, **Then** the booking transitions to `CONFIRMED`.
3. **Given** a cancellation with a Flexible policy within the free-cancel window, **Then** a full refund is initiated via Razorpay Refunds API.

---

### User Story 13 — Bonus: Messaging & Notifications (Priority: P4)

Each booking has a conversation thread between Guest and Host. Key events (booking confirmed, listing approved, payment received) trigger in-app bell notifications and emails.

**Acceptance Scenarios**:

1. **Given** a Guest sends a message in a booking thread, **Then** the Host sees it in the in-app notification bell and receives an email alert.
2. **Given** any key event (booking confirmed, listing approved), **Then** all involved parties receive an in-app notification.

---

### User Story 14 — Bonus: Reviews (Priority: P4)

After a stay is `COMPLETED`, both Guest and Host can leave a double-blind review (neither sees the other's review until both have submitted or the 14-day window closes). Guest reviews rate 6 sub-categories. Hosts can post a public response to a Guest review.

**Acceptance Scenarios**:

1. **Given** a booking transitions to `COMPLETED`, **Then** both Guest and Host receive an invitation to review within 14 days.
2. **Given** both parties submit reviews, **Then** both reviews are published simultaneously (double-blind).
3. **Given** only one party submits within 14 days, **Then** only their review is published after the window closes.
4. **Given** a Host submits a public response to a Guest review, **Then** the response appears beneath the Guest's review on the listing page.

---

### Edge Cases

- What happens when a Guest tries to book a property they already have an active booking for?
- What happens when a Host's account is deactivated while they have `CONFIRMED` bookings?
- What happens when two Guests simultaneously book the last available slot?
- What happens if a Razorpay webhook arrives after a booking is already cancelled?
- What if a user attempts to verify their email with an expired token?
- What if a refresh token is replayed after it has already been used or invalidated (token rotation)?
- What if the platform fee configuration changes mid-booking-flow?
- What happens to `PENDING` bookings when a listing is `SUSPENDED`?

---

## Requirements *(mandatory)*

### Functional Requirements

**Authentication & Security**
- **FR-001**: System MUST support email/password registration with email verification before login is permitted.
- **FR-002**: System MUST issue JWT access tokens (15 min TTL) and refresh tokens (7 day TTL) on successful login.
- **FR-003**: System MUST enforce role-based access control (Super Admin, Property Manager, Host, Guest, Support Agent).
- **FR-004**: System MUST rate-limit login to 5 attempts per 15-minute window per account/IP.
- **FR-005**: System MUST support forgot-password flow via one-time email link (1 h TTL).
- **FR-006**: System MUST NOT store secrets in source code; all secrets via environment variables.
- **FR-007**: System MUST use BCrypt with cost factor 12 for password hashing.
- **FR-008**: System MUST lock CORS to configured frontend origins only.

**Property Listings**
- **FR-009**: Hosts MUST be able to create listings via a 6-step form: Basic Info, Location, Amenities, Photos, Pricing, Availability.
- **FR-010**: Listing status lifecycle MUST follow: `DRAFT` → `PENDING` → `ACTIVE` → `SUSPENDED`.
- **FR-011**: Only `ACTIVE` listings MUST be visible in Guest search results.
- **FR-012**: Property Managers MUST be able to Approve, Reject (with reason), Request Changes, or Suspend any listing.
- **FR-013**: Email notifications MUST be sent to Hosts on listing status changes.
- **FR-014**: Hosts MUST be able to manage availability via a calendar (block/unblock date ranges).

**Search & Discovery**
- **FR-015**: System MUST support search by location, check-in/check-out dates, and guest count.
- **FR-016**: System MUST support filtering by price range, amenities (multi-select), and property type.
- **FR-017**: System MUST support sorting by price (asc/desc), rating, and newest.
- **FR-018**: All search results MUST be paginated (configurable page size, default 12).
- **FR-019**: Search results MUST exclude listings with conflicting confirmed bookings for requested dates.

**Booking**
- **FR-020**: System MUST support two booking modes: Instant Book (auto-confirm) and Request to Book (host approval).
- **FR-021**: Every booking MUST display an itemised price breakdown: nightly rate, platform fee, taxes, total.
- **FR-022**: Booking status lifecycle MUST follow: `PENDING` → `CONFIRMED` → `COMPLETED` | `CANCELLED`.
- **FR-023**: System MUST prevent double-booking via optimistic locking or serialised availability checks.
- **FR-024**: System MUST apply cancellation policies (Flexible, Moderate, Strict) on cancellation refunds.
- **FR-025**: Completed stays MUST automatically transition to `COMPLETED` status after check-out date.

**Admin Operations**
- **FR-026**: Admins MUST be able to search and filter all users by name, email, role, and status.
- **FR-027**: Admins MUST be able to activate, deactivate, change roles, and soft-delete user accounts.
- **FR-028**: Admins MUST be able to view, filter, and cancel any booking.
- **FR-029**: Admins MUST be able to issue full or partial refunds on bookings.
- **FR-030**: Super Admins MUST be able to configure platform settings (service fee %, tax rates, payout delay, cancellation policies, feature flags).
- **FR-031**: Platform configuration changes MUST take effect immediately for new bookings.

**API & Documentation**
- **FR-032**: All endpoints MUST be documented in OpenAPI 3 / Swagger UI accessible at `/api/v1/docs`.
- **FR-033**: All list endpoints MUST support pagination via `page`, `size`, `sort` query parameters.
- **FR-034**: All API responses MUST use the standard `ApiResponse<T>` wrapper.
- **FR-035**: All validation errors MUST return structured error responses with field-level detail.

### Key Entities

- **User**: Platform account with role, status (`UNVERIFIED`, `ACTIVE`, `INACTIVE`, `DELETED`), profile info, login rate-limit tracking.
- **Property**: Listing with multi-step form data, status lifecycle, host FK, amenities (join table), photos, pricing config, booking mode.
- **PropertyAvailability**: Calendar of blocked/booked date ranges per property.
- **Amenity**: Master list of amenities (WiFi, Pool, Kitchen, Parking, etc.) with icon reference.
- **Booking**: Guest ↔ Property reservation with dates, guest count, price breakdown snapshot, status, and cancellation policy applied.
- **PriceBreakdown**: Embedded value object in Booking — nightly total, platform fee, tax, grand total.
- **Review**: Post-stay review by Guest (6 sub-category ratings) and/or Host response; double-blind until reveal.
- **Message**: Per-booking conversation message between Guest and Host.
- **Notification**: In-app bell notification per user with read/unread state.
- **Payment**: Razorpay order and payment record linked to a Booking.
- **Refund**: Refund record linked to a Booking/Payment with amount and status.
- **HostApplication**: User's application to become a Host with review status and admin notes.
- **PlatformConfig**: Key-value store for platform-wide settings (fee %, payout delay, etc.).
- **AuditLog**: Immutable log of significant admin actions (approve/reject/suspend/deactivate).

---

## Success Criteria *(mandatory)*

- **SC-001**: A Guest can complete the full journey — search → view listing → book stay — in under 3 minutes with no errors.
- **SC-002**: A Host can create and submit a listing through all 6 form steps and see it in `PENDING` status within 5 minutes.
- **SC-003**: A Property Manager can review and approve/reject a listing from the moderation queue in under 60 seconds.
- **SC-004**: All major endpoints are documented in Swagger UI at `/api/v1/docs` with accurate request/response examples.
- **SC-005**: JUnit + Mockito service-layer tests achieve 70%+ coverage.
- **SC-006**: `docker-compose up` starts the Spring Boot API + PostgreSQL with all Liquibase migrations applied and the API is ready to serve requests within 60 seconds.
- **SC-007**: JWT access token expires after 15 minutes; refresh token after 7 days; no session state is stored server-side.
- **SC-008**: All list APIs return paginated results — no endpoint returns an unbounded collection.
- **SC-009**: All API responses follow the standard `ApiResponse<T>` envelope.
- **SC-010**: Role-based access control is enforced — a Guest cannot access Host-only or Admin-only endpoints (403 returned).
- **SC-011**: Liquibase `.sql` migrations cover the complete DB schema; `spring.jpa.hibernate.ddl-auto` is set to `validate` in production profile.
- **SC-012**: Demo video covers: Guest search → listing detail → booking flow; Host listing creation → calendar; Admin approval queue → approve/reject a listing.

---

## Assumptions

- PostgreSQL is the only supported database. No multi-DB support needed.
- Email delivery uses a configurable SMTP provider (e.g., Mailhog for local dev, SendGrid in staging). Email templates are plain-text or simple HTML — no design system required for emails.
- Photo uploads are stored as URLs (e.g., Cloudinary or S3) — binary uploads are out of scope for this backend; the frontend will handle upload directly to the storage provider.
- Map integration (Leaflet) is a frontend concern only. The backend stores lat/lng coordinates per property and returns them in the listing response.
- Razorpay is used in **test mode only**. No real payment processing.
- WebSockets (STOMP/SockJS) for real-time messaging are a bonus and are not required for the core deliverable.
- Redis caching and CI/CD pipeline are bonus features and not part of the core submission.
- The mobile app is out of scope. The platform is web-only.
- All monetary values are stored in **paise** (smallest currency unit) in the database and converted to INR for display.
- The `SUPER_ADMIN` role is seeded via Liquibase/data migration — there is no self-registration for Super Admins.
- Frontend uses the native Fetch API with a JWT interceptor pattern (no Axios).
- React 19 frontends use TanStack Router, TanStack Query, React Hook Form + Zod, and Tailwind CSS.
- The GitHub monorepo structure is: `apps/customer/`, `apps/admin/`, `services/api/`, `.gitignore`, `README.md`.

---

## Out of Scope (v1)

- Native mobile app (iOS / Android)
- Multi-currency support
- Multi-language / i18n
- Hard-delete of user data (soft-delete only)
- External OAuth / SSO (Google, GitHub login)
- SMS notifications
- Video uploads or virtual tours
- Dynamic pricing / surge pricing algorithms
