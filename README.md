# BusinessPilot AI

Plateforme SaaS B2B d'automatisation commerciale pour PME — Québec & Afrique francophone.

## Stack technique

| Couche | Technologie |
|---|---|
| Backend | Spring Boot 4.x (Java 25) |
| Frontend | Angular 22 |
| Base de données | PostgreSQL 16 + Flyway |
| Auth | JWT (stateless) |
| API Docs | Swagger / OpenAPI 3 |
| Infra | Docker Compose + Nginx |

## Prérequis

- Java 25
- Gradle 8.x (ou utiliser le wrapper `./gradlew`)
- Node 22+ et npm 10+
- Angular CLI 22 (`npm install -g @angular/cli@22`)
- Docker et Docker Compose v2

## Démarrage en développement

### 1. Variables d'environnement

```bash
cp docker/.env.example docker/.env
# Éditer docker/.env avec vos valeurs locales
```

### 2. Lancer PostgreSQL

```bash
docker compose -f docker/docker-compose.yml up -d postgres
```

### 3. Lancer le backend

```bash
cd backend
./gradlew bootRun
# API disponible sur http://localhost:8080
# Swagger : http://localhost:8080/swagger-ui.html
```

### 4. Lancer le frontend

```bash
cd frontend
npm install
ng serve
# App disponible sur http://localhost:4200
```

## Structure des modules backend

```
com.businesspilot
├── common/           # Sécurité, tenant, entités de base, exceptions
├── module/
│   ├── auth/         # Authentification JWT
│   ├── organization/ # Organisations et multi-tenancy
│   ├── knowledge/    # Base de connaissances RAG
│   ├── ai/           # Assistant IA
│   ├── conversation/ # Conversations avec prospects
│   ├── prospect/     # Mini-CRM
│   ├── subscription/ # Abonnements et plans
│   ├── dashboard/    # Statistiques
│   └── admin/        # Administration SaaS
```

## Multi-tenancy

Chaque organisation est isolée via `organization_id` sur toutes les tables.
Le `TenantContext` est initialisé par le filtre de sécurité à chaque requête.
Aucune donnée d'une organisation n'est accessible par une autre.

## Variables d'environnement clés

| Variable | Description |
|---|---|
| `DB_URL` | JDBC URL PostgreSQL |
| `DB_USERNAME` | Utilisateur PostgreSQL |
| `DB_PASSWORD` | Mot de passe PostgreSQL |
| `JWT_SECRET` | Secret JWT (min 256 bits) |
| `JWT_EXPIRY_MS` | Durée du token en ms (défaut : 86400000) |
| `AI_PROVIDER_API_KEY` | Clé API du fournisseur LLM |
