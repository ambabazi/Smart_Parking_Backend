-- Remove demo seed data from production and fresh databases

DO $$
DECLARE
  parking_col TEXT;
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'reservations' AND column_name = 'parking_space_id'
  ) THEN
    parking_col := 'parking_space_id';
  ELSE
    parking_col := 'parking_id';
  END IF;

  EXECUTE format($sql$
    DELETE FROM payments
    WHERE reservation_id IN (
      SELECT r.id
      FROM reservations r
      LEFT JOIN users u ON u.id = r.user_id
      LEFT JOIN parking_spaces p ON p.id = r.%I
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
    )
  $sql$, parking_col);

  EXECUTE format($sql$
    DELETE FROM reservations
    WHERE user_id IN (
      SELECT id FROM users WHERE email IN ('admin@smartparking.rw', 'host@smartparking.rw', 'host@kigali.seed')
    )
    OR %I IN (
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
    )
  $sql$, parking_col);

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

  DELETE FROM events WHERE name = 'BK Arena Music Festival';

  DELETE FROM users
  WHERE email IN ('admin@smartparking.rw', 'host@smartparking.rw', 'host@kigali.seed');
END $$;
