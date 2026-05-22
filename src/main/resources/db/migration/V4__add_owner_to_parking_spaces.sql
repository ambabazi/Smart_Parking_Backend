-- Add owner_id column to parking_spaces (idempotent)

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'parking_spaces' AND column_name = 'owner_id'
    ) THEN
        ALTER TABLE parking_spaces ADD COLUMN owner_id BIGINT NOT NULL DEFAULT 1;
        ALTER TABLE parking_spaces ALTER COLUMN owner_id DROP DEFAULT;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_parking_owner'
    ) THEN
        ALTER TABLE parking_spaces
            ADD CONSTRAINT fk_parking_owner FOREIGN KEY (owner_id) REFERENCES users(id);
    END IF;
END $$;
