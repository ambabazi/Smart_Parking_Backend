-- Reconcile floating-point column types with the JPA entities.
-- V1 created these as DECIMAL/NUMERIC, but Event and ParkingSpace map them as
-- Double (float8). Hibernate's schema validation (ddl-auto: validate) rejects the
-- NUMERIC vs FLOAT mismatch, so widen them to DOUBLE PRECISION. Idempotent:
-- re-running against an already-DOUBLE column is a no-op. BigDecimal-backed money
-- columns (amounts) intentionally stay NUMERIC/DECIMAL.

DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns
             WHERE table_name = 'events' AND column_name = 'latitude') THEN
    EXECUTE 'ALTER TABLE events ALTER COLUMN latitude TYPE DOUBLE PRECISION';
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns
             WHERE table_name = 'events' AND column_name = 'longitude') THEN
    EXECUTE 'ALTER TABLE events ALTER COLUMN longitude TYPE DOUBLE PRECISION';
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns
             WHERE table_name = 'events' AND column_name = 'radius_metres') THEN
    EXECUTE 'ALTER TABLE events ALTER COLUMN radius_metres TYPE DOUBLE PRECISION';
  END IF;

  IF EXISTS (SELECT 1 FROM information_schema.columns
             WHERE table_name = 'parking_spaces' AND column_name = 'latitude') THEN
    EXECUTE 'ALTER TABLE parking_spaces ALTER COLUMN latitude TYPE DOUBLE PRECISION';
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns
             WHERE table_name = 'parking_spaces' AND column_name = 'longitude') THEN
    EXECUTE 'ALTER TABLE parking_spaces ALTER COLUMN longitude TYPE DOUBLE PRECISION';
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns
             WHERE table_name = 'parking_spaces' AND column_name = 'price_per_slot') THEN
    EXECUTE 'ALTER TABLE parking_spaces ALTER COLUMN price_per_slot TYPE DOUBLE PRECISION';
  END IF;
END $$;
