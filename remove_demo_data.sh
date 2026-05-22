#!/bin/bash
# Remove demo/seed data from the Smart Parking database (uses env vars).

set -euo pipefail

DB_NAME="${DB_NAME:-smartparking}"
DB_USER="${DB_USERNAME:-postgres}"
DB_HOST="${DB_HOST:-localhost}"

echo "Removing demo/seed data from database '${DB_NAME}'..."

psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" <<'EOF'
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

DELETE FROM events
WHERE name IN ('BK Arena Music Festival', 'BK Arena Concert');

DELETE FROM users
WHERE email LIKE '%@smartparking.rw'
   OR email LIKE '%@smartparking.com'
   OR email LIKE '%@kigali.seed';

SELECT COUNT(*) AS users_remaining FROM users;
SELECT COUNT(*) AS parking_spaces_remaining FROM parking_spaces;
EOF

echo "Demo data removal completed."
