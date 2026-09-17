# Agentic URL Shortner

This repository is a governed agentic software-engineering platform demonstrated through URL-shortener scenarios. It turns submitted requirements into repository analysis, a dynamic plan, specialist outputs, generated source and tests, governed isolated changes, real Maven validation, bounded repair, evidence-derived outcomes, and exact-hash human approvals.

## Prerequisites

- Java 21
- Docker Desktop with Docker Compose for PostgreSQL
- PowerShell 7 or Windows PowerShell 5.1

## Start locally

```powershell
docker compose up -d postgres
.\mvnw.cmd spring-boot:run
```

The default database settings can be overridden with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. The application listens on `8080`. Local API access uses HTTP Basic authentication with `operator` / `local-development-only`; replace both through `AGENTIC_BASIC_USERNAME` and `AGENTIC_BASIC_PASSWORD`.

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

## Governed source application

After reviewing the current plan, request agent-generated production and test proposals using only its exact hash. Callers cannot submit file operations or content:

```powershell
$applyBody = @{ planHash = $plan.planHash } | ConvertTo-Json
$changes = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/api/v1/workflows/$workflowId/changes/apply" `
  -ContentType "application/json" -Body $applyBody

$changes.status
$changes.changedPaths
$changes.unifiedDiff
```

The endpoint moves a successful workflow to `EXECUTING`. Implementation and test agents return the operations applied by the controlled patch engine; there is no separate hardcoded writer. Each operation carries requirement, criterion, task and input-hash lineage. Policies enforce isolated-workspace paths, permitted roots/types, operation and byte limits, duplicate rejection, optimistic hashes, atomic replacement, manifests, diffs and verified rollback.

Commit 5 proves the provider-to-proposal-to-applied-diff chain with compiled-source-set traceability contracts. Complete requirement-specific URL-shortener scenario generation is delivered in commit 8; real compiler/test execution begins in commit 6.

## Real validation and recovery

`POST /api/v1/workflows/{workflowId}/validate` runs only the fixed Maven Wrapper `clean verify` capability in the isolated revision workspace. The child process receives no model/API credentials. Exit code, duration, timeout, bounded output, failure classification, discovered tests, coverage availability, recovery decision, and audit events are persisted for every attempt.

Compiler, test, and configuration failures are supplied with bounded current source, prior proposal, and hashes to the repair agent. Any returned repair uses the same structured operation policies and governed applier as the original change. Transient dependency, infrastructure, and timeout failures use bounded retry/backoff. Missing safe repairs stop for human intervention; exhausted or non-retryable failures restore and verify the baseline before entering `ROLLED_BACK` or `FAILED`.

## Governance and release outcome

Change approval requires `POST /api/v1/workflows/{id}/approvals/change` with the exact current plan hash and `X-Change-Approver-Token`. After real validation, `POST /outcome` builds criterion-level traceability from persisted production paths, test paths, artifact hashes, and validation attempts. Release approval requires that exact outcome hash and `X-Release-Approver-Token`; incomplete evidence cannot become `RELEASE_READY`. Operators may request a safe stop through `POST /cancel`.

Generated URL-shortener behavior is connected to `POST /urls`, `GET /{code}`, and `GET /urls/{code}/analytics`. See `docs/SCENARIOS.md` for greenfield, brownfield, ambiguity, repair, safe-stop, and rollback runs.

## Durable execution and packaged review

Flyway V8 adds transactional task claims, expiring leases, heartbeat timestamps, monotonically increasing fencing tokens, and idempotent completion effects. Recovery runs at startup and on a schedule; it requeues only expired `RUNNING` tasks and preserves clarification/approval pauses. Two Compose orchestrators share PostgreSQL and workspace storage on ports 8080 and 8081.

```powershell
.\mvnw.cmd clean verify
docker compose config --quiet
docker compose up -d --build
.\demo.ps1 greenfield
.\demo.ps1 brownfield
.\demo.ps1 ambiguous
.\demo.ps1 repair
.\demo.ps1 safe-stop
.\demo.ps1 failover
```

See [docs/REVIEWER-GUIDE.md](docs/REVIEWER-GUIDE.md), [docs/OBSERVABILITY.md](docs/OBSERVABILITY.md), and [docs/PRODUCTION-DEPLOYMENT.md](docs/PRODUCTION-DEPLOYMENT.md). Production mode uses OIDC/JWT role mapping and optional application TLS; this repository does not claim an externally deployed production environment.
