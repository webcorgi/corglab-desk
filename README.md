# CORGLAB Desk
AI-powered inquiry management MVP (development preview — **not production-ready**).

## Stack
Java 21, Spring Boot 3.5, Spring Security (temporary HTTP Basic), Flyway, PostgreSQL 17, React 19, Docker Compose.

## Run locally
1. Copy `.env.example` to `.env` and change the database password, admin username and 16+ character admin password.
2. `docker compose up --build -d`
3. Open http://localhost:8080 (only accessible on localhost).

Admin login uses temporary stateless HTTP Basic, and is **not designed for public hosting**. Never use the system to collect real customer information on a public server until HTTPS, production authentication and privacy controls are in place.

The schema has a tenant ID and filters every inquiry query by the configured tenant. It **does not yet support multiple customer organizations**. The Flyway V1 migration is intended for an empty database; existing legacy data needs a migration plan.

When the API key is absent, inquiries remain `unclassified`. Build and integration tests have not yet been confirmed.

See [PRD](docs/PRD.md), [Tasks](docs/TASKS.md) and [Codex rules](AGENTS.md).
