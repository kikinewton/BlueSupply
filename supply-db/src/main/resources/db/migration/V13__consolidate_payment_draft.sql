-- V13: Consolidate payment_draft into payment
-- Both tables are empty at the time of this migration so no data migration is needed.

-- 1. Add stage column to drive the approval workflow lifecycle
ALTER TABLE payment
    ADD COLUMN IF NOT EXISTS stage VARCHAR(20) NOT NULL DEFAULT 'DRAFT';

-- 2. Add auditor-approval date (existed only on payment_draft before)
ALTER TABLE payment
    ADD COLUMN IF NOT EXISTS approval_by_auditor_date TIMESTAMP;

-- 3. Relax constraints that were too strict for DRAFT-stage payments
--    (bank and cheque_number are optional at creation time for a draft)
ALTER TABLE payment
    ALTER COLUMN bank DROP NOT NULL;

ALTER TABLE payment
    ALTER COLUMN cheque_number DROP NOT NULL;

-- 4. paymentAmount was updatable=false before; relax at the DB level
--    (no column constraint change needed — NOT NULL was never on paymentAmount)

-- 5. Remove the stale back-pointer column (payment_draft_id) that tracked
--    which draft a payment was copied from — no longer meaningful.
ALTER TABLE payment
    DROP COLUMN IF EXISTS payment_draft_id;
