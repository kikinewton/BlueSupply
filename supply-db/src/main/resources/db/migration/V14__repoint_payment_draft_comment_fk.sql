-- V14: Re-point payment_draft_comment FK from payment_draft to payment

-- 1. Add the new FK column
ALTER TABLE payment_draft_comment
    ADD COLUMN payment_id INT4;

-- 2. Add FK constraint to payment
ALTER TABLE payment_draft_comment
    ADD CONSTRAINT fk_payment_draft_comment_payment
    FOREIGN KEY (payment_id) REFERENCES payment(id);

-- 3. Drop the old FK to payment_draft and the old column
ALTER TABLE payment_draft_comment
    DROP CONSTRAINT IF EXISTS fkn15rpkk362tvutwwgfxuk4ix4;

ALTER TABLE payment_draft_comment
    DROP COLUMN IF EXISTS payment_draft_id;
