-- Support Google (OAuth2) sign-in.
-- Google users authenticate without a phone number or local password, so:
--  1. Relax the NOT NULL on users.phone (Postgres unique indexes allow multiple NULLs).
--  2. Track how the account was created via auth_provider ('LOCAL' or 'GOOGLE').

ALTER TABLE users ALTER COLUMN phone DROP NOT NULL;

ALTER TABLE users ADD COLUMN IF NOT EXISTS auth_provider VARCHAR(20) DEFAULT 'LOCAL';
UPDATE users SET auth_provider = 'LOCAL' WHERE auth_provider IS NULL;
