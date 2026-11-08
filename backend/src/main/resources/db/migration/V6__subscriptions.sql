CREATE TABLE plans (
    id                    UUID           DEFAULT gen_random_uuid() PRIMARY KEY,
    name                  VARCHAR(50)    NOT NULL,
    slug                  VARCHAR(50)    NOT NULL UNIQUE,
    price_monthly         NUMERIC(10,2),
    price_yearly          NUMERIC(10,2),
    max_prospects         INTEGER        NOT NULL DEFAULT -1,
    max_conversations     INTEGER        NOT NULL DEFAULT -1,
    max_knowledge_entries INTEGER        NOT NULL DEFAULT -1,
    max_users             INTEGER        NOT NULL DEFAULT -1,
    features              JSONB          NOT NULL DEFAULT '[]',
    is_active             BOOLEAN        NOT NULL DEFAULT TRUE,
    sort_order            INTEGER        NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE TABLE subscriptions (
    id                    UUID        DEFAULT gen_random_uuid() PRIMARY KEY,
    organization_id       UUID        NOT NULL UNIQUE REFERENCES organizations(id) ON DELETE CASCADE,
    plan_id               UUID        NOT NULL REFERENCES plans(id),
    status                VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    billing_cycle         VARCHAR(10) NOT NULL DEFAULT 'MONTHLY',
    current_period_start  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    current_period_end    TIMESTAMPTZ,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Seed plans
INSERT INTO plans (name, slug, price_monthly, price_yearly, max_prospects, max_conversations, max_knowledge_entries, max_users, features, sort_order)
VALUES
  ('Gratuit',        'free',         0,    0,    50,   10,   10,  1,
   '["50 prospects","10 conversations / mois","10 entrées base de connaissances","1 utilisateur"]'::jsonb, 1),
  ('Démarrage',      'starter',      29,   290,  500,  100,  50,  3,
   '["500 prospects","100 conversations / mois","50 entrées KB","3 utilisateurs","Support par email"]'::jsonb, 2),
  ('Professionnel',  'professional', 79,   790,  -1,   -1,   -1,  10,
   '["Prospects illimités","Conversations illimitées","KB illimitée","10 utilisateurs","Support prioritaire","Export des données"]'::jsonb, 3),
  ('Entreprise',     'enterprise',   NULL, NULL, -1,   -1,   -1,  -1,
   '["Tout illimité","Utilisateurs illimités","SLA garanti","Support dédié","Intégrations personnalisées","Facturation sur mesure"]'::jsonb, 4);
