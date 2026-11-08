CREATE TABLE prospects (
    id              UUID         DEFAULT gen_random_uuid() PRIMARY KEY,
    organization_id UUID         NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    first_name      VARCHAR(100),
    last_name       VARCHAR(100),
    email           VARCHAR(255),
    phone           VARCHAR(50),
    company         VARCHAR(255),
    status          VARCHAR(30)  NOT NULL DEFAULT 'LEAD',
    notes           TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE interactions (
    id              UUID        DEFAULT gen_random_uuid() PRIMARY KEY,
    prospect_id     UUID        NOT NULL REFERENCES prospects(id) ON DELETE CASCADE,
    organization_id UUID        NOT NULL,
    type            VARCHAR(20) NOT NULL,
    content         TEXT        NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_prospects_org        ON prospects(organization_id);
CREATE INDEX idx_prospects_org_status ON prospects(organization_id, status);
CREATE INDEX idx_interactions_prospect ON interactions(prospect_id);
CREATE INDEX idx_interactions_org     ON interactions(organization_id);
