# Agentic URL Shortner

This repository is a governed agentic software-engineering platform demonstrated through URL-shortener scenarios. Commit 1 establishes the Java 21 foundation, durable schema, health endpoints, execution contracts, and domain invariants. Later commits connect requirement interpretation to repository analysis, generated source and tests, real validation, repair, governance, and release readiness.

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

## Commit 1 API boundary

`POST /api/v1/workflows` accepts only `requirement` and `repositoryPath`. In commit 1 it returns a workflow identity and initial revision in `RECEIVED` state; durable submission storage arrives with the asynchronous requirement pipeline. Caller-provided execution state, completion output, validation, or artifacts are rejected. Requirement interpretation and asynchronous execution are introduced in commit 2.
