-- Ajout du provider d'authentification sur les utilisateurs
-- Permet Google OAuth2 en plus de l'email/password classique

CREATE TYPE auth_provider AS ENUM ('EMAIL', 'GOOGLE');

ALTER TABLE users
    ADD COLUMN provider auth_provider NOT NULL DEFAULT 'EMAIL',
    ALTER COLUMN password_hash DROP NOT NULL;

CREATE INDEX idx_users_provider ON users(provider);
