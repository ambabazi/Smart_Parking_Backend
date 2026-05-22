-- Final demo purge for DBs that previously ran seed migrations (V5/V13).
-- Only removes known demo domains and seeded parking/event names.

DO $$
DECLARE
  parking_col TEXT;
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'reservations' AND column_name = 'parking_space_id'
  ) THEN
    parking_col := 'parking_space_id';
  ELSIF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'reservations' AND column_name = 'parking_id'
  ) THEN
    parking_col := 'parking_id';
  END IF;

  DELETE FROM payments
  WHERE reservation_id IN (
    SELECT r.id FROM reservations r
    JOIN users u ON u.id = r.user_id
    WHERE u.email LIKE '%@smartparking.rw'
       OR u.email LIKE '%@smartparking.com'
       OR u.email LIKE '%@kigali.seed'
  );

  DELETE FROM reservations r
  USING users u
  WHERE r.user_id = u.id
    AND (u.email LIKE '%@smartparking.rw'
      OR u.email LIKE '%@smartparking.com'
      OR u.email LIKE '%@kigali.seed');

  IF parking_col IS NOT NULL THEN
    EXECUTE format($sql$
      DELETE FROM reservations r
      USING parking_spaces p
      WHERE r.%I = p.id
        AND p.name IN (
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
    $sql$, parking_col);
  END IF;

  DELETE FROM events
  WHERE name IN ('BK Arena Music Festival', 'BK Arena Concert');

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
  );

  DELETE FROM users
  WHERE email LIKE '%@smartparking.rw'
     OR email LIKE '%@smartparking.com'
     OR email LIKE '%@kigali.seed';
END $$;
