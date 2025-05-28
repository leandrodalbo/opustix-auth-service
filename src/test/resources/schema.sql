-- Enable UUID and enum support
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    roles VARCHAR(255) NOT NULL,
    auth_provider VARCHAR(100) NOT NULL,
    is_verified BOOLEAN NOT NULL,
    refresh_token UUID
);