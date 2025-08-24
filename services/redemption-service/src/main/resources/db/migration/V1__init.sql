
CREATE TABLE qrs (
    id UUID PRIMARY KEY,
    station_id VARCHAR(255) NOT NULL,
    dispenser_code VARCHAR(255) NOT NULL,
    nonce VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE,
    signature_alg VARCHAR(50),
    signature TEXT
);

CREATE TABLE points (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    amount INTEGER NOT NULL,
    source_transaction_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_qrs_nonce ON qrs(nonce);
