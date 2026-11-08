-- Localisation préférée par utilisateur
ALTER TABLE users ADD COLUMN IF NOT EXISTS preferred_locale VARCHAR(5) NOT NULL DEFAULT 'fr';
