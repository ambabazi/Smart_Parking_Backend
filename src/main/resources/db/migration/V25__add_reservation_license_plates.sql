-- License plate(s) for each reservation. Comma-separated when slot_count > 1
-- (e.g. 'RAB123A,RAB456B' for two cars).
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS license_plates VARCHAR(500);
