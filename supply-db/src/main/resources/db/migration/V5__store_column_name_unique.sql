DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'uk_store_name_u940' AND table_name = 'store'
    ) THEN
        ALTER TABLE store ADD CONSTRAINT UK_store_name_u940 UNIQUE (name);
    END IF;
END $$;
