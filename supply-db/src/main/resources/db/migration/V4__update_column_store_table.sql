DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'store' AND column_name = 'created_by'
    ) THEN
        ALTER TABLE store RENAME COLUMN created_by TO created_by_id;
    END IF;
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'store' AND column_name = 'last_modified_by'
    ) THEN
        ALTER TABLE store RENAME COLUMN last_modified_by TO last_modified_by_id;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fkixs2wmnld5ldfxk0q27od0v6j'
    ) THEN
        ALTER TABLE store ADD CONSTRAINT FKixs2wmnld5ldfxk0q27od0v6j
            FOREIGN KEY (created_by_id) REFERENCES employee;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fkf18jgat64phjo4ms2oy5fennm'
    ) THEN
        ALTER TABLE store ADD CONSTRAINT FKf18jgat64phjo4ms2oy5fennm
            FOREIGN KEY (last_modified_by_id) REFERENCES employee;
    END IF;
END $$;
