# CLAUDE.md – ccc-service

## Project

Backend WebSocket service maintained by SOGIS. Java/Spring Boot.

## Coding Standards

- **Language:** Java 21+ (project targets Java 25 per agents.md)
- **Style:** follow existing naming, indentation, and Javadoc conventions in the repo
- **Directory structure:** maintain existing package structure under `src/main/java/ch/so/agi/cccservice/`
- **Tests:** JUnit 5, minimum 80% coverage; never skip or approve code with failing tests
- **Error handling:** follow existing patterns for exception handling, logging, and error codes

## Constraints

- Do **not** introduce new external dependencies without explicit human approval
- Do **not** delete or rename public API interfaces (controllers, endpoints) without human approval
- Do **not** expose secrets, API keys, or credentials in code
- Do **not** break backwards compatibility unless explicitly requested
- Keep the build passing at all times — tests green, no broken API contracts

## Workflow Rules

- Major architecture changes (public API modified, new dependencies) require human review before implementation
- Security- or performance-sensitive changes require human review before merging
- Do not merge to `main` or `master` without explicit human approval
- When a task is ambiguous, ask rather than assume

## Output Format

When making non-trivial changes, include:
- Summary of what changed and why
- Any assumptions made
- Test gaps or follow-up items (if any)
