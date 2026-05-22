-- Remove all demo/seed data (idempotent). Safe to re-run on every deploy.

DO $$
DECLARE
  parking_col TEXT;
  demo_parking_names TEXT[] := ARRAY[
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
  ];
  demo_emails TEXT[] := ARRAY[
    'admin@smartparking.rw',
    'host@smartparking.rw',
    'host@kigali.seed',
    'driver@smartparking.rw',
    'host2@smartparking.rw',
    'testdriver@smartparking.com',
    'testhost@smartparking.com'
  ];
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
    WHERE u.email = ANY (demo_emails)
  );

  DELETE FROM reservations
  WHERE user_id IN (SELECT id FROM users WHERE email = ANY (demo_emails));

  IF parking_col IS NOT NULL THEN
    EXECUTE format(
      'DELETE FROM reservations WHERE %I IN (SELECT id FROM parking_spaces WHERE name = ANY ($1))',
      parking_col
    ) USING demo_parking_names;
  END IF;

  DELETE FROM events WHERE name IN ('BK Arena Music Festival', 'BK Arena Concert');

  DELETE FROM parking_spaces WHERE name = ANY (demo_parking_names);

  DELETE FROM parking_spaces
  WHERE owner_id IN (SELECT id FROM users WHERE email = ANY (demo_emails));

  DELETE FROM users WHERE email = ANY (demo_emails);
END $$;
