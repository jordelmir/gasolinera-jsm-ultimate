
-- V1__init.sql - Initial schema for auth-service

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone_number VARCHAR(255) UNIQUE NOT NULL,
    roles TEXT[] NOT NULL DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Create index for phone number lookups
CREATE INDEX idx_users_phone_number ON users(phone_number);

-- Insert default admin user
INSERT INTO users (phone_number, roles) VALUES
('admin@puntog.com', ARRAY['ADMIN']),
('anunciante@tosty.com', ARRAY['ADVERTISER']);
