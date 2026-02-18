# Test Coverage & Documentation Analysis — BlueSupply

*Generated: 2026-02-18 | Branch: `add-claude-md`*

---

## Test Coverage Gaps

### Current State

| Metric | Count |
|---|---|
| Controllers with tests | 16 / 22 (73%) |
| Empty test class stubs | 2 (`GRNControllerTest`, `InvoiceControllerTest`) |
| Service unit tests | 0 / 42 |
| Test type | Integration only (all spin up full Spring Boot + Postgres via Testcontainers) |

All existing tests go through the controller layer via MockMvc — there are no isolated unit tests for the service layer at all. Bugs in business logic are only catchable if they surface through an endpoint.

---

### Critical Gaps — Add These First

#### 1. Payment Pipeline (No tests at all)
The entire financial end of the procurement workflow is untested. `PaymentController` and `PaymentDraftController` together cover:

- Creating and updating payment drafts (`ROLE_ACCOUNT_OFFICER`)
- Multi-stage approval: Auditor → Financial Manager → General Manager
- Cheque cancellation (`PUT /api/payments/{id}/cancelCheque`)
- Fetching outstanding GRNs without payment
- Payment draft history with role-based filtering

These are the highest-value and highest-risk operations in the system. A regression here has direct financial consequences.

#### 2. Reporting & Excel Export (No tests at all)
`ReportController` has 6 complex endpoints, each generating Excel files via Apache POI:

- Procured items report (date + supplier filtering)
- Payment report (period filtering)
- Petty cash payment report
- Float ageing analysis (complex multi-param filtering)
- Float order payment report
- GRN report

These are hard to test manually and easy to silently break during dependency upgrades (e.g. the Phase 4 iText7 and Phase 3 Flying Saucer upgrades planned in `SECURITY_REMEDIATION.md`). No test means no safety net for those upcoming changes.

#### 3. Supplier Management (No tests at all)
`SupplierController` handles the supplier master data that underpins the entire procurement workflow — create, read, update, delete, and complex filtered listing (suppliers with/without quotations, unregistered suppliers). Zero test coverage.

#### 4. Dashboard & Notifications (No tests at all)
`DashboardController` and `NotificationCountController` have no tests. Dashboard metrics aggregate data across the entire schema — a broken query silently returns wrong numbers.

#### 5. Untested Endpoints Within Partially Tested Controllers
Even controllers that have some tests have significant gaps:

| Controller | Tests exist | What's missing |
|---|---|---|
| `FloatControllerTest` | 1 (findAll only) | Endorsement, HOD approval, GM approval flows |
| `LpoControllerTest` | 2 (fetch only) | LPO approval, cancellation, finalization |
| `GRNControllerTest` | Stub only | Entire class body is empty |
| `InvoiceControllerTest` | Empty class | Entire class body is empty |

---

### Service Layer — Zero Unit Tests

All 42+ service classes are tested only indirectly through controller integration tests. This means a bug in service business logic is only caught if there is a controller test that hits that exact code path.

High-value services to prioritise for unit tests:

| Service | Why it matters |
|---|---|
| `PaymentService` | Financial state transitions, cheque cancellation |
| `PaymentDraftService` | Multi-stage approval state machine |
| `QuotationService` | Quotation evaluation and selection logic |
| `LocalPurchaseOrderService` | LPO creation, approval, finalization |
| `RoleService` | Security-sensitive; wrong role evaluation = authorization bypass |
| `ExcelService` | 7 report templates; complex data mapping |

---

### Recommended Priority Order

```
1. PaymentController + PaymentDraftController  (critical, financial)
2. ReportController                            (needed before Phase 3/4 security fixes)
3. SupplierController                          (core master data)
4. Fill GRNControllerTest + InvoiceControllerTest stubs
5. Float approval workflow tests in FloatControllerTest
6. Unit tests: PaymentService, PaymentDraftService, RoleService
```

---

## Documentation Ambiguities

### README.md

| Location | Issue |
|---|---|
| Petty Cash section | **Incomplete** — Step 3 is literally blank. The workflow cuts off after "Request is sent to HOD". |
| LPO Draft step | **Ambiguous** — Says "HOD review and approval" then separately "forwarded to GM for final approval." It is not clear if HOD can reject, or if "review" and "approval" are distinct steps. |
| Payment approval chain | **Contradictory** — refers to both "chief accountant" and "financial manager" without clarifying if these are the same role. |
| Notification step | **Vague** — says the procurement department "informs" the user department and store, but does not say whether this is an automated system notification, email, or a manual step. |
| Rejection handling | **Missing entirely** — the entire README describes the happy path only. What happens when a request is rejected at any stage? |

---

### supply-db/README.md

The most problematic documentation in the project. It is 29 lines and provides almost no actionable information:

- "Place the Flyway migration scripts in the **designated folder**" — never says which folder (`supply-db/src/main/resources/db/migration/`)
- "Configure the **scheduler**" — never mentions `DatabaseBackupScheduler`, Spring's `@Scheduled`, or the cron expression (`0 0 0 * * ?`)
- "**Backup location** to suit your needs" — no mention of which config property controls this, or where backups are written by default
- "**Start the service**" — does not say whether this means `mvn flyway:migrate`, `mvn spring-boot:run`, or both, and in which order relative to `supply-svc`

---

### CLAUDE.md

| Location | Issue |
|---|---|
| PostgreSQL credentials section | Lists `password: docker` — hardcoded credentials in a committed doc is a minor security hygiene issue if the repo is public |
| "Role-based access controls enforce which endpoints each stage actor can call" | True but gives no guidance on where to find this mapping — there is no RBAC reference table anywhere in the docs |
| Procurement workflow steps | "HOD endorses → Procurement processes" reads as simultaneous; sequence vs parallel is unclear |

---

### SECURITY_REMEDIATION.md

| Location | Issue |
|---|---|
| Execution order | Says "Phase 6 → Phase 5 → Phase 4 → Phase 3 → Phase 2 → Phase 1" (low risk first), but Phase 1 is labelled Critical. The ordering rationale (risk mitigation) is correct but the contradiction in labelling vs sequence needs a clarifying note |
| Phase 2 testing requirements | Lists "refresh flows" — it is unclear whether token refresh is currently implemented in BlueSupply |
| Phase 3 XXE evaluation | Deferred without a conclusion — the doc says "evaluate whether Flying Saucer processes untrusted input" but does not record the result of that evaluation |
| "Full authentication regression" | Subjective — no checklist of what passes as "full" |

---

### docs/uuid-migration-analysis.md

| Location | Issue |
|---|---|
| "20–40% performance degradation" | No source cited — this is a commonly quoted range but the doc would be stronger with a reference or caveat that it depends on workload |
| `gen_random_uuid()` with `uuid_generate_v1mc()` | Grammatically reads as one approach, actually two different options — needs splitting |
| "Leave numeric IDs for now" | "For now" implies a future trigger for reconsideration — never specifies what that trigger would be (e.g., moving to microservices, hitting a volume threshold) |

---

## Overall Priorities

### Tests
1. `PaymentController` + `PaymentDraftController` integration tests
2. `ReportController` tests (needed before Phase 3/4 security upgrades in `SECURITY_REMEDIATION.md`)
3. Fill the two empty test class stubs (`GRNControllerTest`, `InvoiceControllerTest`)
4. `SupplierController` integration tests
5. Unit tests for `PaymentService`, `PaymentDraftService`, `RoleService`

### Docs
1. Complete the petty cash workflow in `README.md`
2. Rewrite `supply-db/README.md` with actual folder paths, commands, and config properties
3. Add rejection/escalation paths to the workflow description in `README.md`
4. Add an RBAC reference table (role → permitted endpoints)
