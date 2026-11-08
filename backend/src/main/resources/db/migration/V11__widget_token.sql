ALTER TABLE organizations
    ADD COLUMN IF NOT EXISTS widget_token UUID DEFAULT gen_random_uuid() NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_organizations_widget_token ON organizations(widget_token);
