CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(200) NOT NULL,
  role VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS parking_space (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(200),
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  available_slots INTEGER
);

CREATE TABLE IF NOT EXISTS reservation (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT REFERENCES users(id),
  parking_space_id BIGINT REFERENCES parking_space(id),
  slot_count INTEGER,
  qr_code TEXT
);
