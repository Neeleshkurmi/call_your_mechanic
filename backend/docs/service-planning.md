# Call Your Mechanic: Service Build Plan and API Endpoints

## 1) Product goal
Build a backend service that helps end users:
- Discover nearby mechanics based on service type, vehicle type, and location.
- Book vehicle services with a selected mechanic and track booking lifecycle.
- Manage profile, vehicles, and saved locations.

## 2) Core user journeys
1. **Authentication and onboarding**
   - User signs in with OTP.
   - User selects role (`USER` or `MECHANIC`).
2. **Vehicle and location setup**
   - End user adds vehicle(s) and saved service location(s).
3. **Mechanic discovery**
   - User requests mechanics near a location with filters (vehicle type, service, availability).
4. **Booking flow**
   - User creates a booking request, gets estimate/time slot, confirms booking.
   - Mechanic accepts/rejects booking.
   - Booking transitions through statuses until completion/cancellation.
5. **Post-service**
   - User rates mechanic and can rebook quickly.

## 3) Domain modules and ownership
- **Auth module**: OTP, tokens, role assignment, session refresh/logout.
- **User module**: profile, saved locations.
- **Vehicle module**: add/update/delete vehicles.
- **Mechanic module**: mechanic profile, live location, availability window.
- **Catalog module**: service catalog and supported vehicle types.
- **Booking module**: request, assignment, status management, cancellation.
- **Discovery module**: nearest mechanic search and filtering.

## 4) API design conventions
- Base path: `/api/v1`
- Response envelope: existing `ApiResponse<T>` wrapper.
- Auth: JWT bearer token for protected routes.
- Idempotency: booking creation and payment-related actions should support idempotency key header.
- Pagination: `page`, `size`, `sort` query params for list endpoints.

## 5) Endpoint plan

### 5.1 Authentication and session
These already exist and should be retained:
- `POST /auth/otp/request`
- `POST /auth/otp/verify`
- `POST /auth/otp/role`
- `POST /refresh`
- `POST /logout`

### 5.2 User profile and saved locations
- `GET /users/me` - fetch current user profile.
- `PATCH /users/me` - update name/email.
- `GET /users/me/locations` - list saved locations.
- `POST /users/me/locations` - add location.
- `PATCH /users/me/locations/{locationId}` - update location metadata.
- `DELETE /users/me/locations/{locationId}` - remove location.

### 5.3 Vehicle management
- `GET /users/me/vehicles`
- `POST /users/me/vehicles`
- `GET /users/me/vehicles/{vehicleId}`
- `PATCH /users/me/vehicles/{vehicleId}`
- `DELETE /users/me/vehicles/{vehicleId}`

### 5.4 Service catalog
- `GET /services` - list services by vehicle type.
- `GET /services/{serviceId}`

### 5.5 Mechanic profile and availability
- `GET /mechanics/me`
- `PATCH /mechanics/me`
- `PATCH /mechanics/me/availability` - online/offline, work radius.
- `POST /mechanics/me/locations` - push current location ping.
- `GET /mechanics/{mechanicId}` - public mechanic profile.

### 5.6 Discovery (end user)
- `GET /mechanics/nearby?lat=&lng=&radiusKm=&vehicleType=&serviceId=`
- `GET /mechanics/{mechanicId}/slots?date=` - optional slot visibility.

### 5.7 Booking lifecycle
- `POST /bookings` - create booking (user).
- `GET /bookings/{bookingId}` - fetch booking details.
- `GET /users/me/bookings` - list user bookings.
- `GET /mechanics/me/bookings` - list mechanic bookings.
- `PATCH /bookings/{bookingId}/accept` - mechanic action.
- `PATCH /bookings/{bookingId}/reject` - mechanic action.
- `PATCH /bookings/{bookingId}/start` - mechanic marks in-progress.
- `PATCH /bookings/{bookingId}/complete` - mechanic marks complete.
- `PATCH /bookings/{bookingId}/cancel` - user/mechanic/system cancellation.

### 5.8 Ratings and feedback
- `POST /bookings/{bookingId}/rating`
- `GET /mechanics/{mechanicId}/ratings`

## 6) Booking state machine
Recommended status sequence:
- `PENDING` -> `ACCEPTED` -> `IN_PROGRESS` -> `COMPLETED`
- Cancellation branches:
  - `PENDING`/`ACCEPTED` -> `CANCELLED_BY_USER`
  - `PENDING`/`ACCEPTED` -> `CANCELLED_BY_MECHANIC`
  - System timeout -> `EXPIRED`

## 7) Delivery roadmap

### Phase 1 (MVP)
- Complete auth stabilization and role onboarding.
- Implement user profile + vehicle CRUD + saved locations CRUD.
- Build service catalog read endpoints.
- Implement booking creation/read/list + mechanic accept/reject.

### Phase 2
- Nearby mechanic discovery with distance filter.
- Mechanic live location pings and availability controls.
- Booking progression endpoints (`start`, `complete`, `cancel`).

### Phase 3
- Ratings, booking timeline/history, notifications, analytics.
- Smarter dispatch/ranking (rating, ETA, specialization).

## 8) Non-functional requirements
- **Security**: role-based access (`USER`, `MECHANIC`, `ADMIN`) at endpoint level.
- **Performance**: indexes on booking status/time and geo lookup strategy.
- **Observability**: structured logs, request IDs, metrics for booking funnel.
- **Reliability**: retries for OTP delivery and location ingestion.
- **Auditability**: immutable booking status history table in later phase.

## 9) Suggested immediate next tasks
1. Add REST controllers for User, Vehicle, Service, Mechanic, and Booking modules.
2. Add DTO + validation for each create/update endpoint.
3. Add service-layer authorization guards around booking transitions.
4. Add OpenAPI documentation and example payloads for all endpoints.
5. Add integration tests for booking state transitions and role access control.

## 10) Implementation progress (current)
- Added `GET /api/v1/services` and `GET /api/v1/services/{serviceId}` endpoints with optional `vehicleType` filtering.
- Added booking endpoints:
  - `POST /api/v1/bookings`
  - `GET /api/v1/bookings/{bookingId}`
  - `GET /api/v1/users/me/bookings`
  - `GET /api/v1/mechanics/me/bookings`
  - `PATCH /api/v1/bookings/{bookingId}/accept`
  - `PATCH /api/v1/bookings/{bookingId}/reject`
- Booking APIs are now JWT protected; user identity and role are extracted from Bearer access token claims.
- Added validation and guardrails for booking ownership and state transition (`REQUESTED -> ACCEPTED/CANCELLED`).
