INSERT INTO users (id, created_at, updated_at, name, mob, role)
VALUES
    (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Test User', '+919900000001', 'USER'),
    (2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Test Mechanic User', '+919900000002', 'MECHANIC'),
    (3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Backup User', '+919900000003', 'USER'),
    (4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Amit Garage', '+919900000004', 'MECHANIC'),
    (5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Rahul Auto Care', '+919900000005', 'MECHANIC'),
    (6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Vikram Wheels', '+919900000006', 'MECHANIC'),
    (7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Suresh Roadside', '+919900000007', 'MECHANIC'),
    (8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Arjun Motors', '+919900000008', 'MECHANIC'),
    (9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Deepak Workshop', '+919900000009', 'MECHANIC'),
    (10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Rakesh Service Point', '+919900000010', 'MECHANIC'),
    (11, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Manoj Mobile Mechanic', '+919900000011', 'MECHANIC'),
    (12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Karan Quick Fix', '+919900000012', 'MECHANIC')
ON CONFLICT (id) DO UPDATE SET
    updated_at = CURRENT_TIMESTAMP,
    name = EXCLUDED.name,
    mob = EXCLUDED.mob,
    role = EXCLUDED.role;

INSERT INTO mechanics (id, created_at, updated_at, user_id, is_available, experience_years, rating, bio, skills)
VALUES
    (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2, TRUE, 6, 4.80, 'Fast roadside mechanic for engine and battery jobs.', 'Engine diagnostics, battery jump start'),
    (2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 4, TRUE, 5, 4.60, 'Quick puncture and tyre support for city breakdowns.', 'Tyre repair, wheel balancing'),
    (3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 5, TRUE, 8, 4.75, 'Experienced mechanic for hatchback and sedan issues.', 'Battery jump start, engine diagnostics'),
    (4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 6, TRUE, 4, 4.30, 'Focused on bike and scooter emergency servicing.', 'Chain adjustment, puncture repair'),
    (5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 7, TRUE, 7, 4.55, 'On-road service specialist with fast response times.', 'Oil check, flat tyre support'),
    (6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 8, TRUE, 9, 4.90, 'Senior mechanic for complex vehicle troubleshooting.', 'Engine repair, battery, roadside inspection'),
    (7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 9, TRUE, 3, 4.10, 'Budget-friendly mechanic for daily breakdown needs.', 'Bike service, jump start'),
    (8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 10, TRUE, 10, 4.85, 'Premium roadside support for cars and SUVs.', 'Engine diagnostics, puncture, battery'),
    (9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 11, TRUE, 6, 4.50, 'Reliable mechanic available for emergency vehicle care.', 'Tyre repair, oil service'),
    (10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 12, TRUE, 5, 4.40, 'General roadside mechanic for quick on-site fixes.', 'Battery, puncture, minor engine support')
ON CONFLICT (id) DO UPDATE SET
    updated_at = CURRENT_TIMESTAMP,
    user_id = EXCLUDED.user_id,
    is_available = EXCLUDED.is_available,
    experience_years = EXCLUDED.experience_years,
    rating = EXCLUDED.rating,
    bio = EXCLUDED.bio,
    skills = EXCLUDED.skills;

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
    (1, 1, 22.5945, 76.9094, CURRENT_TIMESTAMP - INTERVAL '10 minutes'),
    (2, 2, 22.5945, 76.9094, CURRENT_TIMESTAMP - INTERVAL '9 minutes'),
    (3, 3, 22.5945, 76.9094, CURRENT_TIMESTAMP - INTERVAL '8 minutes'),
    (4, 4, 22.5945, 76.9094, CURRENT_TIMESTAMP - INTERVAL '7 minutes'),
    (5, 5, 22.5945, 76.9094, CURRENT_TIMESTAMP - INTERVAL '6 minutes'),
    (6, 6, 22.5945, 76.9094, CURRENT_TIMESTAMP - INTERVAL '5 minutes'),
    (7, 7, 22.5945, 76.9094, CURRENT_TIMESTAMP - INTERVAL '4 minutes'),
    (8, 8, 22.5945, 76.9094, CURRENT_TIMESTAMP - INTERVAL '3 minutes'),
    (9, 9, 22.5945, 76.9094, CURRENT_TIMESTAMP - INTERVAL '2 minutes'),
    (10, 10, 22.5945, 76.9094, CURRENT_TIMESTAMP - INTERVAL '1 minutes')
ON CONFLICT (id) DO UPDATE SET
    mechanic_id = EXCLUDED.mechanic_id,
    latitude = EXCLUDED.latitude,
    longitude = EXCLUDED.longitude,
    recorded_at = EXCLUDED.recorded_at;

INSERT INTO user_locations (id, created_at, updated_at, user_id, latitude, longitude, label, address, is_default)
VALUES
    (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 18.5204, 73.8567, 'HOME', 'Pune Test Address', TRUE),
    (2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 18.6300, 73.8000, 'OFFICE', 'Backup Test Address', FALSE)
ON CONFLICT (id) DO UPDATE SET
    updated_at = CURRENT_TIMESTAMP,
    user_id = EXCLUDED.user_id,
    latitude = EXCLUDED.latitude,
    longitude = EXCLUDED.longitude,
    label = EXCLUDED.label,
    address = EXCLUDED.address,
    is_default = EXCLUDED.is_default;

SELECT setval(pg_get_serial_sequence('users', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM users), 1), true);
SELECT setval(pg_get_serial_sequence('mechanics', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM mechanics), 1), true);
SELECT setval(pg_get_serial_sequence('services', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM services), 1), true);
SELECT setval(pg_get_serial_sequence('vehicles', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM vehicles), 1), true);
SELECT setval(pg_get_serial_sequence('bookings', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM bookings), 1), true);
SELECT setval(pg_get_serial_sequence('mechanic_locations', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM mechanic_locations), 1), true);
SELECT setval(pg_get_serial_sequence('user_locations', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM user_locations), 1), true);
SELECT setval(pg_get_serial_sequence('user_live_locations', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM user_live_locations), 1), true);
SELECT setval(pg_get_serial_sequence('reviews', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM reviews), 1), true);
