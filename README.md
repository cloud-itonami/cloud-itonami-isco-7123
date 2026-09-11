# cloud-itonami-isco-7123

Open Occupation Blueprint for **ISCO-08 7123**: Plasterers.

This repository designs a forkable OSS business for a plastering job-site scheduling and logistics coordination practice: a job-site scheduling and supply-coordination robot manages crew/task records under a governor-gated actor, so a plastering crew keeps its own operating records instead of renting a closed workforce-management SaaS.

**Maturity: `:implemented`.** `src/plasterer/` implements the
`PlastererActor` as a `langgraph.graph/state-graph`
(`plasterer.actor`) wired to a `Plasterer Advisor`
(`plasterer.advisor`) and an independent `PlastererGovernor`
(`plasterer.governor`), following the itonami actor pattern
(ADR-2607121000): `:intake -> :advise -> :govern -> :decide -+-> :commit
(:ok?) +-> :request-approval (:escalate?, human-in-the-loop interrupt)
+-> :hold (:hard?)`. 21 tests / 45 assertions green (`kbb -M:test`).
HARD invariants (always hold, never overridable): plasterer provenance,
site provenance, no-actuation (`:effect` must be `:propose`), a closed
op-allowlist (`:log-work-record`, `:schedule-crew-operation`,
`:flag-safety-concern`, `:coordinate-supply-order` — nothing else may
ever be proposed), and a permanent, unconditional block on any
proposal that would directly finalize a plastering-execution decision
(e.g. deciding to proceed with a specific wall/ceiling finishing step)
or override a site safety officer's judgment. Always-escalate paths
(human sign-off regardless of confidence, mapping this repo's Trust
Controls in [`docs/business-model.md`](docs/business-model.md)):
`:flag-safety-concern` (always) and `:coordinate-supply-order` above
the registered cost threshold.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs
the physical domain work**. Here a job-site scheduling/logistics coordination robot performs crew scheduling, task/materials-usage/progress-record logging and plastering-materials supply-order coordination for a plastering crew, under an actor that proposes actions and an independent **Plasterer Governor** that gates them. The governor never
dispatches hardware itself, never performs plastering work on the job site, and never finalizes a plastering-execution decision or overrides a site safety officer's judgment; `:high`/`:safety-critical` actions (such as a flagged dust-exposure/scaffold-hazard/site-condition concern, or an above-threshold supply order) require human sign-off. **This actor coordinates job-site scheduling/logistics only — it never performs plastering work itself.**

## Core Contract

```text
crew roster + job-site registration + safety-reporting policy
        |
        v
Plasterer Advisor -> Plasterer Governor -> log/schedule/coordinate, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses, finalize
a plastering-execution decision, override a site safety officer's judgment,
suppress an operating record, or disclose sensitive data without governor
approval and audit evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `7123`). Required capabilities:

- :robotics
- :identity
- :audit-ledger

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

AGPL-3.0-or-later.
