-- Create raffles table
CREATE TABLE raffles (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    max_participants INTEGER NOT NULL DEFAULT 1000,
    prize_description TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    merkle_root VARCHAR(64),
    external_seed VARCHAR(255),
    winner_entry_id VARCHAR(36),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create raffle_participants table
CREATE TABLE raffle_participants (
    id VARCHAR(36) PRIMARY KEY,
    raffle_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    participation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    eligibility_proof TEXT NOT NULL,
    entry_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_participant_raffle FOREIGN KEY (raffle_id) REFERENCES raffles(id) ON DELETE CASCADE,
    CONSTRAINT uk_raffle_user UNIQUE (raffle_id, user_id)
);

-- Create raffle_winners table
CREATE TABLE raffle_winners (
    id VARCHAR(36) PRIMARY KEY,
    raffle_id VARCHAR(36) NOT NULL,
    participant_id VARCHAR(36) NOT NULL,
    selection_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    merkle_proof JSONB NOT NULL DEFAULT '[]',
    external_seed VARCHAR(255) NOT NULL,
    selection_index INTEGER NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    prize_claimed BOOLEAN NOT NULL DEFAULT FALSE,
    claim_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_winner_raffle FOREIGN KEY (raffle_id) REFERENCES raffles(id) ON DELETE CASCADE,
    CONSTRAINT fk_winner_participant FOREIGN KEY (participant_id) REFERENCES raffle_participants(id) ON DELETE CASCADE,
    CONSTRAINT uk_raffle_winner UNIQUE (raffle_id)
);

-- Create indexes for performance
CREATE INDEX idx_raffles_status ON raffles(status);
CREATE INDEX idx_raffles_dates ON raffles(start_date, end_date);
CREATE INDEX idx_raffles_created_at ON raffles(created_at);

CREATE INDEX idx_participants_raffle ON raffle_participants(raffle_id);
CREATE INDEX idx_participants_user ON raffle_participants(user_id);
CREATE INDEX idx_participants_date ON raffle_participants(participation_date);
CREATE INDEX idx_participants_hash ON raffle_participants(entry_hash);

CREATE INDEX idx_winners_raffle ON raffle_winners(raffle_id);
CREATE INDEX idx_winners_participant ON raffle_winners(participant_id);
CREATE INDEX idx_winners_verified ON raffle_winners(verified);
CREATE INDEX idx_winners_claimed ON raffle_winners(prize_claimed);
CREATE INDEX idx_winners_selection_date ON raffle_winners(selection_date);

-- Add comments for documentation
COMMENT ON TABLE raffles IS 'Stores raffle/lottery information';
COMMENT ON TABLE raffle_participants IS 'Stores user participation in raffles';
COMMENT ON TABLE raffle_winners IS 'Stores raffle winners and prize claim information';

COMMENT ON COLUMN raffles.merkle_root IS 'Merkle tree root hash for transparency';
COMMENT ON COLUMN raffles.external_seed IS 'External randomness seed (e.g., Bitcoin block hash)';
COMMENT ON COLUMN raffle_participants.entry_hash IS 'Hash of participant entry for Merkle tree';
COMMENT ON COLUMN raffle_winners.merkle_proof IS 'JSON array of Merkle proof hashes';
COMMENT ON COLUMN raffle_winners.selection_index IS 'Index used for deterministic winner selection';