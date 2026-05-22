-- V7: Check-in/check-out tracking (idempotent for Render / partial databases)

ALTER TABLE reservations ADD COLUMN IF NOT EXISTS checked_in_at TIMESTAMP NULL;
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS checked_out_at TIMESTAMP NULL;
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS overtime_amount DECIMAL(19, 2) DEFAULT 0;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'reservations' AND column_name = 'status'
  ) THEN
    ALTER TABLE reservations ADD COLUMN status VARCHAR(50) DEFAULT 'ACTIVE';
  END IF;
END $$;

UPDATE reservations SET status = 'ACTIVE' WHERE status IS NULL;

CREATE INDEX IF NOT EXISTS idx_reservations_status ON reservations (status);
