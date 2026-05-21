# AGENTS.md – ccc-service

## Project

Java 25 / Spring Boot 3.5 WebSocket relay service. Connects a "GIS" client (map viewer) with an "App" client (business application) via paired WebSocket sessions on `/ccc-service`.

## Build & Test Commands

| Goal | Command |
|---|---|
| Unit tests (fast, no Spring ctx) | `./gradlew test` |
| Integration tests (Spring Boot + real WebSocket) | `./gradlew integrationTest` |
| Smoke tests against deployed service | `./gradlew smokeTest -Dccc.smoke.url=wss://host/ccc-service` |
| Static analysis (ErrorProne + SpotBugs) | `./gradlew spotbugsMain spotbugsTest` |
| Assemble JAR | `./gradlew assemble` |
| Build + run local Docker image | `./gradlew runImage` |
| Remove local test container | `./gradlew removeTestContainer` |

- All Gradle tasks run in the repo root.
- `test` excludes tags `integration` and `smoke`.
- `integrationTest` includes only `integration`.
- `smokeTest` includes only `smoke`; falls back to `ccc.smoke.url` in `application.properties` if no system property given.

## Test Structure

- **Fast tests** use `MockWebSocketSession` (in `src/test/java/.../session/`) and run completely without Spring.
- **Integration tests** annotated with `@Tag("integration")` spin up the full Spring context.
- **Smoke tests** annotated with `@Tag("smoke")` require a running service endpoint.
- Minimum coverage requirement: 80%.

## Entrypoints & Architecture

- **Spring Boot entry:** `ch.so.agi.cccservice.Application`
- **WebSocket handler:** `CCCWebSocketHandler` registered on `/ccc-service`
- **Message flow:**
  1. `CCCWebSocketHandler.handleTextMessage()`
  2. `MessageAccumulator.accumulate()` (handles fragmented frames)
  3. `MessageHandler.handleMessage()` -> `Message.forJsonString()` (factory) -> `message.process()`
- **Session registry:** `Sessions` is a **static** `ConcurrentHashMap`; each `Session` pairs one App `SockConnection` with one GIS `SockConnection`.
- **Routing:** messages are forwarded raw to the peer connection; no re-serialization except the `apiVersion` filter (`Message.rawMessageForApp`).

## Versioning & Release

- Base version in `gradle.properties` (`cccVersion`).
- Local builds produce `<cccVersion>.localbuild`.
- CI builds produce `<cccVersion>.<GITHUB_RUN_NUMBER>`.
- Release builds use `RELEASE_TAG` env var; tag must match `cccVersion` exactly or with `b<N>` suffix (e.g. `1.2.3b2`). Mismatched tags fail the build.
- Release workflow triggers on GitHub release "published" event.

## Docker / Local Runtime

- Dockerfile is in `docker/`; JAR is copied there by `./gradlew jardist`.
- `./gradlew runImage` builds image `sogis/ccc-service:latest`, starts container `ccctest` on port 8080 with `CCC_DEBUG=1`.
- Health endpoint: `http://localhost:8080/actuator/health`
- Status page (session overview): `http://localhost:8080/`
- Debug logging profile: `application-ccc-debug.properties`; activated via `CCC_DEBUG=1` or `--spring.profiles.active=ccc-debug`.

## Security Features (Default Off)

Both disabled by default; enable in `application.properties` if needed:
- `ccc.security.connection-limiter.enabled=true` — max 10 concurrent / 30 new per minute per IP
- `ccc.security.rate-limiter.enabled=true` — exponential backoff on failed connect/reconnect attempts

## Coding standards, constraints & workflow rules

See [`CLAUDE.md`](CLAUDE.md). The coding standards, constraints (dependencies, public API, secrets, backwards compatibility), workflow rules (human review, merge approval) and output format defined there apply to **all** agents working in this repo — Claude Code, Codex, Cursor, Continue, Zed, OpenCode etc. — not just Claude.

CLAUDE.md is the canonical source for these rules; this file (AGENTS.md) covers tool-agnostic project facts (build, test, architecture, runtime).
