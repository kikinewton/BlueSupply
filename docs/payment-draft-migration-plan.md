# Payment Draft → Payment Consolidation Plan

## Context

The codebase has two parallel entities, `PaymentDraft` and `Payment`, that share 15 of the same fields
and represent sequential stages of the same business object (a payment against a GRN). The current
flow is:

1. Account Officer creates a `PaymentDraft`
2. Auditor, FM, and GM each approve the draft (three sequential `PUT` calls)
3. On GM approval, a `@PreUpdate` hook soft-deletes the draft and an async Spring event fires to copy
   the draft's fields into a new `Payment` row via `BeanUtils.copyProperties`

This two-table design introduces several bugs (described in `test-coverage-and-docs-analysis.md`) and
can be safely collapsed into a single table because **both `payment` and `payment_draft` are currently
empty** — no data migration is required.

---

## Target Design

A single `payment` table tracks the full lifecycle via a `stage` column:

```
DRAFT → AUDITOR_APPROVED → FM_APPROVED → FULLY_APPROVED
```

- `DRAFT`: created by Account Officer, pending first approval
- `AUDITOR_APPROVED`: Auditor approved; visible to FM queue
- `FM_APPROVED`: FM approved; visible to GM queue
- `FULLY_APPROVED`: GM approved; considered a settled payment — appears in `payment_report`

The `payment_draft_comment` table is re-pointed to `payment`. The `payment_draft` table is dropped.
The `PaymentDraftListener` async event (and the race condition it carries) is eliminated. Approval
logic lives directly in the service, updating `stage` in the same transaction.

---

## Phase A — Database Schema (Flyway)

### V13: Extend `payment` table

```sql
-- Stage column drives the approval workflow lifecycle
ALTER TABLE payment
    ADD COLUMN stage VARCHAR(20) NOT NULL DEFAULT 'DRAFT';

-- Draft had this date; payment did not
ALTER TABLE payment
    ADD COLUMN approval_by_auditor_date TIMESTAMP;

-- Remove the redundant back-pointer (payment_draft_id) — no longer meaningful
-- Keep it nullable for now; drop after Phase B is deployed and verified
-- ALTER TABLE payment DROP COLUMN payment_draft_id;  -- run in V15 after cutover
```

### V14: Re-point `payment_draft_comment` to `payment`

```sql
-- Add the new FK column alongside the old one
ALTER TABLE payment_draft_comment
    ADD COLUMN payment_id INT4;

-- Add the FK constraint
ALTER TABLE payment_draft_comment
    ADD CONSTRAINT fk_payment_draft_comment_payment
    FOREIGN KEY (payment_id) REFERENCES payment(id);

-- The old FK column (payment_draft_id) is left in place here.
-- Drop it in V15 once Phase B is deployed and the old column is unused.
```

### V15: Drop `payment_draft` table (run after Phase B is fully deployed)

```sql
-- Drop old FK from comment to draft
ALTER TABLE payment_draft_comment
    DROP CONSTRAINT fkn15rpkk362tvutwwgfxuk4ix4;

ALTER TABLE payment_draft_comment
    DROP COLUMN payment_draft_id;

-- Remove back-pointer from payment that is no longer needed
ALTER TABLE payment
    DROP COLUMN IF EXISTS payment_draft_id;

-- Archive unique constraint from draft that now lives on payment
-- payment.purchase_number already has a UNIQUE constraint — no change needed

DROP TABLE payment_draft;
```

### V16: Update `payment_report` view to filter by stage

The existing view returns all `payment` rows. After consolidation, rows in the `DRAFT`,
`AUDITOR_APPROVED`, and `FM_APPROVED` stages should not appear in financial reports — only
`FULLY_APPROVED` rows should.

```sql
CREATE OR REPLACE VIEW public.payment_report AS
SELECT
    p.id,
    (SELECT s.name FROM supplier s WHERE s.id = grn.supplier) AS supplier,
    (SELECT i.invoice_number FROM invoice i WHERE i.id = grn.invoice_id) AS invoice_no,
    (SELECT s.account_number FROM supplier s WHERE s.id = grn.supplier) AS account_number,
    (SELECT e.full_name FROM employee e WHERE e.id = p.employee_gm_id) AS approved_by_gm,
    (SELECT e.full_name FROM employee e WHERE e.id = p.employee_fm_id) AS verified_by_fm,
    (SELECT e.full_name FROM employee e WHERE e.id = p.employee_auditor_id) AS checked_by_auditor,
    p.cheque_number,
    p.purchase_number,
    date(grn.payment_date) AS payment_due_date,
    p.payment_amount AS payable_amount,
    p.withholding_tax_percentage,
    p.withholding_tax_amount,
    p.payment_status,
    date(p.created_date) AS payment_date
FROM payment p
JOIN goods_received_note grn ON p.goods_received_note_id = grn.id
WHERE p.stage = 'FULLY_APPROVED'
  AND p.deleted = false;
```

---

## Phase B — Java Code Changes

### 1. Add `PaymentStage` enum

Create `supply-svc/src/main/java/com/logistics/supply/enums/PaymentStage.java`:

```java
package com.logistics.supply.enums;

public enum PaymentStage {
    DRAFT,
    AUDITOR_APPROVED,
    FM_APPROVED,
    FULLY_APPROVED
}
```

### 2. Update `Payment` entity

Key changes:
- Add `stage` field (`PaymentStage`, default `DRAFT`)
- Add `approvalByAuditorDate` field (was only on draft)
- Add `createdBy` field (`@ManyToOne Employee`, was only on draft)
- Move `@PrePersist` withholding tax calculation from `PaymentDraft` here
- Move `@CreationTimestamp` here (draft had it; payment relied on `AbstractAuditable`)
- Change `goodsReceivedNote` from `@ManyToOne` to keep as-is (draft used `@OneToOne` — do NOT
  change to `@OneToOne` here; a GRN can in principle have partial payments)
- Remove `paymentDraftId` field after V15 is run

```java
// Fields to add:
@Enumerated(EnumType.STRING)
@Column(length = 20, nullable = false)
private PaymentStage stage = PaymentStage.DRAFT;

private Date approvalByAuditorDate;

@ManyToOne
@JoinColumn(name = "created_by_id", updatable = false)
private Employee createdBy;

// Lifecycle hook — moved from PaymentDraft:
@PrePersist
public void calculateWithholdingTax() {
    // Divide raw percentage input (e.g. 5.0) by 100 to get the rate (0.05)
    BigDecimal rate = withholdingTaxPercentage.divide(BigDecimal.valueOf(100));
    BigDecimal invoiceAmount = goodsReceivedNote.getInvoiceAmountPayable();
    withholdingTaxAmount = invoiceAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    // Store the normalised rate, not the raw input
    withholdingTaxPercentage = rate;
}
```

Remove the `updatable = false` on `approvalFromAuditor` — all three approval flags should have
identical column definitions.

### 3. Update `PaymentDraftComment` / rename table reference

Rename the entity to `PaymentComment` (or keep the class name but update the `@JoinColumn`). Change
the `paymentDraft` field to reference `Payment`:

```java
// Before:
@ManyToOne
@JoinColumn(name = "payment_draft_id")
PaymentDraft paymentDraft;

// After:
@ManyToOne
@JoinColumn(name = "payment_id")
Payment payment;
```

Update `PaymentDraftCommentRepository`, `PaymentDraftCommentService`, and
`PaymentDraftCommentListener` accordingly.

### 4. Merge `PaymentDraftService` into `PaymentService`

All methods from `PaymentDraftService` move to `PaymentService`, backed by `PaymentRepository`.
The key changes per method:

| Old method | Change |
|---|---|
| `savePaymentDraft` | Becomes `createPaymentDraft`; saves a `Payment` with `stage = DRAFT` |
| `approvePaymentDraft` | Updates `stage` and approval fields on a `Payment` row; no event fired |
| `findAllDrafts` | Queries `Payment` filtered by `stage` per role (see below) |
| `paymentDraftHistory` | Queries `Payment` filtered by approvals per role |
| `findByDraftId` | Becomes `findById` (already exists) |
| `updatePaymentDraft` | Updates the `Payment` row (only allowed while `stage = DRAFT`) |
| `deleteById` | Uses existing `paymentDraftRepository.deleteById` → `paymentRepository.deleteById` |
| `count` | Uses `paymentRepository.count()` |

Role-based draft queue (replaces the `PaymentDraftSpecification` switch):

```
AUDITOR       → stage = DRAFT
FINANCIAL_MANAGER → stage = AUDITOR_APPROVED
GENERAL_MANAGER   → stage = FM_APPROVED
```

Approval logic update (replaces the three private `*Approval` methods):

```java
// On ROLE_AUDITOR:
payment.setApprovalFromAuditor(true);
payment.setApprovalByAuditorDate(new Date());
payment.setEmployeeAuditorId(employee.getId());
payment.setStage(PaymentStage.AUDITOR_APPROVED);

// On ROLE_FINANCIAL_MANAGER:
payment.setApprovalFromFM(true);
payment.setApprovalByFMDate(new Date());
payment.setEmployeeFmId(employee.getId());
payment.setStage(PaymentStage.FM_APPROVED);

// On ROLE_GENERAL_MANAGER:
payment.setApprovalFromGM(true);
payment.setApprovalByGMDate(new Date());
payment.setEmployeeGmId(employee.getId());
payment.setStage(PaymentStage.FULLY_APPROVED);
// No event fired — the record is already in the payment table
```

The `CompletableFuture.runAsync` event publish and `PaymentDraftListener.addPayment` are **deleted
entirely**. The payment is already the payment; no copy is needed.

### 5. Update `PaymentDraftController`

Keep the same URL paths (`/api/paymentDraft`, `/api/paymentDrafts`) to avoid breaking any existing
API clients. Wire the controller to the merged `PaymentService` methods instead of
`PaymentDraftService`. The request/response shapes (`PaymentDraftDTO`) remain unchanged.

Internally, responses return `Payment` objects filtered by stage, which carry all the same fields
that `PaymentDraft` did — API consumers see no structural change.

### 6. Delete these classes after the merge is complete

- `com.logistics.supply.model.PaymentDraft`
- `com.logistics.supply.service.PaymentDraftService`
- `com.logistics.supply.repository.PaymentDraftRepository`
- `com.logistics.supply.event.listener.PaymentDraftListener` (and `PaymentDraftEvent`)
- `com.logistics.supply.exception.PaymentDraftNotFoundException` — replace with the existing
  `PaymentNotFoundException` or a generic `ResourceNotFoundException`
- `com.logistics.supply.specification.PaymentDraftSpecification` — inline the stage-based queries
  directly into the repository or fold into `PaymentSpecification`

---

## Rollout Order

Run these steps in sequence. Each step is independently deployable and safe to stop at.

```
1. Apply V13 (add stage + approval_by_auditor_date to payment)
2. Apply V14 (add payment_id FK to payment_draft_comment)
3. Implement Phase B Java changes, deploy application
4. Smoke-test: create draft → approve as auditor → FM → GM
   verify stage progresses correctly, payment_report shows only FULLY_APPROVED rows
5. Apply V15 (drop payment_draft table, clean up old columns)
6. Apply V16 (update payment_report view to filter by stage)
7. Remove PaymentDraftControllerTest seeded data for payment_draft from init_script.sql;
   update integration tests to use Payment with stage=DRAFT instead
```

---

## Integration Test Updates

The existing `PaymentDraftControllerTest` and `SecurityAccessControlTest` tests remain valid — the
endpoints and role rules are unchanged. The only update required is in
`supply-svc/src/test/resources/sql/init_script.sql`:

- Remove the `INSERT INTO payment_draft` seed row
- Replace with `INSERT INTO payment (..., stage) VALUES (..., 'DRAFT')` using the same id (100)

No controller test method signatures change.

---

## What is NOT changed

- All five database views (`float_aging_analysis`, `float_payment_report`, `grn_report`,
  `petty_cash_payment_report`, `procured_item_report`) — none reference `payment_draft`
- `cancel_payment` table — already FKs to `payment`, not `payment_draft`
- `PaymentController` and `PaymentService` — these handle the settled payment CRUD; they only grow
  to absorb the draft workflow methods
- All API URL paths — kept identical to avoid breaking clients
- `PaymentDraftDTO` — reused as the creation request body unchanged
