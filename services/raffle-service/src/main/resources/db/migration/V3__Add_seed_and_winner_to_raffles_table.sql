-- V3__Add_seed_and_winner_to_raffles_table.sql

ALTER TABLE raffles
ADD COLUMN seed_source VARCHAR(255),
ADD COLUMN seed_value VARCHAR(255),
ADD COLUMN winner_point_id VARCHAR(255);
