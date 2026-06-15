-- Master reconcile: idempotent full schema sync for JPA entities.
-- Run after V1–V15; safe on fresh DBs and partially-migrated Render databases.

CREATE SEQUENCE IF NOT EXISTS parking_spaces_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS reservations_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS payments_seq START WITH 1;

-- ── users ──────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    full_name   VARCHAR(100)        NOT NULL,
    email       VARCHAR(150) UNIQUE NOT NULL,
    phone       VARCHAR(20)  UNIQUE NOT NULL,
    password    VARCHAR(255)        NOT NULL,
    role        VARCHAR(20)         NOT NULL DEFAULT 'DRIVER',
    created_at  TIMESTAMP           NOT NULL DEFAULT NOW()
);

-- ── events ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS events (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL
);

ALTER TABLE events ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION;
ALTER TABLE events ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;
ALTER TABLE events ADD COLUMN IF NOT EXISTS radius_metres DOUBLE PRECISION DEFAULT 1000;
-- V1 created this as radius_km DECIMAL(5,2); V9 only renamed it (type unchanged),
-- so on existing DBs it is still DECIMAL(5,2) and cannot hold 1000. Widen it now.
ALTER TABLE events ALTER COLUMN radius_metres TYPE DOUBLE PRECISION;
-- 'venue' is a legacy V1 column (NOT NULL, no default) that the Event entity no
-- longer maps. Drop the constraint so entity inserts and seed rows don't fail.
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'events' AND column_name = 'venue'
  ) THEN
    EXECUTE 'ALTER TABLE events ALTER COLUMN venue DROP NOT NULL';
  END IF;
END $$;
ALTER TABLE events ADD COLUMN IF NOT EXISTS start_time TIMESTAMP;
ALTER TABLE events ADD COLUMN IF NOT EXISTS end_time TIMESTAMP;
ALTER TABLE events ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE;
ALTER TABLE events ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();

UPDATE events SET latitude = 0 WHERE latitude IS NULL;
UPDATE events SET longitude = 0 WHERE longitude IS NULL;
UPDATE events SET radius_metres = 1000 WHERE radius_metres IS NULL;
UPDATE events SET start_time = NOW() WHERE start_time IS NULL;
UPDATE events SET end_time = NOW() + INTERVAL '1 hour' WHERE end_time IS NULL;
UPDATE events SET active = TRUE WHERE active IS NULL;

-- ── parking_spaces ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS parking_spaces (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

ALTER TABLE parking_spaces ADD COLUMN IF NOT EXISTS owner_id BIGINT;
ALTER TABLE parking_spaces ADD COLUMN IF NOT EXISTS address VARCHAR(255);
ALTER TABLE parking_spaces ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION;
ALTER TABLE parking_spaces ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;
ALTER TABLE parking_spaces ADD COLUMN IF NOT EXISTS total_slots INT DEFAULT 1;
ALTER TABLE parking_spaces ADD COLUMN IF NOT EXISTS available_slots INT DEFAULT 1;
ALTER TABLE parking_spaces ADD COLUMN IF NOT EXISTS price_per_slot DOUBLE PRECISION DEFAULT 500;
ALTER TABLE parking_spaces ADD COLUMN IF NOT EXISTS event_enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE parking_spaces ADD COLUMN IF NOT EXISTS current_event_id BIGINT;
ALTER TABLE parking_spaces ADD COLUMN IF NOT EXISTS uuid VARCHAR(36);
ALTER TABLE parking_spaces ADD COLUMN IF NOT EXISTS reference_code VARCHAR(50);

UPDATE parking_spaces SET uuid = gen_random_uuid()::text WHERE uuid IS NULL;
UPDATE parking_spaces
SET reference_code = 'PKG-' || TO_CHAR(NOW(), 'YYYY-MM-DD') || '-' || upper(substr(md5(random()::text), 1, 8))
WHERE reference_code IS NULL;

-- ── reservations ───────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS reservations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    total_amount DECIMAL(19, 2) NOT NULL DEFAULT 0
);

ALTER TABLE reservations ADD COLUMN IF NOT EXISTS parking_space_id BIGINT;
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS slot_count INT DEFAULT 1;
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS paid BOOLEAN DEFAULT FALSE;
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS verified BOOLEAN DEFAULT FALSE;
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS qr_code VARCHAR(100);
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS checked_in_at TIMESTAMP;
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS checked_out_at TIMESTAMP;
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS overtime_amount DECIMAL(19, 2) DEFAULT 0;
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'ACTIVE';
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS uuid VARCHAR(36);
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS reference_code VARCHAR(50);

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'reservations' AND column_name = 'parking_id'
  ) AND NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'reservations' AND column_name = 'parking_space_id'
  ) THEN
    EXECUTE 'ALTER TABLE reservations RENAME COLUMN parking_id TO parking_space_id';
  END IF;
END $$;

UPDATE reservations SET qr_code = 'legacy-' || id::text WHERE qr_code IS NULL OR btrim(qr_code) = '';
UPDATE reservations SET status = 'ACTIVE' WHERE status IS NULL;
UPDATE reservations SET paid = FALSE WHERE paid IS NULL;
UPDATE reservations SET verified = FALSE WHERE verified IS NULL;
UPDATE reservations SET slot_count = 1 WHERE slot_count IS NULL;
UPDATE reservations SET uuid = gen_random_uuid()::text WHERE uuid IS NULL;
UPDATE reservations
SET reference_code = 'RES-' || TO_CHAR(NOW(), 'YYYY-MM-DD') || '-' || upper(substr(md5(random()::text), 1, 8))
WHERE reference_code IS NULL;

-- ── payments ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    reservation_id BIGINT
);

ALTER TABLE payments ADD COLUMN IF NOT EXISTS amount DECIMAL(19, 2);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS status VARCHAR(30);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS flutterwave_id VARCHAR(100);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS uuid VARCHAR(36);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS reference_code VARCHAR(50);

UPDATE payments SET uuid = gen_random_uuid()::text WHERE uuid IS NULL;
UPDATE payments
SET reference_code = 'PAY-' || TO_CHAR(NOW(), 'YYYY-MM-DD') || '-' || upper(substr(md5(random()::text), 1, 8))
WHERE reference_code IS NULL;

-- ── indexes ────────────────────────────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_parking_spaces_owner_id ON parking_spaces (owner_id);
CREATE INDEX IF NOT EXISTS idx_parking_spaces_uuid ON parking_spaces (uuid);
CREATE INDEX IF NOT EXISTS idx_parking_spaces_reference_code ON parking_spaces (reference_code);
CREATE INDEX IF NOT EXISTS idx_parking_spaces_name ON parking_spaces (name);
CREATE INDEX IF NOT EXISTS idx_reservations_user_id ON reservations (user_id);
CREATE INDEX IF NOT EXISTS idx_reservations_parking_space_id ON reservations (parking_space_id);
CREATE INDEX IF NOT EXISTS idx_reservations_status ON reservations (status);
CREATE INDEX IF NOT EXISTS idx_reservations_uuid ON reservations (uuid);
CREATE INDEX IF NOT EXISTS idx_reservations_reference_code ON reservations (reference_code);
CREATE INDEX IF NOT EXISTS idx_payments_reservation_id ON payments (reservation_id);
CREATE INDEX IF NOT EXISTS idx_payments_uuid ON payments (uuid);
CREATE INDEX IF NOT EXISTS idx_payments_reference_code ON payments (reference_code);
