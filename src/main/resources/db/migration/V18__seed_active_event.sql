-- Seed one active event so Event Parking has live data on fresh deploys.
-- 'venue' is a legacy V1 column (NOT NULL, no default) the Event entity no longer
-- maps. On databases where V16 already applied before its venue fix existed, the
-- constraint is still present, so drop it here (idempotent) before inserting and
-- before the app performs entity inserts that omit venue.
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'events' AND column_name = 'venue'
  ) THEN
    EXECUTE 'ALTER TABLE events ALTER COLUMN venue DROP NOT NULL';
  END IF;
END $$;

INSERT INTO events (name, latitude, longitude, radius_metres, start_time, end_time, active, created_at)
SELECT
    'BK Arena Music Festival',
    -1.9502,
    30.0588,
    1500,
    NOW() - INTERVAL '30 minutes',
    NOW() + INTERVAL '6 hours',
    TRUE,
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM events WHERE name = 'BK Arena Music Festival' AND active = TRUE
);

UPDATE parking_spaces ps
SET event_enabled = TRUE,
    current_event_id = e.id
FROM events e
WHERE e.name = 'BK Arena Music Festival'
  AND e.active = TRUE
  AND ps.name IN ('BK Arena Parking', 'Kigali Convention Centre', 'Kacyiru Parking', 'Remera Parking Zone')
  AND (ps.current_event_id IS NULL OR ps.current_event_id <> e.id);
