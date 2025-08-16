-- V2__Create_users_table.sql

CREATE TABLE users (
    id UUID PRIMARY KEY,
    phone_number VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
