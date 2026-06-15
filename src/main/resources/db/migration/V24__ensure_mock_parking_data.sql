-- Idempotent mock data: safe on fresh DBs and on DBs where V23 already ran.
-- Ensures parking spaces show on the map immediately after deploy.

INSERT INTO users (full_name, email, phone, password, role)
VALUES ('Smart Parking Demo Host',
        'demo.host@smartparking.app',
        '+250788000088',
        '$2a$12$LQv3c1yqBwEHFl5.7qV0eO6JlhZdWQTdFxI9.dqZ5a7XPbNtq5FaS',
        'HOST')
ON CONFLICT (email) DO NOTHING;

INSERT INTO parking_spaces (uuid, reference_code, owner_id, name, address,
                            latitude, longitude, total_slots, available_slots,
                            price_per_slot, event_enabled, image_url)
SELECT gen_random_uuid()::text,
       'PKG-' || upper(substr(md5(v.name), 1, 8)),
       u.id, v.name, v.address, v.latitude, v.longitude,
       v.total_slots, v.available_slots, v.price_per_slot, FALSE, v.image_url
FROM users u
CROSS JOIN (
    VALUES
        ('Kigali Heights Parking',       'KG 7 Ave, Kimihurura, Kigali',         -1.9534, 30.0934, 60, 48, 700.0,  'https://picsum.photos/seed/kigaliheights/800/500'),
        ('Norrsken House Garage',        'KN 78 St, Nyarugenge, Kigali',         -1.9486, 30.0588, 40, 31, 600.0,  'https://picsum.photos/seed/norrsken/800/500'),
        ('Kigali Convention Centre Lot', 'KG 2 Roundabout, Kimihurura, Kigali',  -1.9540, 30.0928, 80, 65, 1000.0, 'https://picsum.photos/seed/kcc/800/500'),
        ('Kimironko Market Parking',     'KG 11 Ave, Kimironko, Kigali',         -1.9264, 30.1108, 35, 20, 400.0,  'https://picsum.photos/seed/kimironko/800/500'),
        ('Nyabugogo Terminal Parking',   'Nyabugogo Bus Terminal, Kigali',       -1.9398, 30.0444, 100, 72, 300.0, 'https://picsum.photos/seed/nyabugogo/800/500'),
        ('Remera Giporoso Parking',      'KG 9 Ave, Remera, Kigali',             -1.9560, 30.1180, 30, 12, 450.0,  'https://picsum.photos/seed/remera/800/500'),
        ('CBD Central Parking',          'KN 4 Ave, Kigali CBD',                 -1.9441, 30.0619, 50, 5, 800.0,   'https://picsum.photos/seed/cbd/800/500'),
        ('BK Arena Parking',             'KK 15 Ave, Kigali Arena, Kigali',       -1.9502, 30.0588, 120, 85, 900.0, 'https://picsum.photos/seed/bkarena/800/500'),
        ('Kacyiru Central Parking',      'KG 7 Ave, Kacyiru, Kigali',            -1.9302, 30.0863, 45, 28, 550.0,  'https://picsum.photos/seed/kacyiru/800/500'),
        ('Kimihurura Plaza Parking',     'KG 7 Ave, Kimihurura, Kigali',         -1.9396, 30.0758, 25, 18, 650.0,  'https://picsum.photos/seed/kimihurura/800/500')
) AS v(name, address, latitude, longitude, total_slots, available_slots, price_per_slot, image_url)
WHERE u.email = 'demo.host@smartparking.app'
  AND NOT EXISTS (SELECT 1 FROM parking_spaces p WHERE p.name = v.name);

-- Link spaces near the active BK Arena event (V18 uses old names; fix with current ones).
UPDATE parking_spaces ps
SET event_enabled = TRUE,
    current_event_id = e.id
FROM events e
WHERE e.name = 'BK Arena Music Festival'
  AND e.active = TRUE
  AND ps.name IN (
      'BK Arena Parking',
      'Kigali Convention Centre Lot',
      'Kigali Heights Parking',
      'Norrsken House Garage'
  )
  AND (ps.current_event_id IS DISTINCT FROM e.id OR ps.event_enabled IS NOT TRUE);
