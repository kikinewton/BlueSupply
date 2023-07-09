ALTER TABLE request_item ADD COLUMN receiving_store_id INTEGER;

ALTER TABLE request_item ADD CONSTRAINT FK_REQUEST_ITEM_ON_RECEIVING_STORE FOREIGN KEY (receiving_store_id) REFERENCES store (id);


