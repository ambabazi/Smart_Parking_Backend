ALTER TABLE users
    ADD COLUMN IF NOT EXISTS notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS email_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS sms_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS preferred_language VARCHAR(20) NOT NULL DEFAULT 'en',
    ADD COLUMN IF NOT EXISTS reminder_minutes_before_end INTEGER NOT NULL DEFAULT 10;

UPDATE users
SET notifications_enabled = COALESCE(notifications_enabled, TRUE),
    email_notifications_enabled = COALESCE(email_notifications_enabled, TRUE),
    sms_notifications_enabled = COALESCE(sms_notifications_enabled, TRUE),
    preferred_language = COALESCE(NULLIF(preferred_language, ''), 'en'),
    reminder_minutes_before_end = COALESCE(reminder_minutes_before_end, 10);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(128) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_password_reset_token_user_id ON password_reset_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_password_reset_token_expires_at ON password_reset_tokens (expires_at);

CREATE TABLE IF NOT EXISTS admin_staff_members (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(40),
    staff_role VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_admin_staff_role ON admin_staff_members (staff_role);