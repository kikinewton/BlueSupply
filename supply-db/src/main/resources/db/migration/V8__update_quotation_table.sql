ALTER TABLE IF EXISTS quotation ADD COLUMN hod_id INTEGER;

ALTER TABLE IF EXISTS quotation ADD COLUMN auditor_id INTEGER;

ALTER TABLE IF EXISTS quotation ADD COLUMN hod_review_date TIMESTAMP;

ALTER TABLE IF EXISTS quotation ADD COLUMN auditor_review_date TIMESTAMP;

ALTER TABLE IF EXISTS quotation ADD COLUMN updated_at TIMESTAMP;

ALTER TABLE IF EXISTS quotation ADD COLUMN auditor_review BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE IF EXISTS quotation RENAME COLUMN reviewed TO hod_review;

ALTER TABLE IF EXISTS quotation RENAME COLUMN employee_id TO created_by_id;

ALTER TABLE quotation DROP CONSTRAINT fk45h0o7evaf0hyx029t890rwkl;

ALTER TABLE quotation ADD CONSTRAINT FK_QUOTATION_ON_AUDITOR FOREIGN KEY (auditor_id) REFERENCES employee (id);

ALTER TABLE quotation ADD CONSTRAINT FK_QUOTATION_ON_EMPLOYEE FOREIGN KEY (created_by_id) REFERENCES employee (id);

ALTER TABLE quotation ADD CONSTRAINT FK_QUOTATION_ON_HOD FOREIGN KEY (hod_id) REFERENCES employee (id);
