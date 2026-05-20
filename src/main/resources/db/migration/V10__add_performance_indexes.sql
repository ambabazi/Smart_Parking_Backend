-- V10: Add performance indexes for read-heavy queries and dashboard aggregation

CREATE INDEX IF NOT EXISTS idx_parking_spaces_owner_id ON parking_spaces (owner_id);
CREATE INDEX IF NOT EXISTS idx_parking_spaces_current_event_id ON parking_spaces (current_event_id);
CREATE INDEX IF NOT EXISTS idx_events_active ON events (active);
CREATE INDEX IF NOT EXISTS idx_events_start_time ON events (start_time);
CREATE INDEX IF NOT EXISTS idx_events_end_time ON events (end_time);
CREATE INDEX IF NOT EXISTS idx_reservations_user_id ON reservations (user_id);
CREATE INDEX IF NOT EXISTS idx_reservations_parking_space_id ON reservations (parking_space_id);
CREATE INDEX IF NOT EXISTS idx_reservations_status ON reservations (status);
CREATE INDEX IF NOT EXISTS idx_reservations_paid ON reservations (paid);
CREATE INDEX IF NOT EXISTS idx_reservations_created_at ON reservations (created_at);
CREATE INDEX IF NOT EXISTS idx_payments_reservation_id ON payments (reservation_id);
CREATE INDEX IF NOT EXISTS idx_payments_flutterwave_id ON payments (flutterwave_id);
