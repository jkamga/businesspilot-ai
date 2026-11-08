-- Module Base de connaissances
-- Stocke le contenu métier de chaque organisation pour le RAG

CREATE TYPE entry_type AS ENUM ('TEXT', 'URL', 'FAQ');
CREATE TYPE entry_status AS ENUM ('DRAFT', 'ACTIVE', 'ARCHIVED');

CREATE TABLE knowledge_entries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    title           VARCHAR(255) NOT NULL,
    content         TEXT NOT NULL,
    type            entry_type NOT NULL DEFAULT 'TEXT',
    status          entry_status NOT NULL DEFAULT 'DRAFT',
    word_count      INTEGER NOT NULL DEFAULT 0,
    search_vector   TSVECTOR,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ke_organization_id ON knowledge_entries(organization_id);
CREATE INDEX idx_ke_organization_status ON knowledge_entries(organization_id, status);
CREATE INDEX idx_ke_search_vector ON knowledge_entries USING GIN(search_vector);

-- Trigger pour maintenir le vecteur de recherche à jour
CREATE OR REPLACE FUNCTION knowledge_entries_search_vector_update()
RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector := to_tsvector('french', coalesce(NEW.title, '') || ' ' || coalesce(NEW.content, ''));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER knowledge_entries_search_vector_trigger
    BEFORE INSERT OR UPDATE ON knowledge_entries
    FOR EACH ROW EXECUTE FUNCTION knowledge_entries_search_vector_update();
