-- Seed Kigali parking spaces (requires owner_id after V4).
-- Idempotent: skips rows that already exist.

INSERT INTO users (full_name, email, phone, password, role)
VALUES ('Kigali Seed Host',
        'host@kigali.seed',
        '+250788000099',
        '$2a$12$LQv3c1yqBwEHFl5.7qV0eO6JlhZdWQTdFxI9.dqZ5a7XPbNtq5FaS',
        'HOST')
ON CONFLICT (email) DO NOTHING;

INSERT INTO parking_spaces (owner_id, name, address, latitude, longitude,
                            total_slots, available_slots,
                            price_per_slot, event_enabled)
SELECT u.id, v.name, v.address, v.latitude, v.longitude,
       v.total_slots, v.available_slots, v.price_per_slot, v.event_enabled
FROM users u
CROSS JOIN (
    VALUES
        ('Kacyiru Parking', 'KG 7 Ave, Kacyiru, Kigali', -1.9355, 30.0928, 30, 30, 500.0, FALSE),
        ('Kimironko Parking', 'KG 11 Ave, Kimironko, Kigali', -1.9264, 30.1100, 25, 25, 400.0, FALSE),
        ('Nyabugogo Parking', 'Nyabugogo Bus Terminal, Kigali', -1.9394, 30.0444, 50, 50, 300.0, FALSE),
        ('CBD Parking', 'KN 4 Ave, Kigali CBD', -1.9441, 30.0619, 40, 40, 800.0, FALSE),
        ('Remera Parking', 'KG 9 Ave, Remera, Kigali', -1.9536, 30.1122, 20, 20, 450.0, FALSE)
) AS v(name, address, latitude, longitude, total_slots, available_slots, price_per_slot, event_enabled)
WHERE u.email = 'host@kigali.seed'
  AND NOT EXISTS (
    SELECT 1 FROM parking_spaces p WHERE p.name = v.name
);
