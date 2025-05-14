-- Enable UUID and enum support
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Define enum types
CREATE TYPE role_type AS ENUM ('USER', 'ADMIN', 'MODERATOR');
CREATE TYPE auth_provider AS ENUM ('LOCAL', 'GOOGLE', 'FACEBOOK');

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    roles role_type[] NOT NULL,
    auth_provider auth_provider NOT NULL,
    is_verified BOOLEAN NOT NULL
);