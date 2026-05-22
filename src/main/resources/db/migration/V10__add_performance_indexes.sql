-- V10: Performance indexes (idempotent, only when columns exist)

CREATE INDEX IF NOT EXISTS idx_parking_spaces_owner_id ON parking_spaces (owner_id);
CREATE INDEX IF NOT EXISTS idx_parking_spaces_current_event_id ON parking_spaces (current_event_id);

DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'events' AND column_name = 'active') THEN
    EXECUTE 'CREATE INDEX IF NOT EXISTS idx_events_active ON events (active)';
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'events' AND column_name = 'start_time') THEN
    EXECUTE 'CREATE INDEX IF NOT EXISTS idx_events_start_time ON events (start_time)';
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'events' AND column_name = 'end_time') THEN
    EXECUTE 'CREATE INDEX IF NOT EXISTS idx_events_end_time ON events (end_time)';
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'reservations' AND column_name = 'user_id') THEN
    EXECUTE 'CREATE INDEX IF NOT EXISTS idx_reservations_user_id ON reservations (user_id)';
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'reservations' AND column_name = 'parking_space_id') THEN
    EXECUTE 'CREATE INDEX IF NOT EXISTS idx_reservations_parking_space_id ON reservations (parking_space_id)';
  ELSIF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'reservations' AND column_name = 'parking_id') THEN
    EXECUTE 'CREATE INDEX IF NOT EXISTS idx_reservations_parking_id ON reservations (parking_id)';
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'reservations' AND column_name = 'status') THEN
    EXECUTE 'CREATE INDEX IF NOT EXISTS idx_reservations_status ON reservations (status)';
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'reservations' AND column_name = 'paid') THEN
    EXECUTE 'CREATE INDEX IF NOT EXISTS idx_reservations_paid ON reservations (paid)';
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'reservations' AND column_name = 'created_at') THEN
    EXECUTE 'CREATE INDEX IF NOT EXISTS idx_reservations_created_at ON reservations (created_at)';
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payments' AND column_name = 'reservation_id') THEN
    EXECUTE 'CREATE INDEX IF NOT EXISTS idx_payments_reservation_id ON payments (reservation_id)';
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payments' AND column_name = 'flutterwave_id') THEN
    EXECUTE 'CREATE INDEX IF NOT EXISTS idx_payments_flutterwave_id ON payments (flutterwave_id)';
  END IF;
END $$;
