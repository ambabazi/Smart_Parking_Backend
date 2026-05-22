-- Demo seed (optional). Safe after V12 uuid/reference_code columns exist.
-- Uses current schema: owner_id, start_time/end_time/radius_metres/active on events.

INSERT INTO users (full_name, email, phone, password, role) VALUES
('Admin User', 'admin@smartparking.rw', '+250788000001',
 '$2a$12$LQv3c1yqBwEHFl5.7qV0eO6JlhZdWQTdFxI9.dqZ5a7XPbNtq5FaS', 'ADMIN')
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (full_name, email, phone, password, role) VALUES
('John Host', 'host@smartparking.rw', '+250788000002',
 '$2a$12$LQv3c1yqBwEHFl5.7qV0eO6JlhZdWQTdFxI9.dqZ5a7XPbNtq5FaS', 'HOST')
ON CONFLICT (email) DO NOTHING;

INSERT INTO parking_spaces
  (owner_id, name, address, latitude, longitude, total_slots, available_slots,
   price_per_slot, event_enabled, uuid, reference_code)
SELECT u.id, v.name, v.address, v.lat, v.lng, v.slots, v.slots, v.price, FALSE,
       gen_random_uuid()::text,
       'PKG-' || TO_CHAR(NOW(), 'YYYY-MM-DD') || '-' || LPAD(nextval('parking_spaces_seq')::text, 5, '0')
FROM (
  VALUES
    ('BK Arena Parking', 'KG 11 Ave, Kigali', -1.9502, 30.0588, 80, 500.00),
    ('Kigali Convention Centre', 'KG 2 Roundabout, Kigali', -1.9513, 30.0627, 120, 1000.00),
    ('Nyarugenge Market Parking', 'KN 4 Ave, Kigali', -1.9503, 30.0575, 40, 300.00),
    ('Remera Parking Zone', 'KG 9 Ave, Remera', -1.9412, 30.1112, 30, 300.00),
    ('Kicukiro Commercial Parking', 'KK 15 Rd, Kicukiro', -1.9784, 30.0932, 25, 250.00)
) AS v(name, address, lat, lng, slots, price)
JOIN users u ON u.email = 'host@smartparking.rw'
WHERE NOT EXISTS (SELECT 1 FROM parking_spaces p WHERE p.name = v.name);

INSERT INTO events (name, latitude, longitude, radius_metres, start_time, end_time, active)
SELECT 'BK Arena Music Festival', -1.9502, 30.0588, 1500,
       NOW() + INTERVAL '2 hours', NOW() + INTERVAL '6 hours', TRUE
WHERE NOT EXISTS (
  SELECT 1 FROM events e WHERE e.name = 'BK Arena Music Festival'
);
