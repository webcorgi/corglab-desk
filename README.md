# CORGLAB Desk

AI-assisted customer inquiry management MVP. **Development preview only, not production-ready.**

Stack: Java 21 / Spring Boot 3.5 / PostgreSQL 17 / React 19 / Vite / Docker Compose.

## Run locally
1. Copy `.env.example` to `.env` and replace the two secret values.
2. Run `docker compose up --build -d`.
3. Open http://localhost:8080 (bound to localhost only).

Without OPENAI_API_KEY, inquiries are stored with category `unclassified`.
Admin access currently uses a demo API key; **never expose publicly**.

See [PRD](docs/PRD.md), [Tasks](docs/TASKS.md), and [Codex rules](AGENTS.md). The source has not yet passed a runtime integration test.
