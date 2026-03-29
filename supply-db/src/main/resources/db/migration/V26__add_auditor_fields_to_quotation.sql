-- Bring quotation schema up to what the Quotation entity expects.
-- Covers all V8 changes (column renames + new columns) plus auditor fields,
-- using conditional DDL so the migration is safe to run against a DB that
-- already has some of these columns applied.

DO $$
BEGIN
    -- Rename employee_id -> created_by_id (V8 change)
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'quotation' AND column_name = 'employee_id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'quotation' AND column_name = 'created_by_id'
    ) THEN
        ALTER TABLE quotation RENAME COLUMN employee_id TO created_by_id;
    END IF;

    -- Rename reviewed -> hod_review (V8 change)
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'quotation' AND column_name = 'reviewed'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'quotation' AND column_name = 'hod_review'
    ) THEN
        ALTER TABLE quotation RENAME COLUMN reviewed TO hod_review;
    END IF;
END
$$;

-- Add missing columns (V8 + auditor additions)
ALTER TABLE quotation
    ADD COLUMN IF NOT EXISTS hod_id              INTEGER   REFERENCES employee (id),
    ADD COLUMN IF NOT EXISTS auditor_id          INTEGER   REFERENCES employee (id),
    ADD COLUMN IF NOT EXISTS hod_review_date     TIMESTAMP,
    ADD COLUMN IF NOT EXISTS auditor_review_date TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at          TIMESTAMP,
    ADD COLUMN IF NOT EXISTS auditor_review      BOOLEAN   NOT NULL DEFAULT FALSE;

-- Ensure FK constraints exist (V8 added these after the renames)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_quotation_on_employee' AND table_name = 'quotation'
    ) THEN
        ALTER TABLE quotation
            ADD CONSTRAINT FK_QUOTATION_ON_EMPLOYEE FOREIGN KEY (created_by_id) REFERENCES employee (id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_quotation_on_hod' AND table_name = 'quotation'
    ) THEN
        ALTER TABLE quotation
            ADD CONSTRAINT FK_QUOTATION_ON_HOD FOREIGN KEY (hod_id) REFERENCES employee (id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_quotation_on_auditor' AND table_name = 'quotation'
    ) THEN
        ALTER TABLE quotation
            ADD CONSTRAINT FK_QUOTATION_ON_AUDITOR FOREIGN KEY (auditor_id) REFERENCES employee (id);
    END IF;
END
$$;
