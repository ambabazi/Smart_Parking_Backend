-- Align existing database schema with the current JPA entities

DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'reservations'
      AND column_name = 'parking_id'
  ) AND NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'reservations'
      AND column_name = 'parking_space_id'
  ) THEN
    EXECUTE 'ALTER TABLE reservations RENAME COLUMN parking_id TO parking_space_id';
  END IF;

  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'reservations'
      AND column_name = 'slots_booked'
  ) AND NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'reservations'
      AND column_name = 'slot_count'
  ) THEN
    EXECUTE 'ALTER TABLE reservations RENAME COLUMN slots_booked TO slot_count';
  END IF;

  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'reservations'
      AND column_name = 'paid'
  ) THEN
    EXECUTE 'ALTER TABLE reservations ADD COLUMN paid BOOLEAN NOT NULL DEFAULT FALSE';
  END IF;

  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'reservations'
      AND column_name = 'verified'
  ) THEN
    EXECUTE 'ALTER TABLE reservations ADD COLUMN verified BOOLEAN NOT NULL DEFAULT FALSE';
  END IF;
END $$;

ALTER TABLE parking_spaces
  ALTER COLUMN latitude TYPE DOUBLE PRECISION USING latitude::DOUBLE PRECISION,
  ALTER COLUMN longitude TYPE DOUBLE PRECISION USING longitude::DOUBLE PRECISION,
  ALTER COLUMN price_per_slot TYPE DOUBLE PRECISION USING price_per_slot::DOUBLE PRECISION;

ALTER TABLE reservations
  ALTER COLUMN qr_code SET NOT NULL;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'reservations_qr_code_key'
  ) THEN
    EXECUTE 'ALTER TABLE reservations ADD CONSTRAINT reservations_qr_code_key UNIQUE (qr_code)';
  END IF;
END $$;