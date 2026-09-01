# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Rules

- Use Context7 MCP (`mcp__context7__resolve-library-id` + `mcp__context7__query-docs`) to fetch current documentation for any library or package used in this project (Spring Boot, Angular, etc.) before answering questions or making changes involving them.

- Only stage, commit, or push when explicitly asked to (including via the "Create PR" flow). Never do so on your own initiative, and never rewrite published history (`git commit --amend`, `git rebase`, or force-push on a pushed branch) unless explicitly asked.

## Project Overview

**Worldwide Walter** is a multiplayer web-based party game where players submit answers to prompts and try to deceive others into selecting their answer as coming from the "Sphinx" (a rotating role). Built as a monorepo with a Spring Boot 3 backend and Angular 16 frontend.

## Commands

### Full Build (Backend + Frontend)
```bash
mvn clean install              # Prod build (default profile)
mvn -P dev clean package       # Dev build (frontend points at localhost:8080)
mvn clean test package         # Run tests then package (CI default)
```

### Backend Only
```bash
cd backend && mvn clean test   # Run backend tests
cd backend && mvn clean package -DskipTests  # Package without tests
```

### Frontend Only
```bash
cd frontend && npm run start        # Dev server on localhost:4200
cd frontend && npm run test         # Run Karma tests (interactive)
cd frontend && npm run test-headless  # Run tests once (headless Chrome)
cd frontend && npm run build-dev    # Dev build
cd frontend && npm run build        # Production build
```

### Docker
```bash
docker-compose up --build -d   # Start app (port 8080)
docker-compose down            # Stop app
```

### API Documentation
Swagger UI is available at `http://localhost:8080/swagger-ui.html` when the backend is running.

### API contract (OpenAPI)
The file `openapi.json` at the repo root is the **single source of truth** for the REST API.
It is committed and guarded by `OpenApiSpecTest` (backend), which fails `mvn test` whenever it
drifts from the live controllers/DTOs.

- **Regenerate after changing a controller signature or a `dto/` class:**
  ```bash
  cd backend && mvn -Dopenapi.generate=true test -Dtest=OpenApiSpecTest
  ```
  then commit the updated `openapi.json`.
- The frontend generates its models **and** typed Angular HTTP services from `openapi.json`
  into `frontend/src/app/api/` (git-ignored). This runs automatically via the `pre*` npm
  hooks before `start` / `build` / `build-dev` / `test-headless`; run it by hand with
  `cd frontend && npm run generate:api`. Config: `frontend/openapitools.json`.
- `frontend/src/app/service/game.service.ts` is a thin facade over the generated services
  (`GameControllerService` / `RoundControllerService`) — component call sites stay unchanged.
- `openapi-generator-cli` needs a JVM on `PATH` (already present in every Maven build).

## Architecture

### Backend (Spring Boot 3, Java 17)

Layered MVC: **Controller → Service → Repository → Model**

```
ch.zhaw.www/
  controller/   GameController, RoundController (REST endpoints, CORS configured)
  service/      GameService, RoundService, EntityService, EvaluationService, CleanUpService
  model/        Game, Round, Player, Proposition, Prompt
  repository/   GameRepository (Spring Data KeyValue, in-memory), PromptRepository
  dto/          API request/response contracts (source of the OpenAPI schemas)
  utils/        RandomProvider, InstantWrapper, GameTransaction, RoundTransaction
  exception/    GameError, RoundError, PlayerError, PropositionError hierarchy
```

**Key behaviors:**
- All state is held **in-memory** (Spring Data KeyValue — no database)
- `CleanUpService` removes games idle > 30 min (runs every 30 min via `@EnableScheduling`)
- Player identity is passed via `X-PLAYER-ID` request header
- `EntityService` coordinates transactional updates across `GameService` and `RoundService`
- `EvaluationService` computes points after each round's selection phase

**Game flow:**
1. Create game → join game → poll for round open → submit proposition → select proposition → view results → repeat

**Configuration** (`backend/src/main/resources/application.properties`):
- `round.proposition-submission-duration=5m`
- `round.selection-submission-duration=1m`
- `game.minimum-players=4`, `game.maximum-players=12`

### Frontend (Angular 16, TypeScript)

```
src/app/
  components/
    initialization-container/   WelcomeComponent, JoinComponent
    game-container/             RoundComponent, ResultContainerComponent,
                                CurrentGameInfoComponent, CountDownComponent, etc.
  service/    GameService (HTTP), StateService (app state), LoadingService, AppConfigService
  model/      GameState enum (drives UI routing), AppState, InitializationState
  dto/        Mirrors backend DTOs
  interceptor/ HttpPollingInterceptor
```

**Key behaviors:**
- No Angular Router — `GameState` enum drives which container is shown
- Frontend **polls** the backend for state changes (no WebSockets)
- `StateService` is the single source of truth for player ID, game ID, and current state
- On page unload, Beacon API is used to gracefully leave the game
- URL param `?gameId=xyz` enables direct game joining

### Testing

- **Backend**: JUnit + Mockito, Jacoco enforces **50% minimum coverage**. Test helpers in `TestHelper` and `TimeHelper`.
- **Frontend**: Jasmine + Karma. Run headless for CI.
- Two-reviewer PRs required (one junior, one senior). All public methods need Javadoc (except getters/setters).
