-- Remove demo seed data from production and fresh databases
-- This runs after the earlier seed migrations so the app ends up with real data only.

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'reservations' AND column_name = 'parking_space_id'
  ) THEN
    DELETE FROM payments
    WHERE reservation_id IN (
      SELECT r.id
      FROM reservations r
      LEFT JOIN users u ON u.id = r.user_id
      LEFT JOIN parking_spaces p ON p.id = r.parking_space_id
      WHERE u.email IN ('admin@smartparking.rw', 'host@smartparking.rw', 'host@kigali.seed')
         OR p.name IN (
              'BK Arena Parking',
              'Kigali Convention Centre',
              'Nyarugenge Market Parking',
              'Remera Parking Zone',
              'Kicukiro Commercial Parking',
              'Kacyiru Parking',
              'Kimironko Parking',
              'Nyabugogo Parking',
              'CBD Parking',
              'Remera Parking'
         )
    );

    DELETE FROM reservations
    WHERE user_id IN (
      SELECT id FROM users WHERE email IN ('admin@smartparking.rw', 'host@smartparking.rw', 'host@kigali.seed')
    )
    OR parking_space_id IN (
      SELECT id FROM parking_spaces
      WHERE name IN (
        'BK Arena Parking',
        'Kigali Convention Centre',
        'Nyarugenge Market Parking',
        'Remera Parking Zone',
        'Kicukiro Commercial Parking',
        'Kacyiru Parking',
        'Kimironko Parking',
        'Nyabugogo Parking',
        'CBD Parking',
        'Remera Parking'
      )
    );
  ELSE
    DELETE FROM payments
    WHERE reservation_id IN (
      SELECT r.id
      FROM reservations r
      LEFT JOIN users u ON u.id = r.user_id
      LEFT JOIN parking_spaces p ON p.id = r.parking_id
      WHERE u.email IN ('admin@smartparking.rw', 'host@smartparking.rw', 'host@kigali.seed')
         OR p.name IN (
              'BK Arena Parking',
              'Kigali Convention Centre',
              'Nyarugenge Market Parking',
              'Remera Parking Zone',
              'Kicukiro Commercial Parking',
              'Kacyiru Parking',
              'Kimironko Parking',
              'Nyabugogo Parking',
              'CBD Parking',
              'Remera Parking'
         )
    );

    DELETE FROM reservations
    WHERE user_id IN (
      SELECT id FROM users WHERE email IN ('admin@smartparking.rw', 'host@smartparking.rw', 'host@kigali.seed')
    )
    OR parking_id IN (
      SELECT id FROM parking_spaces
      WHERE name IN (
        'BK Arena Parking',
        'Kigali Convention Centre',
        'Nyarugenge Market Parking',
        'Remera Parking Zone',
        'Kicukiro Commercial Parking',
        'Kacyiru Parking',
        'Kimironko Parking',
        'Nyabugogo Parking',
        'CBD Parking',
        'Remera Parking'
      )
    );
  END IF;

  DELETE FROM parking_spaces
  WHERE name IN (
    'BK Arena Parking',
    'Kigali Convention Centre',
    'Nyarugenge Market Parking',
    'Remera Parking Zone',
    'Kicukiro Commercial Parking',
    'Kacyiru Parking',
    'Kimironko Parking',
    'Nyabugogo Parking',
    'CBD Parking',
    'Remera Parking'
  )
  OR owner_id IN (
    SELECT id FROM users WHERE email IN ('admin@smartparking.rw', 'host@smartparking.rw', 'host@kigali.seed')
  );

  DELETE FROM events
  WHERE name = 'BK Arena Music Festival';

  DELETE FROM users
  WHERE email IN ('admin@smartparking.rw', 'host@smartparking.rw', 'host@kigali.seed');
END $$;