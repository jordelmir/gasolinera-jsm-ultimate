-- V1__Create_users_table.sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    phone VARCHAR(255) NOT NULL UNIQUE
);
