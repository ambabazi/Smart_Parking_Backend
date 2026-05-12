-- V2__insert_demo_data.sql
-- Seeds Kigali parking data for demo/hackathon

-- Admin user (password: Admin@123 — bcrypt)
INSERT INTO users (full_name, email, phone, password, role) VALUES
('Admin User',
 'admin@smartparking.rw',
 '+250788000001',
 '$2a$12$LQv3c1yqBwEHFl5.7qV0eO6JlhZdWQTdFxI9.dqZ5a7XPbNtq5FaS',
 'ADMIN')
ON CONFLICT (email) DO NOTHING;

-- Demo host
INSERT INTO users (full_name, email, phone, password, role) VALUES
('John Host',
 'host@smartparking.rw',
 '+250788000002',
 '$2a$12$LQv3c1yqBwEHFl5.7qV0eO6JlhZdWQTdFxI9.dqZ5a7XPbNtq5FaS',
 'HOST')
ON CONFLICT (email) DO NOTHING;

-- 5 real Kigali parking spaces
INSERT INTO parking_spaces
  (host_id, name, address, latitude, longitude, total_slots, available_slots, price_per_hour)
SELECT id, name, address, lat, lng, slots, slots, price FROM (
  VALUES
    (2, 'BK Arena Parking',
     'KG 11 Ave, Kigali', -1.9502, 30.0588, 80, 500.00),
    (2, 'Kigali Convention Centre',
     'KG 2 Roundabout, Kigali', -1.9513, 30.0627, 120, 1000.00),
    (2, 'Nyarugenge Market Parking',
     'KN 4 Ave, Kigali', -1.9503, 30.0575, 40, 300.00),
    (2, 'Remera Parking Zone',
     'KG 9 Ave, Remera', -1.9412, 30.1112, 30, 300.00),
    (2, 'Kicukiro Commercial Parking',
     'KK 15 Rd, Kicukiro', -1.9784, 30.0932, 25, 250.00)
) AS t(hid, name, address, lat, lng, slots, price)
JOIN users u ON u.id = t.hid
ON CONFLICT DO NOTHING;

-- Demo event: BK Arena Concert
INSERT INTO events
  (name, venue, latitude, longitude, event_date, radius_km, is_active, price_multiplier)
VALUES
  ('BK Arena Music Festival',
   'BK Arena, KG 11 Ave',
   -1.9502, 30.0588,
   NOW() + INTERVAL '2 hours',
   1.5, TRUE, 2.0)
ON CONFLICT DO NOTHING;
