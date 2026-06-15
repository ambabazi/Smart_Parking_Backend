-- 1) Per-space photo column (nullable). Any URL: hosted image, CDN, or frontend asset.
ALTER TABLE parking_spaces ADD COLUMN IF NOT EXISTS image_url VARCHAR(512);

-- 2) Seed realistic Kigali mock parking spaces so the map has live data on fresh deploys.
--    A dedicated demo host owns them. The email domain is intentionally NOT one of the
--    demo domains the purge migrations/scripts target (@kigali.seed, @smartparking.rw,
--    @smartparking.com), so this data is not removed by those clean-ups.
INSERT INTO users (full_name, email, phone, password, role)
VALUES ('Smart Parking Demo Host',
        'demo.host@smartparking.app',
        '+250788000088',
        '$2a$12$LQv3c1yqBwEHFl5.7qV0eO6JlhZdWQTdFxI9.dqZ5a7XPbNtq5FaS',
        'HOST')
ON CONFLICT (email) DO NOTHING;

-- uuid and reference_code are NOT NULL (added in V12). The entity fills them via
-- @PrePersist, but raw SQL must supply them explicitly.
INSERT INTO parking_spaces (uuid, reference_code, owner_id, name, address,
                            latitude, longitude, total_slots, available_slots,
                            price_per_slot, event_enabled, image_url)
SELECT gen_random_uuid()::text,
       'PKG-' || upper(substr(md5(v.name), 1, 8)),
       u.id, v.name, v.address, v.latitude, v.longitude,
       v.total_slots, v.available_slots, v.price_per_slot, v.event_enabled, v.image_url
FROM users u
CROSS JOIN (
    VALUES
        ('Kigali Heights Parking',      'KG 7 Ave, Kimihurura, Kigali',        -1.9534, 30.0934, 60, 48, 700.0, FALSE, 'https://picsum.photos/seed/kigaliheights/800/500'),
        ('Norrsken House Garage',       'KN 78 St, Nyarugenge, Kigali',        -1.9486, 30.0588, 40, 31, 600.0, FALSE, 'https://picsum.photos/seed/norrsken/800/500'),
        ('Kigali Convention Centre Lot','KG 2 Roundabout, Kimihurura, Kigali', -1.9540, 30.0928, 80, 65, 1000.0, FALSE, 'https://picsum.photos/seed/kcc/800/500'),
        ('Kimironko Market Parking',    'KG 11 Ave, Kimironko, Kigali',        -1.9264, 30.1108, 35, 20, 400.0, FALSE, 'https://picsum.photos/seed/kimironko/800/500'),
        ('Nyabugogo Terminal Parking',  'Nyabugogo Bus Terminal, Kigali',      -1.9398, 30.0444, 100, 72, 300.0, FALSE, 'https://picsum.photos/seed/nyabugogo/800/500'),
        ('Remera Giporoso Parking',     'KG 9 Ave, Remera, Kigali',            -1.9560, 30.1180, 30, 12, 450.0, FALSE, 'https://picsum.photos/seed/remera/800/500'),
        ('CBD Central Parking',         'KN 4 Ave, Kigali CBD',                -1.9441, 30.0619, 50, 5, 800.0, FALSE, 'https://picsum.photos/seed/cbd/800/500')
) AS v(name, address, latitude, longitude, total_slots, available_slots, price_per_slot, event_enabled, image_url)
WHERE u.email = 'demo.host@smartparking.app'
  AND NOT EXISTS (SELECT 1 FROM parking_spaces p WHERE p.name = v.name);
