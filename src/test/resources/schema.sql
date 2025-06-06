-- Enable UUID and enum support
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    roles VARCHAR(255) NOT NULL,
    auth_providers VARCHAR(100) NOT NULL,
    is_verified BOOLEAN NOT NULL,
    refresh_token UUID,
    password_reset_token UUID,
    password_reset_token_expiry BIGINT
);

CREATE TABLE verify_user (
    token UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    expiry BIGINT NOT NULL,
    CONSTRAINT fk_user_verification
        FOREIGN KEY (email)
        REFERENCES users(email)
        ON DELETE CASCADE
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token UUID NOT NULL UNIQUE,
    expiry BIGINT NOT NULL,
    user_id UUID NOT NULL,

    CONSTRAINT fk_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);
