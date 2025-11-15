# Agents Guide for `ccc-service`

## 1. Overview

This document defines how the AI agents integrated into the `ccc-service` codebase collaborate, how they operate, and what rules they must follow.
The `ccc-service` repository is a backend service maintained by SOGIS. The AI agents will assist in tasks such as architecture design, implementation, testing, review, and documentation maintenance.
The goal is to maintain code quality, consistency, security and avoid conflicts between agents.

## 2. System-Wide Rules (for all agents)

### Global goals

* Keep the `ccc-service` build passing at all times — tests green, no broken API contracts.
* Maintain the SOGIS coding standards (style, naming, dependencies).
* Ensure any changes are backwards-compatible unless explicitly noted.
* Ensure full traceability: each agent’s output must include summary + reasoning + status.

### Universal constraints

* Agents must **not** introduce new external dependencies without human approval.
* Agents must **not** delete or rename public API interfaces (controllers, endpoints) without documenting and obtaining human approval.
* Agents must **not** expose secrets (API keys, credentials) in code.
* Agents must **not** execute unknown shell commands or arbitrary code at runtime.
* Agents must log their actions (outputs, summaries) for human audit.

### Communication rules

* All agent-generated outputs must follow the agreed template (see section 5).
* Agents must include **chain‐of‐thought** reasoning (hidden or separate) and present a concise decision summary.
* When an agent’s task is unclear or ambiguous, it must escalate to a human rather than proceed.
* Agents must not interfere with each other’s hand-offs: each role passes explicit artifact(s) to the next.

## 3. Agent Roles and Definitions

### Architect Agent

**Responsibilities**

* Design new features, modules or API changes for ccc-service.
* Produce architecture specification: endpoints, data models, flow diagrams.
* Identify required tasks for Coder Agent, Test Agent, Documentation Agent.
* Ensure alignment with system goals (scalability, maintainability, compliance).
  **Inputs**
* Feature request or change ticket, relevant domain context.
  **Outputs**
* Architecture spec document (markdown or diagram), list of tasks with priorities, dependencies.
  **Constraints**
* Must not implement code.
* Must not ignore existing architecture without justification.
  **Handoff**
* When spec is approved by human (or orchestrator), Coder Agent may begin implementation.

---

### Coder Agent

**Responsibilities**

* Implement code changes according to Architecture Agent’s spec.
* Write clean, idiomatic code, conforming to style/lint rules.
* Include required unit/integration tests (or delegate to Test Agent).
  **Inputs**
* Approved architecture spec, task list with dependencies.
  **Outputs**
* Code diff / merge request, summary of changes, explanation of decisions, list of test gaps or assumptions.
  **Constraints**
* Must not modify unrelated modules without documented reasoning.
* Must not merge without Reviewer Agent approval.
  **Handoff**
* After implementation, send to Test Agent and Reviewer Agent.

---

### Test Agent

**Responsibilities**

* Generate and/or run tests covering the new or changed code.
* Validate behaviour, edge cases, error conditions.
* Report test coverage, failing cases, and highlight missing tests.
  **Inputs**
* Code changes from Coder Agent.
  **Outputs**
* Test suite (unit/integration), test run results, coverage report, summary of issues.
  **Constraints**
* Must not skip tests or approve code with known failing tests.
* Must not modify production code except where required to fix discovered bugs (with Coder Agent coordination).
  **Handoff**
* After tests pass, signal to Reviewer Agent that code is ready for review.

---

### Reviewer Agent

**Responsibilities**

* Review code changes for correctness, style, security, performance, and architecture compliance.
* Provide reviews with comments, suggestions, and approval or request for changes.
  **Inputs**
* Code diff + tests + coverage report.
  **Outputs**
* Review report: pass/fail, comments, list of required fixes.
  **Constraints**
* Must not approve code with critical issues (e.g., failing tests, security holes, style violations).
  **Handoff**
* On approval, the Orchestrator or human merges the changes; if rejected, pass back to Coder Agent to revise.

---

### Documentation Agent

**Responsibilities**

* Update or create documentation corresponding to new features/changes: README, API docs, examples, changelog.
* Ensure internal code comments and external docs are consistent with implementation.
  **Inputs**
* Approved code changes, architecture spec, feature descriptions.
  **Outputs**
* Updated docs, release notes, user-guide sections.
  **Constraints**
* Must not produce outdated or incorrect documentation.
  **Handoff**
* Final documentation is committed and linked to the release.

---

### Orchestrator Agent

**Responsibilities**

* Coordinates the workflow: assigns tasks, tracks handoffs, ensures statuses.
* Monitors progress, escalates when human intervention is needed.
  **Inputs**
* Feature tickets, task list, agent status updates.
  **Outputs**
* Workflow dashboard/log, escalation alerts, overall status.
  **Constraints**
* Must not override agent decisions without logging human intervention.
  **Handoff**
* Marks features as done when all roles complete; triggers merge/release.

---

## 4. Workflow Protocol

Below is the standard sequence for a new feature or change in `ccc-service`:

1. **Orchestrator** receives a feature request (ticket).
2. **Architect Agent** drafts architecture spec and task breakdown.
3. Human (or automated process) reviews and approves spec.
4. **Coder Agent** implements code, writes initial tests.
5. **Test Agent** runs tests, expands coverage, reports results.
6. **Reviewer Agent** reviews code + tests + docs; approves or rejects.

    * If rejected → return to Coder Agent for revision → repeat steps 4-6.
7. **Documentation Agent** updates all relevant docs.
8. **Orchestrator** finalises feature: merges code, updates version/release notes, closes ticket.

### Messaging / Artifact Format

* Each handoff must include:

    * **Task ID** (ticket or feature number)
    * **Agent Role**
    * **Input version** (e.g., spec v1.0)
    * **Output version** (e.g., code diff, test report)
    * **Timestamp**
    * **Summary** of actions

### Escalation Rules

* If any agent hits a blocking issue (ambiguous input, architecture conflict, test failure, security concern) it must escalate to human (via Orchestrator) rather than proceed.
* Major architecture changes (public API modified, external dependencies introduced) **must** be human-approved.

## 5. Coding & Testing Standards

* Language: Java 25
* Style: follow the company/organization’s code style (indentation, naming, Javadoc, etc.).
* Directory structure: maintain existing structure in `ccc-service`.
* Dependencies: new dependencies must be approved; prefer existing libraries/frameworks.
* Tests: Use preferred framework (e.g., JUnit 5) and maintain high coverage (e.g., minimum 80%).
* Error handling: follow service’s standard for exception handling, logging, error codes.
* CI/CD: All changes must pass build, lint, test on the CI pipeline before merging.

## 6. Templates

### Architecture Spec Template

```markdown
# Architecture Spec – Feature [TICKET-ID]

**Scope:**  
…  

**Key Entities / Models:**  
…  

**API Endpoints / Interfaces:**  
…  

**Data flow / Sequence diagram:**  
…  

**Dependencies / Constraints:**  
…  

**Task Breakdown:**  
- Task 1: …  
- Task 2: …  

**Risks / Notes:**  
…  
```

### Coder Agent Output Template

```markdown
# Implementation – Feature [TICKET-ID]

**Files modified / added:**  
…  

**Summary of changes:**  
…  

**Assumptions made:**  
…  

**Tests added/modified:**  
…  

**Next steps:**  
…  
```

### Review Report Template

```markdown
# Review Report – Feature [TICKET-ID]

**Status:** Pass / Fail  
**Major comments:**  
- …  
**Minor comments / suggestions:**  
- …  
**Required fixes (if any):**  
- …  
```

## 7. Limitations

* Agents must not directly access production data or systems; sandbox/test data only.
* Agents must not perform manual deployments or change CI/CD pipelines without human review.
* Agents cannot merge to `main` or `master` without explicit human/Orchestrator approval.
* Agents cannot change repository policies, permissions, or access control.

## 8. Human Oversight

* Human developers/maintainers must review:

    * Any architecture spec that changes public interfaces.
    * Code with security or performance implications.
    * Merging of major features (version bump, database migrations).
* Human should assign ticket priority, review final merged result, and sign off release notes.
* Human should monitor the log of agent actions periodically and audit for compliance.

---

## Appendix: Change History

* **v0.1** – initial draft
* **v0.2** – added additional workflows and escalation rules
* *(Future versions will include more role-specific detail as needed)*

---

Feel free to adapt or extend this `agents.md` to your repository’s particular architecture, language, CI/CD tools, and organizational standards. If you like, I can generate a **Markdown file** ready to place into the root of the `ccc-service` repository, with further customization (e.g., exact Java version, repository directories, CI workflow).
