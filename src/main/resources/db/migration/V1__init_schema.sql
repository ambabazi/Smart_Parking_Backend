CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(200) NOT NULL,
  role VARCHAR(50) NOT NULL
);

CREATE TABLE parking_space (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(200),
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  available_slots INTEGER
);

CREATE TABLE reservation (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT REFERENCES users(id),
  parking_space_id BIGINT REFERENCES parking_space(id),
  slot_count INTEGER,
  qr_code TEXT
);

CREATE TABLE event (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(200),
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  radius DOUBLE PRECISION
);

CREATE TABLE payment (
  id BIGSERIAL PRIMARY KEY,
  transaction_id VARCHAR(200),
  status VARCHAR(100),
  amount DECIMAL(10,2)
);
