DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'events'
      AND column_name = 'is_active'
  ) AND NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'events'
      AND column_name = 'active'
  ) THEN
    EXECUTE 'ALTER TABLE events ADD COLUMN active BOOLEAN';
    EXECUTE 'UPDATE events SET active = COALESCE(is_active, FALSE) WHERE active IS NULL';
    EXECUTE 'ALTER TABLE events ALTER COLUMN active SET DEFAULT FALSE';
    EXECUTE 'ALTER TABLE events ALTER COLUMN active SET NOT NULL';
  ELSIF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'events'
      AND column_name = 'active'
  ) THEN
    EXECUTE 'ALTER TABLE events ADD COLUMN active BOOLEAN NOT NULL DEFAULT FALSE';
  END IF;
END $$;