# Repository Guidelines

## Project Structure & Module Organization
This Turborepo hosts a Spring Boot OAuth server in `backend/`, a Next.js 14 client in `frontend/`, and shared React primitives under `frontend/packages/ui`. Keep backend packages aligned to domains (`config`, `auth`, `common`, `social`, `user`); concentrate orchestration in services and expose request/response shapes through `auth.dto`. Frontend routes live in the App Router (`src/app`), with `/login`, `/register`, and `/oauth/callback` handling onboarding flows. Reuse `src/lib/api-client.ts` and `token-storage.ts` for token-aware requests instead of rolling new fetch helpers.

## Build, Test, and Development Commands
Install dependencies once with `pnpm install`, then use `pnpm dev` for the turbo-managed pipeline or `pnpm --filter @repo/frontend dev` to focus on the Next.js app. Shared UI builds with `pnpm --filter @repo/ui build`. Start the backend via `./gradlew bootRun` (wrapper preferred), produce artifacts with `./gradlew build`, and execute tests with `./gradlew test`. Lint and format by running `pnpm lint`, `pnpm format`, or scoped equivalents such as `pnpm --filter @repo/frontend lint`.

## Coding Style & Naming Conventions
Frontend code follows TypeScript, ESLint (Next config), and Prettier 3 defaults. Name React components and hooks with PascalCase files, keep presentation logic inside components, and compose Tailwind utility classes defined in `tailwind.config.ts`. Shared UI modules must export from `frontend/packages/ui/src/index.ts` and accept explicit prop types. Backend code targets Java 17 with Lombok; favor constructor injection, keep controllers slim, and return the shared `ApiResponse<T>` envelope from public APIs.

## Testing Guidelines
House JUnit 5 tests under `backend/src/test/java`, matching the package you cover and naming classes `*Test`. Use transactional tests when touching JPA repositories and add focused coverage for security filters and JWT helpers. The frontend currently lacks automated suites; when you add them, adopt React Testing Library or Playwright and ensure they run alongside `./gradlew test` before merging.

## Commit & PR Guidelines
Compose commits in the imperative mood (e.g., "Add Kakao profile sync"), scoped to a single concern. Confirm linting, formatting, and test commands pass locally, call out configuration or migration steps in the description, and attach screenshots or screencasts for UX-facing updates. Reference related issues and list any manual verification done.

## Security & Configuration Tips
Store secrets in environment variables or `backend/src/main/resources/secret.yaml`, which `application.yml` already imports. Keep `JWT_SECRET`, OAuth credentials, and `NEXT_PUBLIC_API_BASE_URL` consistent across services; adjust `FRONTEND_BASE_URL` to match the host that serves the client. Use `infra/docker-compose.yaml` for local Postgres with persisted volumes, or update Spring datasource settings to point at your own database.
