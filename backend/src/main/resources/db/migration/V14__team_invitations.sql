CREATE TABLE team_invitations (
    id            UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    email         VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    token         UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    invited_by    UUID NOT NULL REFERENCES users(id),
    expires_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    accepted_at   TIMESTAMP WITH TIME ZONE,
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

CREATE INDEX idx_team_inv_token  ON team_invitations(token);
CREATE INDEX idx_team_inv_org    ON team_invitations(organization_id);
CREATE INDEX idx_team_inv_email  ON team_invitations(email);
