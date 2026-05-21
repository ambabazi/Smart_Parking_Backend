-- Remove demo seed data from production and fresh databases
-- This runs after the earlier seed migrations so the app ends up with real data only.

DO $$
BEGIN
  -- Remove payments linked to demo reservations first.
  DELETE FROM payments
  WHERE reservation_id IN (
    SELECT r.id
    FROM reservations r
    LEFT JOIN users u ON u.id = r.user_id
    LEFT JOIN parking_spaces p ON p.id = r.parking_id
    WHERE u.email IN ('admin@smartparking.rw', 'host@smartparking.rw')
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

  -- Remove demo reservations.
  DELETE FROM reservations
  WHERE user_id IN (
    SELECT id FROM users WHERE email IN ('admin@smartparking.rw', 'host@smartparking.rw')
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

  -- Remove demo parking spaces from both seed files.
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
  OR host_id IN (
    SELECT id FROM users WHERE email IN ('admin@smartparking.rw', 'host@smartparking.rw')
  );

  -- Remove the demo event if present.
  DELETE FROM events
  WHERE name = 'BK Arena Music Festival';

  -- Remove demo users last so foreign keys are satisfied.
  DELETE FROM users
  WHERE email IN ('admin@smartparking.rw', 'host@smartparking.rw');
END $$;