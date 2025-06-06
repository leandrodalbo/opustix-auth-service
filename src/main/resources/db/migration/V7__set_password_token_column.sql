ALTER TABLE users
ADD COLUMN password_reset_token UUID,
ADD COLUMN password_reset_token_expiry BIGINT;