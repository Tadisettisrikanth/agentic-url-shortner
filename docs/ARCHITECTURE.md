# Architecture

## System boundary

The platform separates a durable control plane from an isolated execution plane. PostgreSQL stores workflows, revisions, plans, tasks, dependencies, attempts, artifacts, validations and specialist invocations. Repository tools operate only on approved roots and revision-specific copies. Later commits add proposal application, fixed build capabilities, recovery and distributed task claiming without changing these boundaries.

## Current flow

1. `POST /api/v1/workflows` persists workflow revision 1 and dispatches requirement interpretation.
2. Ambiguous work pauses in `AWAITING_CLARIFICATION`; accepted answers create a child revision.
3. `POST /api/v1/workflows/{id}/plan` creates an isolated repository copy and baseline manifest.
4. Repository analysis and dynamic planning are persisted with content hashes.
5. The specialist orchestrator invokes all provider-neutral roles through one bounded model gateway.
6. Each invocation persists a task, dependency, execution attempt, validated artifact and invocation evidence.
7. The workflow pauses in `AWAITING_CHANGE_APPROVAL`. No generated proposal is applied in commit 4.

## Model boundary

`SpecialistAgent` is the provider-neutral role contract. `ModelBackedSpecialistAgent` supplies role-specific instructions and current-revision context to `BoundedModelGateway`; both deterministic and OpenAI providers implement `ModelProvider` and return the same `ModelResponse` contract.

The gateway enforces:

- configured context and output character limits;
- a hard call timeout;
- redaction of bearer tokens, common credential assignments and private keys before provider invocation;
- strict deserialization with unknown-property rejection;
- semantic validation of required fields and collection bounds;
- a shared JSON Schema used by the OpenAI Responses API in strict mode.

The deterministic provider needs no key and is the default. The optional OpenAI provider uses the Responses API and requires `OPENAI_API_KEY`. Provider selection does not change persistence, validation or orchestration behavior.

## Specialist roles

The automatically invoked roles are requirement interpretation, ambiguity analysis, repository analysis, task planning, architecture, implementation, test generation, validation diagnosis, repair, documentation, security/risk review and release readiness.

These commit-4 artifacts are advisory, current-revision outputs. Validation diagnosis and repair explicitly decline to invent failures when no real failure evidence exists. Release readiness remains false until production changes and tests have been applied and validated by later execution stages.

## Evidence model

Every specialist invocation records workflow, revision, task, agent role, provider, model, attempt number, input artifact hashes, complete output JSON, output hash, decisions, assumptions, risks, duration and generated artifact IDs. The corresponding engineering artifact records schema version, storage location, producer identity and validation status. A task completes only after provider execution, artifact persistence and structured-output validation succeed in the same transaction.

## Trust boundaries

- Model output is untrusted data and cannot directly execute a command or mutate source.
- Repository paths are resolved beneath configured approved roots.
- OpenAI credentials come only from environment configuration and are never inserted into model context.
- Source mutation remains behind current-revision change approval and the controlled proposal pipeline introduced in commit 5.
- Real compiler/test execution is limited to fixed capabilities introduced in commit 6.
