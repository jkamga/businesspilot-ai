ALTER TABLE organizations
    ADD COLUMN IF NOT EXISTS system_prompt TEXT;
