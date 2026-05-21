-- V2__insert_demo_data.sql
-- Seeds Kigali parking data for demo/hackathon

DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'users'
      AND column_name = 'name'
  ) AND NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'users'
      AND column_name = 'full_name'
  ) THEN
    EXECUTE 'ALTER TABLE users RENAME COLUMN name TO full_name';
  END IF;

  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'users'
      AND column_name = 'full_name'
  ) THEN
    EXECUTE 'ALTER TABLE users ADD COLUMN full_name VARCHAR(100)';
  END IF;

  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'users'
      AND column_name = 'phone'
  ) THEN
    EXECUTE 'ALTER TABLE users ADD COLUMN phone VARCHAR(20)';
  END IF;

  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'users'
      AND column_name = 'created_at'
  ) THEN
    EXECUTE 'ALTER TABLE users ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW()';
  END IF;
END $$;

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
  (owner_id, name, address, latitude, longitude, total_slots, available_slots, price_per_slot, event_enabled)
SELECT u.id, v.name, v.address, v.lat, v.lng, v.slots, v.slots, v.price, FALSE
FROM (
  VALUES
    ('BK Arena Parking', 'KG 11 Ave, Kigali', -1.9502, 30.0588, 80, 500.00),
    ('Kigali Convention Centre', 'KG 2 Roundabout, Kigali', -1.9513, 30.0627, 120, 1000.00),
    ('Nyarugenge Market Parking', 'KN 4 Ave, Kigali', -1.9503, 30.0575, 40, 300.00),
    ('Remera Parking Zone', 'KG 9 Ave, Remera', -1.9412, 30.1112, 30, 300.00),
    ('Kicukiro Commercial Parking', 'KK 15 Rd, Kicukiro', -1.9784, 30.0932, 25, 250.00)
) AS v(name, address, lat, lng, slots, price)
JOIN users u ON u.email = 'host@smartparking.rw'
WHERE NOT EXISTS (
  SELECT 1 FROM parking_spaces p WHERE p.name = v.name
);

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
