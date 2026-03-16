INSERT INTO users (id, created_at, updated_at, name, mob, role)
VALUES
    (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Test User', '+919900000001', 'USER'),
    (2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Test Mechanic User', '+919900000002', 'MECHANIC'),
    (3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Backup User', '+919900000003', 'USER')
ON CONFLICT (id) DO UPDATE SET
    updated_at = CURRENT_TIMESTAMP,
    name = EXCLUDED.name,
    mob = EXCLUDED.mob,
    role = EXCLUDED.role;

INSERT INTO mechanics (id, created_at, updated_at, user_id, is_available, experience_years, rating)
VALUES
    (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2, TRUE, 6, 4.80)
ON CONFLICT (id) DO UPDATE SET
    updated_at = CURRENT_TIMESTAMP,
    user_id = EXCLUDED.user_id,
    is_available = EXCLUDED.is_available,
    experience_years = EXCLUDED.experience_years,
    rating = EXCLUDED.rating;

INSERT INTO services (id, name, description, vehicle_type)
VALUES
    (1, 'Flat Tyre Repair', 'On-site puncture repair and tyre inflation.', 'BIKE'),
    (2, 'Battery Jump Start', 'Quick jump start service for dead batteries.', 'CAR'),
    (3, 'Engine Diagnostics', 'Basic engine health and fault-code diagnostics.', 'CAR'),
    (4, 'Chain Adjustment', 'Chain tightening and lubrication for bikes.', 'BIKE')
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    vehicle_type = EXCLUDED.vehicle_type;

INSERT INTO vehicles (id, created_at, updated_at, user_id, vehicle_type, brand, model, registration_number)
VALUES
    (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'BIKE', 'Honda', 'Shine', 'MH12AB1234'),
    (2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 'CAR', 'Hyundai', 'i20', 'MH14CD5678')
ON CONFLICT (id) DO UPDATE SET
    updated_at = CURRENT_TIMESTAMP,
    user_id = EXCLUDED.user_id,
    vehicle_type = EXCLUDED.vehicle_type,
    brand = EXCLUDED.brand,
    model = EXCLUDED.model,
    registration_number = EXCLUDED.registration_number;

INSERT INTO bookings (id, created_at, updated_at, mechanic_id, user_id, vehicle_id, service_id, status, booking_time, latitude, longitude)
VALUES
    (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1, 1, 1, 'REQUESTED', CURRENT_TIMESTAMP - INTERVAL '30 minutes', 18.5204, 73.8567),
    (2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1, 1, 4, 'ACCEPTED', CURRENT_TIMESTAMP - INTERVAL '2 hours', 18.5310, 73.8440)
ON CONFLICT (id) DO UPDATE SET
    updated_at = CURRENT_TIMESTAMP,
    mechanic_id = EXCLUDED.mechanic_id,
    user_id = EXCLUDED.user_id,
    vehicle_id = EXCLUDED.vehicle_id,
    service_id = EXCLUDED.service_id,
    status = EXCLUDED.status,
    booking_time = EXCLUDED.booking_time,
    latitude = EXCLUDED.latitude,
    longitude = EXCLUDED.longitude;

INSERT INTO mechanic_locations (id, mechanic_id, latitude, longitude, recorded_at)
VALUES
    (1, 1, 18.5208, 73.8572, CURRENT_TIMESTAMP - INTERVAL '10 minutes')
ON CONFLICT (id) DO UPDATE SET
    mechanic_id = EXCLUDED.mechanic_id,
    latitude = EXCLUDED.latitude,
    longitude = EXCLUDED.longitude,
    recorded_at = EXCLUDED.recorded_at;

INSERT INTO user_locations (id, created_at, updated_at, user_id, latitude, longitude, address, is_default)
VALUES
    (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 18.5204, 73.8567, 'Pune Test Address', TRUE),
    (2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 18.6300, 73.8000, 'Backup Test Address', FALSE)
ON CONFLICT (id) DO UPDATE SET
    updated_at = CURRENT_TIMESTAMP,
    user_id = EXCLUDED.user_id,
    latitude = EXCLUDED.latitude,
    longitude = EXCLUDED.longitude,
    address = EXCLUDED.address,
    is_default = EXCLUDED.is_default;

SELECT setval(pg_get_serial_sequence('users', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM users), 1), true);
SELECT setval(pg_get_serial_sequence('mechanics', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM mechanics), 1), true);
SELECT setval(pg_get_serial_sequence('services', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM services), 1), true);
SELECT setval(pg_get_serial_sequence('vehicles', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM vehicles), 1), true);
SELECT setval(pg_get_serial_sequence('bookings', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM bookings), 1), true);
SELECT setval(pg_get_serial_sequence('mechanic_locations', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM mechanic_locations), 1), true);
SELECT setval(pg_get_serial_sequence('user_locations', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM user_locations), 1), true);
