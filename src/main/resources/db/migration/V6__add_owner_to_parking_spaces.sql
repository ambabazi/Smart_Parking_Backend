-- Add owner_id column to parking_spaces table
-- Allows tracking which HOST/owner manages each parking space

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'parking_spaces'
        AND column_name = 'owner_id'
    ) THEN
        ALTER TABLE parking_spaces
        ADD COLUMN owner_id BIGINT NOT NULL DEFAULT 1;
        
        -- Add foreign key constraint to users table
        ALTER TABLE parking_spaces
        ADD CONSTRAINT fk_parking_owner
        FOREIGN KEY (owner_id) REFERENCES users(id);
        
        -- Remove the default after populating
        ALTER TABLE parking_spaces
        ALTER COLUMN owner_id DROP DEFAULT;
    END IF;
END $$;
