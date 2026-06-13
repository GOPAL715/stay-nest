# StayNest

An Airbnb-style property rental platform built as a full-stack monorepo. StayNest ships two React 19 frontends — a customer-facing web app and an admin/host portal — powered by a single Spring Boot 4 REST API backed by PostgreSQL.

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Backend Language** | Java 25 |
| **Backend Framework** | Spring Boot 4 |
| **ORM** | Spring Data JPA / Hibernate |
| **Database** | PostgreSQL 16 |
| **Schema Migrations** | Liquibase (`.sql` changelogs) |
| **Object Mapping** | MapStruct 1.6+ |
| **Security** | Spring Security 6 + JJWT (JWT + Refresh Tokens) |
| **API Documentation** | SpringDoc OpenAPI 3 (Swagger UI) |
| **Testing** | JUnit 5 + Mockito |
| **Build Tool** | Maven |
| **Containerisation** | Docker + Docker Compose |
| **Frontend Framework** | React 19 |
| **Routing** | TanStack Router |
| **Server State** | TanStack Query |
| **Forms** | React Hook Form + Zod |
| **Styling** | Tailwind CSS |
| **HTTP Client** | Native Fetch API with JWT interceptor |
| **Map** | Leaflet + React-Leaflet (customer app) |
| **Payments (bonus)** | Razorpay (test mode) |

---

## Monorepo Structure

```
staynest/
├── apps/
│   ├── customer/          # React 19 customer web app (search, book, trips)
│   │   ├── src/
│   │   ├── package.json
│   │   └── vite.config.ts
│   └── admin/             # React 19 admin / host portal
│       ├── src/
│       ├── package.json
│       └── vite.config.ts
├── services/
│   └── api/               # Spring Boot 4 REST API
│       ├── src/
│       ├── pom.xml
│       └── Dockerfile
├── docker-compose.yml
├── .env.example
├── .gitignore
└── README.md
```

---

## Prerequisites

| Tool | Version |
|---|---|
| Java | 25 |
| Maven | 3.9+ |
| Node.js | 20+ |
| Docker | latest |
| Docker Compose | v2+ |

---

## Quick Start (Full Stack)

The fastest way to get everything running is with Docker Compose. This starts PostgreSQL and the Spring Boot API together, applies all Liquibase migrations, and exposes the API on port 8080.

1. Copy the environment file and fill in your values:

```bash
cp .env.example .env
```

2. Start the full stack:

```bash
docker-compose up --build
```

The API will be available at `http://localhost:8080`. Swagger UI is at `http://localhost:8080/api/v1/docs`.

> The frontend apps are not containerised — run them separately using the manual steps below.

---

## Manual Dev Setup

### Backend

```bash
cd services/api
mvn spring-boot:run
```

The API starts on port `8080`. Make sure PostgreSQL is running and your `.env` variables are set (or exported in your shell).

### Customer Frontend

```bash
cd apps/customer
npm install
npm run dev
```

Runs at `http://localhost:5173`.

### Admin Frontend

```bash
cd apps/admin
npm install
npm run dev
```

Runs at `http://localhost:5174`.

---

## Environment Variables

Copy `.env.example` to `.env` in the project root and fill in every value. The `.env` file is gitignored — never commit it.

| Variable | Description |
|---|---|
| `POSTGRES_DB` | PostgreSQL database name |
| `POSTGRES_USER` | PostgreSQL username |
| `POSTGRES_PASSWORD` | PostgreSQL password |
| `JWT_SECRET` | 256-bit hex secret for signing JWTs (generate with `openssl rand -hex 32`) |
| `JWT_ACCESS_EXPIRY_MS` | Access token TTL in ms (default: `900000` = 15 min) |
| `JWT_REFRESH_EXPIRY_MS` | Refresh token TTL in ms (default: `604800000` = 7 days) |
| `MAIL_HOST` | SMTP host (use `localhost` with MailHog for local dev) |
| `MAIL_PORT` | SMTP port (use `1025` for MailHog, `587` for SendGrid) |
| `MAIL_USERNAME` | SMTP username / API key |
| `MAIL_PASSWORD` | SMTP password / API key secret |
| `EMAIL_FROM` | Sender address for outbound emails |
| `APP_BASE_URL` | Backend base URL (used in email links) |
| `FRONTEND_CUSTOMER_URL` | Customer app origin (used for CORS) |
| `FRONTEND_ADMIN_URL` | Admin app origin (used for CORS) |
| `RAZORPAY_KEY_ID` | Razorpay test key ID (bonus feature) |
| `RAZORPAY_KEY_SECRET` | Razorpay test key secret (bonus feature) |

See `.env.example` for the full list with comments.

---

## API Documentation

Swagger UI is served at:

```
http://localhost:8080/api/v1/docs
```

All endpoints are documented with request/response schemas. No authentication is required to access the docs UI.

---

## Running Tests

```bash
cd services/api
mvn test
```

Tests are JUnit 5 + Mockito service-layer tests targeting 70%+ coverage. Test classes cover: `AuthServiceImpl`, `PropertyServiceImpl`, `BookingServiceImpl`, `AdminListingServiceImpl`, `AdminUserServiceImpl`, and `NotificationServiceImpl`.

---

## User Roles

| Role | Description |
|---|---|
| **Super Admin** | Full platform access including configuration (fees, policies, feature flags) |
| **Property Manager** | Reviews and moderates property listings; manages the approval queue |
| **Host** | Creates and manages property listings; reviews booking requests; sees earnings |
| **Guest** | Searches and books properties; manages trips; writes reviews |
| **Support Agent** | Views user history; manages bookings on behalf of users; handles escalations |

> The `SUPER_ADMIN` account is seeded via Liquibase on first startup — there is no self-registration for admins.

---

## Key Endpoints

All responses follow the standard `ApiResponse<T>` envelope:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {},
  "timestamp": "2026-06-12T10:00:00Z",
  "path": "/api/v1/properties"
}
```

### Auth — `/api/v1/auth`

| Method | Path | Description |
|---|---|---|
| POST | `/register` | Register a new user |
| POST | `/login` | Login → returns JWT access + refresh token |
| POST | `/refresh` | Rotate refresh token → new access token |
| POST | `/logout` | Invalidate refresh token |
| GET | `/verify-email?token=` | Verify email address |
| POST | `/forgot-password` | Send password reset link |
| POST | `/reset-password` | Reset password via token |

### Properties — `/api/v1/properties`

| Method | Path | Description |
|---|---|---|
| GET | `/` | Search properties (location, dates, guests, filters) |
| GET | `/{propertyId}` | Get property detail |
| POST | `/` | Create listing (Host) |
| PUT | `/{propertyId}` | Update listing (Host owner) |
| PATCH | `/{propertyId}/submit` | Submit listing for review |
| GET | `/my-listings` | Get own listings (Host) |
| GET | `/{propertyId}/availability` | Get availability calendar |
| POST | `/{propertyId}/availability/block` | Block date range (Host) |

### Bookings — `/api/v1/bookings`

| Method | Path | Description |
|---|---|---|
| POST | `/` | Create booking (Guest) |
| GET | `/my-trips` | Guest's own bookings |
| GET | `/host-bookings` | Bookings for host's properties |
| GET | `/{bookingId}` | Get booking detail |
| PATCH | `/{bookingId}/confirm` | Confirm request-to-book (Host) |
| PATCH | `/{bookingId}/cancel` | Cancel booking |

### Admin — `/api/v1/admin`

| Method | Path | Description |
|---|---|---|
| GET | `/listings/pending` | Moderation queue |
| PATCH | `/listings/{propertyId}/approve` | Approve listing |
| PATCH | `/listings/{propertyId}/reject` | Reject listing with reason |
| PATCH | `/listings/{propertyId}/suspend` | Suspend active listing |
| GET | `/bookings` (via `/api/v1/bookings`) | All bookings (filterable) |
| PATCH | `/bookings/{bookingId}/refund` | Issue full or partial refund |
| GET | `/config` | Platform configuration |
| PUT | `/config/{configKey}` | Update platform setting |

Full API reference with all parameters and response schemas: `http://localhost:8080/api/v1/docs`

---

## Demo Flows

### Guest: Search and Book

1. Register on the customer app → verify your email → log in.
2. On the home page, enter a location, check-in/check-out dates, and guest count → click Search.
3. Use the filters (price range, amenities, property type) to narrow results.
4. Click a listing card to open the detail page — review photos, amenities, host info, and availability calendar.
5. Select your dates and click **Reserve** — review the itemised price breakdown (nightly rate × nights + platform fee + taxes = total).
6. Confirm the booking — if the listing is **Instant Book**, it's confirmed immediately; if **Request to Book**, it's pending until the host accepts.
7. View your booking in **My Trips**.

### Host: Create a Listing

1. Log in to the admin/host portal.
2. Navigate to **My Listings** → **Create New Listing**.
3. Complete the 6-step form: Basic Info → Location → Amenities → Photos → Pricing → Availability.
4. Save as **Draft** at any point to resume later.
5. When ready, click **Submit for Review** — the listing moves to `PENDING` and appears in the Property Manager's moderation queue.
6. You'll receive an email once the listing is approved or if changes are requested.

### Admin: Approve a Listing

1. Log in to the admin portal as a Property Manager or Super Admin.
2. Open the **Moderation Queue** (sorted oldest-first).
3. Click a pending listing to review its details, photos, and pricing.
4. Choose one of: **Approve** (moves to `ACTIVE`, host notified), **Reject** (returns to `DRAFT`, host emailed with reason), or **Request Changes** (host notified with a comment).
5. Approved listings are immediately searchable by Guests.
