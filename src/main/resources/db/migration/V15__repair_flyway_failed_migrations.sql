-- Safety backfill if a prior migration partially applied identifier columns.

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'parking_spaces' AND column_name = 'uuid'
  ) THEN
    UPDATE parking_spaces
    SET uuid = gen_random_uuid()::text
    WHERE uuid IS NULL;

    UPDATE parking_spaces
    SET reference_code = 'PKG-' || TO_CHAR(NOW(), 'YYYY-MM-DD') || '-' || upper(substr(md5(random()::text), 1, 8))
    WHERE reference_code IS NULL;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'reservations' AND column_name = 'uuid'
  ) THEN
    UPDATE reservations
    SET uuid = gen_random_uuid()::text
    WHERE uuid IS NULL;

    UPDATE reservations
    SET reference_code = 'RES-' || TO_CHAR(NOW(), 'YYYY-MM-DD') || '-' || upper(substr(md5(random()::text), 1, 8))
    WHERE reference_code IS NULL;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'payments' AND column_name = 'uuid'
  ) THEN
    UPDATE payments
    SET uuid = gen_random_uuid()::text
    WHERE uuid IS NULL;

    UPDATE payments
    SET reference_code = 'PAY-' || TO_CHAR(NOW(), 'YYYY-MM-DD') || '-' || upper(substr(md5(random()::text), 1, 8))
    WHERE reference_code IS NULL;
  END IF;
END $$;
