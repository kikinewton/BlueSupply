DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'request_item' AND column_name = 'receiving_store_id'
    ) THEN
        ALTER TABLE request_item ADD COLUMN receiving_store_id INTEGER;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_request_item_on_receiving_store'
    ) THEN
        ALTER TABLE request_item
            ADD CONSTRAINT FK_REQUEST_ITEM_ON_RECEIVING_STORE
            FOREIGN KEY (receiving_store_id) REFERENCES store (id);
    END IF;
END $$;
