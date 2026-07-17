(ns plasterer.store
  "SSoT for the ISCO-08 7123 plastering job-site scheduling/logistics
  coordination actor (itonami actor pattern, ADR-2607121000 / CLAUDE.md
  Actors section; README's 'Robotics premise' — a job-site
  scheduling/logistics coordination robot performs crew scheduling,
  task/materials-usage/progress-record logging and plastering-materials
  supply-order coordination for a plastering crew under this
  advisor/governor pair, which never dispatches hardware itself, never
  performs plastering work itself, and never finalizes a
  plastering-execution decision (e.g. a specific wall/ceiling finishing
  step) or overrides a site safety officer's judgment — those remain
  the site safety officer's exclusive judgment). Modeled on
  cloud-itonami-isco-7111's housebuilder.store (same wave/domain
  shape).

  Domain:

    plasterer — a registered plastering crew member
                (:plasterer-id, :name)
    site      — a registered plastering job site {:site-id :name
                :max-supply-cost number}. `:max-supply-cost` is an
                informational registered ceiling used only to decide
                whether a `:coordinate-supply-order` proposal escalates
                to human sign-off (the governor never blocks a
                within-threshold order outright; it only decides
                commit vs. escalate).
    record    — a committed operating record (a logged
                task/materials-usage/progress entry, a scheduled crew
                operation, a flagged safety concern, or a coordinated
                supply order) — written ONLY via commit-record!.
    ledger    — append-only audit trail, commit or hold.")

(defprotocol Store
  (plasterer [s plasterer-id])
  (site [s site-id])
  (records-of [s plasterer-id])
  (ledger [s])
  (register-plasterer! [s plasterer])
  (register-site! [s site])
  (commit-record! [s record])
  (append-ledger! [s fact]))

(defrecord MemStore [a]
  Store
  (plasterer [_ plasterer-id] (get-in @a [:plasterers plasterer-id]))
  (site [_ site-id] (get-in @a [:sites site-id]))
  (records-of [_ plasterer-id] (filter #(= plasterer-id (:plasterer-id %)) (:records @a)))
  (ledger [_] (:ledger @a))
  (register-plasterer! [s p]
    (swap! a assoc-in [:plasterers (:plasterer-id p)] p) s)
  (register-site! [s st]
    (swap! a assoc-in [:sites (:site-id st)] st) s)
  (commit-record! [s record]
    (swap! a update :records (fnil conj []) record) s)
  (append-ledger! [s fact]
    (swap! a update :ledger (fnil conj []) fact) s))

(defn mem-store
  ([] (mem-store {}))
  ([seed] (->MemStore (atom (merge {:plasterers {} :sites {} :records [] :ledger []}
                                    seed)))))
