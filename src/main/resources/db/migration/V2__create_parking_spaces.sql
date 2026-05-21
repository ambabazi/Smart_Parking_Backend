-- PostgreSQL-compatible parking spaces table (matches production schema)
CREATE TABLE IF NOT EXISTS parking_spaces
(
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(255) NOT NULL,
    address          VARCHAR(255),
    latitude         DOUBLE PRECISION NOT NULL,
    longitude        DOUBLE PRECISION NOT NULL,
    total_slots      INT          NOT NULL,
    available_slots  INT          NOT NULL,
    price_per_slot   DOUBLE PRECISION NOT NULL,
    event_enabled    BOOLEAN      NOT NULL DEFAULT FALSE,
    current_event_id BIGINT REFERENCES events (id)
);
