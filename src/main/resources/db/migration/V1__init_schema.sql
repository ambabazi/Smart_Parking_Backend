-- V1__init_schema.sql
-- Creates all tables for Smart Parking MVP

CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    full_name   VARCHAR(100)        NOT NULL,
    email       VARCHAR(150) UNIQUE NOT NULL,
    phone       VARCHAR(20)  UNIQUE NOT NULL,
    password    VARCHAR(255)        NOT NULL,
    role        VARCHAR(20)         NOT NULL DEFAULT 'DRIVER',
    created_at  TIMESTAMP           NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS events (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(150)   NOT NULL,
    venue           VARCHAR(255)   NOT NULL,
    latitude        DECIMAL(10,8)  NOT NULL,
    longitude       DECIMAL(11,8)  NOT NULL,
    event_date      TIMESTAMP      NOT NULL,
    radius_km       DECIMAL(5,2)   NOT NULL DEFAULT 1.0,
    is_active       BOOLEAN        NOT NULL DEFAULT FALSE,
    price_multiplier DECIMAL(4,2)  NOT NULL DEFAULT 2.0,
    created_at      TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS parking_spaces (
    id              BIGSERIAL PRIMARY KEY,
    host_id         BIGINT REFERENCES users(id),
    name            VARCHAR(100)   NOT NULL,
    address         VARCHAR(255)   NOT NULL,
    latitude        DECIMAL(10,8)  NOT NULL,
    longitude       DECIMAL(11,8)  NOT NULL,
    total_slots     INT            NOT NULL DEFAULT 1,
    available_slots INT            NOT NULL DEFAULT 1,
    price_per_slot  DECIMAL(10,2)  NOT NULL DEFAULT 500,
    event_enabled   BOOLEAN        NOT NULL DEFAULT FALSE,
    current_event_id BIGINT REFERENCES events(id),
    created_at      TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS reservations (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT REFERENCES users(id)         NOT NULL,
    parking_id      BIGINT REFERENCES parking_spaces(id) NOT NULL,
    slots_booked    INT            NOT NULL DEFAULT 1,
    start_time      TIMESTAMP      NOT NULL,
    end_time        TIMESTAMP      NOT NULL,
    actual_end_time TIMESTAMP,
    status          VARCHAR(30)    NOT NULL DEFAULT 'PENDING',
    qr_code         TEXT,
    total_amount    DECIMAL(10,2)  NOT NULL,
    created_at      TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS payments (
    id              BIGSERIAL PRIMARY KEY,
    reservation_id  BIGINT REFERENCES reservations(id) NOT NULL,
    amount          DECIMAL(10,2)  NOT NULL,
    currency        VARCHAR(10)    NOT NULL DEFAULT 'RWF',
    status          VARCHAR(30)    NOT NULL DEFAULT 'PENDING',
    tx_ref          VARCHAR(100)   UNIQUE NOT NULL,
    flutterwave_id  VARCHAR(100),
    paid_at         TIMESTAMP,
    created_at      TIMESTAMP      NOT NULL DEFAULT NOW()
);
