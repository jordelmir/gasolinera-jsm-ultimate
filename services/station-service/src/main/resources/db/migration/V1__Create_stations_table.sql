-- Create stations table
CREATE TABLE IF NOT EXISTS stations (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index for geographic queries
CREATE INDEX IF NOT EXISTS idx_stations_location ON stations (latitude, longitude);

-- Create index for status queries
CREATE INDEX IF NOT EXISTS idx_stations_status ON stations (status);

-- Create index for name searches
CREATE INDEX IF NOT EXISTS idx_stations_name ON stations (name);

-- Insert sample data
INSERT INTO stations (id, name, latitude, longitude, status) VALUES
('stn_001', 'Gasolinera Central San José', 9.9281, -84.0907, 'ACTIVE'),
('stn_002', 'Gasolinera Norte Heredia', 9.9981, -84.1169, 'ACTIVE'),
('stn_003', 'Gasolinera Sur Cartago', 9.8644, -83.9186, 'ACTIVE'),
('stn_004', 'Gasolinera Oeste Alajuela', 10.0162, -84.2119, 'ACTIVE'),
('stn_005', 'Gasolinera Este Limón', 9.9908, -83.0251, 'INACTIVE')
ON CONFLICT (id) DO NOTHING;