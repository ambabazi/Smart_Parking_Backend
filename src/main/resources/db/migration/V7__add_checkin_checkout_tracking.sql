-- V5: Add check-in/check-out tracking and overtime handling

ALTER TABLE reservations
ADD COLUMN checked_in_at TIMESTAMP NULL,
ADD COLUMN checked_out_at TIMESTAMP NULL,
ADD COLUMN overtime_amount DECIMAL(19,2) DEFAULT 0,
ADD COLUMN status VARCHAR(50) DEFAULT 'ACTIVE';

-- Add index on status for faster queries
CREATE INDEX idx_reservations_status ON reservations(status);
