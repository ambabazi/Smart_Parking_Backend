-- Final schema touch-ups (idempotent). V3 already applied most of these changes.

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

UPDATE reservations
SET qr_code = 'legacy-' || id::text
WHERE qr_code IS NULL OR btrim(qr_code) = '';

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'reservations' AND column_name = 'qr_code' AND is_nullable = 'YES'
  ) THEN
    EXECUTE 'ALTER TABLE reservations ALTER COLUMN qr_code SET NOT NULL';
  END IF;
END $$;