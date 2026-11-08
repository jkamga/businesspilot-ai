-- WhatsApp : identifiant du numéro Meta par organisation
ALTER TABLE organizations
    ADD COLUMN whatsapp_phone_number_id VARCHAR(50),
    ADD COLUMN stripe_customer_id       VARCHAR(255);

-- Canal de la conversation (WEB ou WHATSAPP)
ALTER TABLE conversations
    ADD COLUMN whatsapp_phone VARCHAR(50),
    ADD COLUMN source         VARCHAR(20) NOT NULL DEFAULT 'WEB';

-- Canal et ID externe du message (wamid WhatsApp pour déduplication)
ALTER TABLE messages
    ADD COLUMN source      VARCHAR(20) NOT NULL DEFAULT 'WEB',
    ADD COLUMN external_id VARCHAR(255);

CREATE UNIQUE INDEX idx_messages_external_id ON messages(external_id) WHERE external_id IS NOT NULL;

-- Stripe : identifiant abonnement Stripe
ALTER TABLE subscriptions
    ADD COLUMN stripe_subscription_id VARCHAR(255),
    ADD COLUMN stripe_customer_id     VARCHAR(255);
