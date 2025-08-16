-- V2__Create_raffles_table.sql

CREATE TABLE raffles (
    id UUID PRIMARY KEY,
    period VARCHAR(100) NOT NULL,
    merkle_root VARCHAR(256) NOT NULL,
    status VARCHAR(50) NOT NULL, -- e.g., 'CREATED', 'DRAWN'
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    drawn_at TIMESTAMPTZ
);
