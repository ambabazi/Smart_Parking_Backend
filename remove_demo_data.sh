#!/bin/bash

# Script to remove demo data from Smart Parking database

echo "Removing demo data from Smart Parking database..."

# Connect to PostgreSQL and remove demo data
sudo -u postgres psql -d smartparking <<EOF

-- Remove demo seed data
DO $$
BEGIN
  -- Remove payments linked to demo reservations first
  DELETE FROM payments
  WHERE reservation_id IN (
    SELECT r.id
    FROM reservations r
    LEFT JOIN users u ON u.id = r.user_id
    WHERE u.email IN ('admin@smartparking.rw', 'host@smartparking.rw')
  );

  -- Remove demo reservations
  DELETE FROM reservations
  WHERE user_id IN (
    SELECT id FROM users WHERE email IN ('admin@smartparking.rw', 'host@smartparking.rw')
  );

  -- Remove demo users
  DELETE FROM users
  WHERE email IN ('admin@smartparking.rw', 'host@smartparking.rw', 'driver@smartparking.rw', 'host2@smartparking.rw');

  -- Remove demo parking spaces
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
  
  RAISE NOTICE 'Demo data removed successfully';
END $$;

-- Verify deletion
SELECT COUNT(*) as total_users FROM users;
SELECT COUNT(*) as total_parking_spaces FROM parking_spaces;
SELECT COUNT(*) as total_reservations FROM reservations;

EOF

echo "Demo data removal completed."
