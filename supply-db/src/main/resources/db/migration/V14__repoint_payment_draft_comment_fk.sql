-- V14: Re-point payment_draft_comment FK from payment_draft to payment

-- 1. Add the new FK column
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'payment_draft_comment' AND column_name = 'payment_id'
    ) THEN
        ALTER TABLE payment_draft_comment ADD COLUMN payment_id INT4;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_payment_draft_comment_payment'
    ) THEN
        ALTER TABLE payment_draft_comment
            ADD CONSTRAINT fk_payment_draft_comment_payment
            FOREIGN KEY (payment_id) REFERENCES payment(id);
    END IF;
END $$;

-- 2. Drop the old FK to payment_draft and the old column
ALTER TABLE payment_draft_comment
    DROP CONSTRAINT IF EXISTS fkn15rpkk362tvutwwgfxuk4ix4;

ALTER TABLE payment_draft_comment
    DROP COLUMN IF EXISTS payment_draft_id;
