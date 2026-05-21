-- Add UUID and reference codes for privacy and user-friendliness
-- BE3: Hybrid approach - keep sequential IDs for backward compatibility, add UUIDs and reference codes

-- Add UUID and reference code to parking_spaces
ALTER TABLE parking_spaces
ADD COLUMN IF NOT EXISTS uuid VARCHAR(36) UNIQUE NOT NULL DEFAULT gen_random_uuid()::text;

ALTER TABLE parking_spaces
ADD COLUMN IF NOT EXISTS reference_code VARCHAR(50) UNIQUE NOT NULL DEFAULT 'PKG-' || TO_CHAR(NOW(), 'YYYY-MM-DD') || '-' || LPAD(NEXTVAL('parking_spaces_seq')::text, 5, '0');

-- Add UUID and reference code to reservations
ALTER TABLE reservations
ADD COLUMN IF NOT EXISTS uuid VARCHAR(36) UNIQUE NOT NULL DEFAULT gen_random_uuid()::text;

ALTER TABLE reservations
ADD COLUMN IF NOT EXISTS reference_code VARCHAR(50) UNIQUE NOT NULL DEFAULT 'RES-' || TO_CHAR(NOW(), 'YYYY-MM-DD') || '-' || LPAD(NEXTVAL('reservations_seq')::text, 5, '0');

-- Add UUID and reference code to payments
ALTER TABLE payments
ADD COLUMN IF NOT EXISTS uuid VARCHAR(36) UNIQUE NOT NULL DEFAULT gen_random_uuid()::text;

ALTER TABLE payments
ADD COLUMN IF NOT EXISTS reference_code VARCHAR(50) UNIQUE NOT NULL DEFAULT 'PAY-' || TO_CHAR(NOW(), 'YYYY-MM-DD') || '-' || LPAD(NEXTVAL('payments_seq')::text, 5, '0');

-- Create sequences if they don't exist
CREATE SEQUENCE IF NOT EXISTS parking_spaces_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS reservations_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS payments_seq START WITH 1;

-- Add indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_parking_spaces_uuid ON parking_spaces(uuid);
CREATE INDEX IF NOT EXISTS idx_parking_spaces_reference_code ON parking_spaces(reference_code);
CREATE INDEX IF NOT EXISTS idx_parking_spaces_name ON parking_spaces(name);

CREATE INDEX IF NOT EXISTS idx_reservations_uuid ON reservations(uuid);
CREATE INDEX IF NOT EXISTS idx_reservations_reference_code ON reservations(reference_code);

CREATE INDEX IF NOT EXISTS idx_payments_uuid ON payments(uuid);
CREATE INDEX IF NOT EXISTS idx_payments_reference_code ON payments(reference_code);
