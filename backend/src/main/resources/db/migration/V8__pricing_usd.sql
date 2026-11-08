-- Migration vers grille tarifaire USD unique (global)
-- Starter : 29 CAD/mois → 10 USD/mois  (96 USD/an — économie 2 mois)
-- Professional : 79 CAD/mois → 25 USD/mois (240 USD/an — économie 2 mois)

UPDATE plans SET
    price_monthly = 10.00,
    price_yearly  = 96.00,
    features = '["500 prospects","100 conversations / mois","50 entrées KB","3 utilisateurs","Support par email"]'::jsonb
WHERE slug = 'starter';

UPDATE plans SET
    price_monthly = 25.00,
    price_yearly  = 240.00
WHERE slug = 'professional';

UPDATE plans SET
    price_monthly = 0.00,
    price_yearly  = 0.00
WHERE slug = 'free';
