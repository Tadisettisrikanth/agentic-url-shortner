# Engineering Outcome

The final outcome is generated at runtime by `POST /api/v1/workflows/{id}/outcome`; this document explains its provenance and does not hardcode a successful run.

The outcome contains workflow/revision identity, original and normalized requirements, the current plan hash, generated production and test paths, validation attempt IDs, artifact hashes, and the evidence-derived release-ready decision. `criterion_traceability` maps each acceptance criterion to planning tasks, production paths, test paths, attempt IDs, hashes, and real Maven feature proof. Release approval is accepted only for the exact persisted outcome hash.

Supporting evidence includes specialist outputs for architecture, documentation, risk, and release readiness; provider/model invocation records; structured file proposals; applied operations and unified diffs; source manifests; fixed-capability logs; repair lineage; rollback verification; policies; approvals; audits; and durable coordination records.

Known limitation: the deterministic generated target uses in-memory storage to keep scenario builds self-contained. The orchestration control plane uses PostgreSQL/Flyway. Production URL storage, externally deployed OIDC/TLS, and a production traffic rate limiter require deployment-specific integration and are not represented as already deployed.
