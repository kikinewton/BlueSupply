DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='quotation' AND column_name='hod_id') THEN
        ALTER TABLE quotation ADD COLUMN hod_id INTEGER;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='quotation' AND column_name='auditor_id') THEN
        ALTER TABLE quotation ADD COLUMN auditor_id INTEGER;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='quotation' AND column_name='hod_review_date') THEN
        ALTER TABLE quotation ADD COLUMN hod_review_date TIMESTAMP;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='quotation' AND column_name='auditor_review_date') THEN
        ALTER TABLE quotation ADD COLUMN auditor_review_date TIMESTAMP;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='quotation' AND column_name='updated_at') THEN
        ALTER TABLE quotation ADD COLUMN updated_at TIMESTAMP;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='quotation' AND column_name='auditor_review') THEN
        ALTER TABLE quotation ADD COLUMN auditor_review BOOLEAN NOT NULL DEFAULT false;
    END IF;
    -- Rename reviewed -> hod_review
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='quotation' AND column_name='reviewed')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='quotation' AND column_name='hod_review') THEN
        ALTER TABLE quotation RENAME COLUMN reviewed TO hod_review;
    END IF;
    -- Rename employee_id -> created_by_id
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='quotation' AND column_name='employee_id')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='quotation' AND column_name='created_by_id') THEN
        ALTER TABLE quotation RENAME COLUMN employee_id TO created_by_id;
    END IF;
    -- Drop old FK
    IF EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name='fk45h0o7evaf0hyx029t890rwkl') THEN
        ALTER TABLE quotation DROP CONSTRAINT fk45h0o7evaf0hyx029t890rwkl;
    END IF;
    -- Add new FKs
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name='fk_quotation_on_auditor') THEN
        ALTER TABLE quotation ADD CONSTRAINT FK_QUOTATION_ON_AUDITOR FOREIGN KEY (auditor_id) REFERENCES employee (id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name='fk_quotation_on_employee') THEN
        ALTER TABLE quotation ADD CONSTRAINT FK_QUOTATION_ON_EMPLOYEE FOREIGN KEY (created_by_id) REFERENCES employee (id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name='fk_quotation_on_hod') THEN
        ALTER TABLE quotation ADD CONSTRAINT FK_QUOTATION_ON_HOD FOREIGN KEY (hod_id) REFERENCES employee (id);
    END IF;
END $$;
