# Agentic URL Shortner

This repository is a governed agentic software-engineering platform demonstrated through URL-shortener scenarios. The current implementation provides the Java 21 foundation, durable schema, execution contracts, asynchronous requirement interpretation, ambiguity handling, authenticated clarification, and revision lineage. Later commits connect those requirements to repository analysis, generated source and tests, real validation, repair, governance, and release readiness.

## Prerequisites

- Java 21
- Docker Desktop with Docker Compose for PostgreSQL
- PowerShell 7 or Windows PowerShell 5.1

## Start locally

```powershell
docker compose up -d postgres
.\mvnw.cmd spring-boot:run
```

The default database settings can be overridden with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. The application listens on `8080`.

Verify health:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health/liveness
Invoke-RestMethod http://localhost:8080/actuator/health/readiness
```

Run verification:

```powershell
.\mvnw.cmd clean verify
```

## Requirement API

`POST /api/v1/workflows` accepts only `requirement` and `repositoryPath`, persists revision 1, returns HTTP 202 in `RECEIVED`, and asynchronously produces normalized acceptance criteria, assumptions, constraints, risks, and ambiguity analysis.

`GET /api/v1/workflows/{workflowId}` returns the current revision and analysis. A clear requirement advances to `PLANNING`. An ambiguous requirement advances to `AWAITING_CLARIFICATION` and keeps `sourceMutationAllowed` false.

Clarification is submitted to `POST /api/v1/workflows/{workflowId}/clarifications` with `X-Operator-Id` and `X-Operator-Token`. For local development the token defaults to `local-operator-token`; set `AGENTIC_CLARIFICATION_TOKEN` outside local development. A complete clarification creates a child revision, invalidates requirement-derived outputs, reuses unaffected repository-derived evidence, and reruns interpretation asynchronously.

Caller-provided execution state, completion output, validation, or artifacts remain rejected.

## Repository planning API

`POST /api/v1/workflows/{workflowId}/plan` is available after a clear requirement reaches `PLANNING`. It copies the submitted repository into an isolated revision workspace, records a content-addressed baseline manifest, analyzes brownfield structure and data flow, and persists a validated dependency plan before advancing to `AWAITING_CHANGE_APPROVAL`.

Repositories must be below an explicitly approved root. The local default is `./scenario-repositories`; configure it with `AGENTIC_REPOSITORY_ROOT`. Repository access rejects absolute/traversal paths, symbolic-link escapes, unsupported files, and configured size, file-count, and search-result limit violations.

Planning automatically invokes the 12 specialist roles documented in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md). The default `deterministic` provider requires no API key. To use the same contracts with the OpenAI Responses API, set:

```powershell
$env:AGENTIC_MODEL_PROVIDER = "openai"
$env:AGENTIC_MODEL_NAME = "gpt-5"
$env:OPENAI_API_KEY = "<environment-secret>"
```

Optional model controls are `OPENAI_BASE_URL`, `AGENTIC_MODEL_TIMEOUT`, `AGENTIC_MODEL_MAX_CONTEXT_CHARS`, and `AGENTIC_MODEL_MAX_OUTPUT_CHARS`. Credentials are environment-only and are redacted from provider context. Model output cannot execute commands or mutate the repository directly.

Example after polling a workflow to `PLANNING`:

```powershell
$plan = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/api/v1/workflows/$workflowId/plan"
$plan.status
$plan.repositoryMap
$plan.plan.tasks
$plan.agentInvocations
```
