-- V9: Ensure events table matches Event entity schema
-- Safely add/rename columns without breaking existing data

DO $$
BEGIN
  -- 1. Ensure start_time column exists (rename from event_date or add)
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'events' AND column_name = 'start_time') THEN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'events' AND column_name = 'event_date') THEN
      ALTER TABLE events RENAME COLUMN event_date TO start_time;
    ELSE
      ALTER TABLE events ADD COLUMN start_time TIMESTAMP NOT NULL DEFAULT NOW();
    END IF;
  END IF;

  -- 2. Ensure end_time column exists
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'events' AND column_name = 'end_time') THEN
    ALTER TABLE events ADD COLUMN end_time TIMESTAMP NOT NULL DEFAULT (NOW() + INTERVAL '1 hour');
  END IF;

  -- 3. Ensure radius_metres column exists (rename from radius_km or add)
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'events' AND column_name = 'radius_metres') THEN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'events' AND column_name = 'radius_km') THEN
      ALTER TABLE events RENAME COLUMN radius_km TO radius_metres;
    ELSE
      ALTER TABLE events ADD COLUMN radius_metres DOUBLE PRECISION NOT NULL DEFAULT 1000;
    END IF;
  END IF;

  -- 4. Ensure active column exists (rename from is_active or add)
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'events' AND column_name = 'active') THEN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'events' AND column_name = 'is_active') THEN
      ALTER TABLE events RENAME COLUMN is_active TO active;
    ELSE
      ALTER TABLE events ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
    END IF;
  END IF;

END $$;
