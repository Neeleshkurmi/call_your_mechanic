# ER Review: Scalability and Normalization

## Quick assessment of the provided model

Overall, the model is a good base and mostly normalized (close to 3NF):
- Core entities are separated (`users`, `mechanics`, `vehicles`, `services`, `bookings`).
- Lookup-like categorical values are constrained (`role`, `vehicle_type`, `status`).
- Transaction data (`bookings`) references master data through foreign keys.

## What is already good

1. **`users` vs `mechanics` split**
   - Keeping mechanic-specific fields (`is_available`, `experience_years`, `rating`) in `mechanics` is a good normalization choice.
   - Prevents sparse/null-heavy `users` rows.

2. **`vehicles` linked to users**
   - Correct one-to-many structure and avoids repeating vehicle attributes in bookings.

3. **`services` as separate entity**
   - Good for consistent service catalog management.

4. **`bookings` as central transactional table**
   - Properly references user, mechanic, vehicle, and service.

## Scalability / normalization improvements applied in entities

1. **User and mechanic extensions remain separated**
   - `mechanics.user_id` is unique (one mechanic profile per user).

2. **Locations modeled as dedicated tables**
   - `user_locations` supports multiple saved addresses per user.
   - `mechanic_locations` supports time-series location updates (history) for tracking and dispatch.

3. **Unique and query-friendly indexes added**
   - `users.mob` unique.
   - Composite indexes for booking time queries (`user_id, booking_time` and `mechanic_id, booking_time`).
   - Status and coordinate-related indexes where useful.

4. **Snapshot fields in bookings retained intentionally**
   - Booking `latitude`/`longitude` kept as immutable snapshots to preserve where the job was requested, even if saved location changes later.

5. **Vehicle identity uniqueness enhanced**
   - Added `registration_number` unique in `vehicles` to avoid duplicates and improve matching.

## Optional next improvements (future)

- If one service can support multiple vehicle types, replace `services.vehicle_type` with bridge table:
  - `service_vehicle_types(service_id, vehicle_type)`.
- Add geospatial indexing (PostGIS/Spatial) for nearest-mechanic queries at scale.
- Add `booking_status_history` table for full audit trail instead of only current status.

## Implementation note from review feedback

- Added shared inheritance with `BaseEntity` (`id`) and `AuditableEntity` (`createdAt`, `updatedAt`) to remove duplicate columns/mappings across entities and keep the model consistent as tables grow.
