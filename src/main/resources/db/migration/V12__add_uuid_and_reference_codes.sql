-- Add UUID and reference codes for privacy and user-friendliness
-- Sequences must exist before defaults/backfill use them.

CREATE SEQUENCE IF NOT EXISTS parking_spaces_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS reservations_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS payments_seq START WITH 1;

-- parking_spaces
ALTER TABLE parking_spaces ADD COLUMN IF NOT EXISTS uuid VARCHAR(36);
ALTER TABLE parking_spaces ADD COLUMN IF NOT EXISTS reference_code VARCHAR(50);

UPDATE parking_spaces
SET uuid = gen_random_uuid()::text
WHERE uuid IS NULL;

UPDATE parking_spaces
SET reference_code = 'PKG-' || TO_CHAR(NOW(), 'YYYY-MM-DD') || '-' || LPAD(nextval('parking_spaces_seq')::text, 5, '0')
WHERE reference_code IS NULL;

ALTER TABLE parking_spaces ALTER COLUMN uuid SET NOT NULL;
ALTER TABLE parking_spaces ALTER COLUMN reference_code SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_parking_spaces_uuid') THEN
        ALTER TABLE parking_spaces ADD CONSTRAINT uk_parking_spaces_uuid UNIQUE (uuid);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_parking_spaces_reference_code') THEN
        ALTER TABLE parking_spaces ADD CONSTRAINT uk_parking_spaces_reference_code UNIQUE (reference_code);
    END IF;
END $$;

-- reservations
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS uuid VARCHAR(36);
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS reference_code VARCHAR(50);

UPDATE reservations
SET uuid = gen_random_uuid()::text
WHERE uuid IS NULL;

UPDATE reservations
SET reference_code = 'RES-' || TO_CHAR(NOW(), 'YYYY-MM-DD') || '-' || LPAD(nextval('reservations_seq')::text, 5, '0')
WHERE reference_code IS NULL;

ALTER TABLE reservations ALTER COLUMN uuid SET NOT NULL;
ALTER TABLE reservations ALTER COLUMN reference_code SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_reservations_uuid') THEN
        ALTER TABLE reservations ADD CONSTRAINT uk_reservations_uuid UNIQUE (uuid);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_reservations_reference_code') THEN
        ALTER TABLE reservations ADD CONSTRAINT uk_reservations_reference_code UNIQUE (reference_code);
    END IF;
END $$;

-- payments
ALTER TABLE payments ADD COLUMN IF NOT EXISTS uuid VARCHAR(36);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS reference_code VARCHAR(50);

UPDATE payments
SET uuid = gen_random_uuid()::text
WHERE uuid IS NULL;

UPDATE payments
SET reference_code = 'PAY-' || TO_CHAR(NOW(), 'YYYY-MM-DD') || '-' || LPAD(nextval('payments_seq')::text, 5, '0')
WHERE reference_code IS NULL;

ALTER TABLE payments ALTER COLUMN uuid SET NOT NULL;
ALTER TABLE payments ALTER COLUMN reference_code SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_payments_uuid') THEN
        ALTER TABLE payments ADD CONSTRAINT uk_payments_uuid UNIQUE (uuid);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_payments_reference_code') THEN
        ALTER TABLE payments ADD CONSTRAINT uk_payments_reference_code UNIQUE (reference_code);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_parking_spaces_uuid ON parking_spaces(uuid);
CREATE INDEX IF NOT EXISTS idx_parking_spaces_reference_code ON parking_spaces(reference_code);
CREATE INDEX IF NOT EXISTS idx_parking_spaces_name ON parking_spaces(name);

CREATE INDEX IF NOT EXISTS idx_reservations_uuid ON reservations(uuid);
CREATE INDEX IF NOT EXISTS idx_reservations_reference_code ON reservations(reference_code);

CREATE INDEX IF NOT EXISTS idx_payments_uuid ON payments(uuid);
CREATE INDEX IF NOT EXISTS idx_payments_reference_code ON payments(reference_code);
