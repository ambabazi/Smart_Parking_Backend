CREATE TABLE IF NOT EXISTS parking_spaces
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(255) NOT NULL,
    address          VARCHAR(255),
    latitude         DOUBLE       NOT NULL,
    longitude        DOUBLE       NOT NULL,
    total_slots      INT          NOT NULL,
    available_slots  INT          NOT NULL,
    price_per_slot   DOUBLE       NOT NULL,
    event_enabled    BOOLEAN      NOT NULL DEFAULT FALSE,
    current_event_id BIGINT,
    FOREIGN KEY (current_event_id) REFERENCES events (id)
    );