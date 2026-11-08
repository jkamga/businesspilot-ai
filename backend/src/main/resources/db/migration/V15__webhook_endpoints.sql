CREATE TABLE webhook_endpoints (
    id              UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    url             VARCHAR(2048) NOT NULL,
    secret          VARCHAR(64)   NOT NULL DEFAULT encode(gen_random_bytes(32), 'hex'),
    events          TEXT[]        NOT NULL DEFAULT '{}',
    active          BOOLEAN       NOT NULL DEFAULT true,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

CREATE INDEX idx_webhook_org ON webhook_endpoints(organization_id);
