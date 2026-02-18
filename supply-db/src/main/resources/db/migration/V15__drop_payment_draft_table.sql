-- V15: Drop the now-redundant payment_draft table
-- Run only after V13 and V14 are applied and the application has been
-- deployed and verified in production.

-- Drop FK constraints that reference payment_draft
ALTER TABLE payment_draft_comment
    DROP CONSTRAINT IF EXISTS fkn15rpkk362tvutwwgfxuk4ix4;

DROP TABLE IF EXISTS payment_draft;
