
CREATE TABLE raffles (
    id UUID PRIMARY KEY,
    period VARCHAR(255) NOT NULL,
    merkle_root VARCHAR(255),
    seed_source VARCHAR(255),
    seed_value VARCHAR(255),
    drawn_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE raffle_entries (
    id UUID PRIMARY KEY,
    raffle_id UUID REFERENCES raffles(id),
    point_id UUID NOT NULL -- Assuming points have UUIDs
);

CREATE TABLE raffle_winners (
    id UUID PRIMARY KEY,
    raffle_id UUID REFERENCES raffles(id),
    user_id UUID NOT NULL,
    point_id UUID NOT NULL,
    picked_index BIGINT
);
