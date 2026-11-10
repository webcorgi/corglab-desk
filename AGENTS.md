# Codex rules
Use Java 21, Spring Boot, Spring Security, Flyway, React, PostgreSQL, Docker Compose.
Do not install BMAD; keep AGENTS.md, docs/PRD.md and docs/TASKS.md updated.
Implement incremental changes with tests, run backend tests, frontend build and container checks before declaring completion.
Never commit .env, credentials, API keys, customer records or private logs.
Production blockers: per-tenant identity mapping and cross-tenant tests; secure session login and CSRF; public intake throttling; consent, retention and deletion; safe upgrades from legacy DB; HTTPS; backup and restore.
Current auth is for local development only. No public service deployment before blockers are resolved.
