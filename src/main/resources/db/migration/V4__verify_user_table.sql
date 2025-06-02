CREATE TABLE verify_user (
    token UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    expiry BIGINT NOT NULL,
    CONSTRAINT fk_user_verification
        FOREIGN KEY (email)
        REFERENCES users(email)
        ON DELETE CASCADE
);
