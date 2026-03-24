-- Seed data only. Schema (tables, views, sequences) is managed by Flyway.

INSERT INTO public.role VALUES (1, 'ROLE_ADMIN');
INSERT INTO public.role VALUES (2, 'ROLE_REGULAR');
INSERT INTO public.role VALUES (3, 'ROLE_HOD');
INSERT INTO public.role VALUES (4, 'ROLE_GENERAL_MANAGER');
INSERT INTO public.role VALUES (5, 'ROLE_PROCUREMENT_OFFICER');
INSERT INTO public.role VALUES (6, 'ROLE_STORE_OFFICER');
INSERT INTO public.role VALUES (7, 'ROLE_ACCOUNT_OFFICER');
INSERT INTO public.role VALUES (8, 'ROLE_CHIEF_ACCOUNT_OFFICER');
INSERT INTO public.role VALUES (9, 'ROLE_PROCUREMENT_MANAGER');
INSERT INTO public.role VALUES (10, 'ROLE_FINANCIAL_MANAGER');
INSERT INTO public.role VALUES (11, 'ROLE_AUDITOR');
INSERT INTO public.role VALUES (12, 'ROLE_STORE_MANAGER');

INSERT INTO public.privilege VALUES (3, 'READ_PRIVILEGE');
INSERT INTO public.privilege VALUES (4, 'WRITE_PRIVILEGE');

INSERT INTO public.roles_privileges VALUES (1, 3);
INSERT INTO public.roles_privileges VALUES (1, 4);
INSERT INTO public.roles_privileges VALUES (2, 3);
INSERT INTO public.roles_privileges VALUES (2, 4);
INSERT INTO public.roles_privileges VALUES (3, 3);
INSERT INTO public.roles_privileges VALUES (3, 4);
INSERT INTO public.roles_privileges VALUES (4, 3);
INSERT INTO public.roles_privileges VALUES (4, 4);
INSERT INTO public.roles_privileges VALUES (5, 3);
INSERT INTO public.roles_privileges VALUES (5, 4);
INSERT INTO public.roles_privileges VALUES (6, 3);
INSERT INTO public.roles_privileges VALUES (6, 4);
INSERT INTO public.roles_privileges VALUES (7, 3);
INSERT INTO public.roles_privileges VALUES (7, 4);
INSERT INTO public.roles_privileges VALUES (8, 3);
INSERT INTO public.roles_privileges VALUES (8, 4);
INSERT INTO public.roles_privileges VALUES (9, 3);
INSERT INTO public.roles_privileges VALUES (9, 4);
INSERT INTO public.roles_privileges VALUES (10, 3);
INSERT INTO public.roles_privileges VALUES (10, 4);
INSERT INTO public.roles_privileges VALUES (11, 3);
INSERT INTO public.roles_privileges VALUES (11, 4);

INSERT INTO department (id, name, description) VALUES (10, 'Culinary', 'Catering for employees');
INSERT INTO department (id, name, description) VALUES (11, 'IT', 'IT department');
INSERT INTO department(id, created_date, description, "name", updated_date) VALUES (9, '2022-07-07 13:32:18.474', 'Procurement Department', 'Procurement', NULL);

INSERT INTO employee (id, changed_default_password, created_at, deleted, email, enabled, first_name, full_name, last_login, last_name, "password", phone_no, updated_at, department_id)
VALUES(100, false, '2023-06-11 09:30:59.179', false, 'derrickagyemang12@outlook.com', false, 'Super', 'Super Admin', NULL, 'Admin', '$2a$10$0YozPuPfeu2pYK5jUEq7Outf.240hM.j/ny.kdyRiNqoAIXP2FRKG', '000000000000', '2023-06-11 09:31:14.899', 10);

INSERT INTO employee (id, changed_default_password, created_at, deleted, email, enabled, first_name, full_name, last_login, last_name, "password", phone_no, updated_at, department_id)
VALUES(2, false, '2023-06-11 09:30:59.179', false, 'kikinewton@gmail.com', true, 'Kiki', 'Kiki Newton', NULL, 'Newton', '$2a$10$0YozPuPfeu2pYK5jUEq7Outf.240hM.j/ny.kdyRiNqoAIXP2FRKG', '00000000061', '2023-06-11 09:31:14.899', 10);

INSERT INTO employee (id, changed_default_password, created_at, deleted, email, enabled, first_name, full_name, last_login, last_name, "password", phone_no, updated_at, department_id)
VALUES(3, false, '2023-06-11 09:30:59.179', false, 'chulk@mail.com', true, 'Mark', 'Mark Freeman', NULL, 'Freeman', '$2a$10$0YozPuPfeu2pYK5jUEq7Outf.240hM.j/ny.kdyRiNqoAIXP2FRKG', '000000000081', '2023-06-11 09:31:14.899', 10);

INSERT INTO public.employee
(id, changed_default_password, created_at, deleted, email, enabled, first_name, full_name, last_login, last_name, "password", phone_no, updated_at, department_id)
VALUES(9, false, '2022-07-07 13:48:45.458', false, 'eric.mensah@blueskies.com', true, 'Eric', 'Eric Kojo Mensah', '2023-06-15 08:26:56.192', 'Kojo Mensah', '$2a$10$PiLnRm4AvRMRF78maDZQi.T.Kwj6koDNj4uAokDW0jvrFW15AdG3O', '0000000000', '2022-07-07 13:50:23.480', 9);

-- Additional employees for role-specific integration tests
INSERT INTO employee (id, changed_default_password, created_at, deleted, email, enabled, first_name, full_name, last_name, password, phone_no, updated_at, department_id)
VALUES(10, true, NOW(), false, 'account.officer@test.com', true, 'Account', 'Account Officer', 'Officer', '$2a$10$0YozPuPfeu2pYK5jUEq7Outf.240hM.j/ny.kdyRiNqoAIXP2FRKG', '0550000001', NOW(), 10);

INSERT INTO employee (id, changed_default_password, created_at, deleted, email, enabled, first_name, full_name, last_name, password, phone_no, updated_at, department_id)
VALUES(11, true, NOW(), false, 'auditor@test.com', true, 'Audit', 'Auditor User', 'User', '$2a$10$0YozPuPfeu2pYK5jUEq7Outf.240hM.j/ny.kdyRiNqoAIXP2FRKG', '0550000002', NOW(), 10);

INSERT INTO employee (id, changed_default_password, created_at, deleted, email, enabled, first_name, full_name, last_name, password, phone_no, updated_at, department_id)
VALUES(12, true, NOW(), false, 'fm@test.com', true, 'Finance', 'Finance Manager', 'Manager', '$2a$10$0YozPuPfeu2pYK5jUEq7Outf.240hM.j/ny.kdyRiNqoAIXP2FRKG', '0550000003', NOW(), 10);

INSERT INTO employee (id, changed_default_password, created_at, deleted, email, enabled, first_name, full_name, last_name, password, phone_no, updated_at, department_id)
VALUES(13, true, NOW(), false, 'gm@test.com', true, 'General', 'General Manager', 'Manager', '$2a$10$0YozPuPfeu2pYK5jUEq7Outf.240hM.j/ny.kdyRiNqoAIXP2FRKG', '0550000004', NOW(), 10);

INSERT INTO employee (id, changed_default_password, created_at, deleted, email, enabled, first_name, full_name, last_name, password, phone_no, updated_at, department_id)
VALUES(14, true, NOW(), false, 'hod.it@test.com', true, 'IT', 'IT HOD', 'HOD', '$2a$10$0YozPuPfeu2pYK5jUEq7Outf.240hM.j/ny.kdyRiNqoAIXP2FRKG', '0550000005', NOW(), 11);

INSERT INTO public.employee_role (employee_id, role_id) VALUES(3, 3);
INSERT INTO public.employee_role (employee_id, role_id) VALUES(9, 9);
INSERT INTO public.employee_role (employee_id, role_id) VALUES(10, 7);   -- ROLE_ACCOUNT_OFFICER
INSERT INTO public.employee_role (employee_id, role_id) VALUES(11, 11);  -- ROLE_AUDITOR
INSERT INTO public.employee_role (employee_id, role_id) VALUES(12, 10);  -- ROLE_FINANCIAL_MANAGER
INSERT INTO public.employee_role (employee_id, role_id) VALUES(13, 4);   -- ROLE_GENERAL_MANAGER
INSERT INTO public.employee_role (employee_id, role_id) VALUES(14, 3);   -- ROLE_HOD for IT department

INSERT INTO public.store (id, created_by_id, created_date, last_modified_by_id, last_modified_date, "name", deleted)
VALUES(100, 100, NOW(), NULL, NULL, 'Engineering store', false);

INSERT INTO public.store (id, created_by_id, created_date, last_modified_by_id, last_modified_date, "name", deleted)
VALUES(101, 100, NOW(), NULL, NULL, 'Redundant store', true);

INSERT INTO public.verification_token (created_date, email, expiry_date, "token", verification_type) VALUES (NOW(), 'kikinewton@gmail.com', NOW() + INTERVAL '1 day', 'c2d297-3d0bKd497', 'PASSWORD_RESET');

INSERT INTO public.supplier (id, created_date, last_modified_date, account_number, bank, description, email, "location", "name", phone_no, registered, created_by_id, last_modified_by_id)
VALUES(1, NOW(), NOW(), NULL, NULL, 'IT Equipments', NULL, NULL, 'Jilorm Ventures', '0000000000', false, 100, 100);

INSERT INTO public.supplier
(id, created_date, last_modified_date, account_number, bank, description, email, "location", "name", phone_no, registered, created_by_id, last_modified_by_id)
VALUES(2, NOW(), NOW(), NULL, NULL, 'Internet Service Provider', 'rand19@mail.com', 'Accra', 'Ginet Technology Limited', '88377288192', true, 100, 100);

INSERT INTO public.request_document
(id, created_date, last_modified_date, document_format, document_type, file_name, created_by_id, last_modified_by_id)
VALUES(100, NOW(), NOW(), 'pdf', 'pdf', 'test', 100, 100);

INSERT INTO public.request_document
(id, created_date, last_modified_date, document_format, document_type, file_name, created_by_id, last_modified_by_id)
VALUES(101, NOW(), NOW(), 'pdf', 'pdf', 'test', 100, 100);

INSERT INTO public.request_category
(id, created_date, description, "name", updated_date)
VALUES(100, NOW(), 'IT Related Items', 'IT Items', NULL);

INSERT INTO public.request_item (id, approval, approval_date, created_date, currency, deleted, endorsement, endorsement_date, "name", priority_level, purpose, quantity, reason, request_date, request_item_ref, request_review, request_type, status, supplied_by, total_price, unit_price, updated_date, employee_id, request_category, user_department, grn_id, receiving_store_id)
VALUES(100, 'PENDING', NULL, NOW(), NULL, false, 'PENDING', NULL, '1 BUCKET OF RED OXIDE PAINT', 'NORMAL', 'SITE DRAIN COVERS', 1, 'FreshNeed', '2022-12-09 15:26:05.331', 'RQI-TRA-00000314-912', NULL, 'GOODS_REQUEST', 'PENDING', NULL, 0.00, 0.00, NOW(), 100, NULL, 11, NULL, 100);

INSERT INTO public.request_item (id, approval, approval_date, created_date, currency, deleted, endorsement, endorsement_date, "name", priority_level, purpose, quantity, reason, request_date, request_item_ref, request_review, request_type, status, supplied_by, total_price, unit_price, updated_date, employee_id, request_category, user_department, grn_id, receiving_store_id)
VALUES(101, 'PENDING', NULL, '2022-11-15 13:15:31.108', NULL, false, 'ENDORSED', '2022-11-15 15:35:24.016', 'TILT COUPLER ([FEMALE AND MALE)', 'NORMAL', 'GR 8053-14 AND GR 7006-18', 4, 'Replace', '2022-11-15 13:15:31.107', 'RQI-TRA-00000110-1511', NULL, 'GOODS_REQUEST', 'PENDING', NULL, 0.00, 0.00, '2022-11-18 11:56:59.721', 100, NULL, 11, NULL, 100);

INSERT INTO public.request_item (id, approval, approval_date, created_date, currency, deleted, endorsement, endorsement_date, "name", priority_level, purpose, quantity, reason, request_date, request_item_ref, request_review, request_type, status, supplied_by, total_price, unit_price, updated_date, employee_id, request_category, user_department, grn_id, receiving_store_id)
VALUES(102, 'PENDING', NULL, NOW(), NULL, false, 'COMMENT', NULL, '1 BUCKET OF RED OIL PAINT', 'NORMAL', 'SITE DRAIN COVERS', 1, 'FreshNeed', '2022-12-09 15:26:05.331', 'RQI-TRA-00000514-021', NULL, 'GOODS_REQUEST', 'PENDING', NULL, 0.00, 0.00, NOW(), 2, NULL, 11, NULL, 100);

INSERT INTO public.request_item
(id, approval, approval_date, created_date, currency, deleted, endorsement, endorsement_date, "name", priority_level, purpose, quantity, reason, request_date, request_item_ref, request_review, request_type, status, supplied_by, total_price, unit_price, updated_date, employee_id, request_category, user_department, grn_id, receiving_store_id)
VALUES(103, 'PENDING', NULL, '2022-10-07 12:48:39.669', NULL, false, 'ENDORSED', NOW(), 'Flap Disc', 'NORMAL', 'Flap Disc', 10, 'FreshNeed', '2022-10-07 12:48:39.601', 'RQI-ENG-00000071-710', NULL, 'GOODS_REQUEST', 'PENDING', NULL, 0.00, 0.00, '2022-10-12 13:16:54.536', 2, NULL, 10, NULL, 100);

INSERT INTO public.request_item
(id, approval, approval_date, created_date, currency, deleted, endorsement, endorsement_date, "name", priority_level, purpose, quantity, reason, request_date, request_item_ref, request_review, request_type, status, supplied_by, total_price, unit_price, updated_date, employee_id, request_category, user_department, grn_id, receiving_store_id)
VALUES(104, 'PENDING', NULL, '2022-10-07 12:48:39.669', NULL, false, 'ENDORSED', NOW(), 'Fridge', 'NORMAL', 'Official', 10, 'FreshNeed', '2022-10-07 12:48:39.601', 'RQI-ENG-00202071-710', NULL, 'GOODS_REQUEST', 'PENDING', 1, 100.00, 10.00, '2022-10-12 13:16:54.536', 2, NULL, 10, NULL, 100);

INSERT INTO public.request_item_suppliers (request_id, supplier_id) VALUES(101, 1);
INSERT INTO public.request_item_suppliers (request_id, supplier_id) VALUES(104, 1);

INSERT INTO public.quotation
(id, created_at, deleted, expired, linked_to_lpo, quotation_ref, hod_review, created_by_id, request_document_id, supplier_id)
VALUES(100, NOW(), false, false, false, 'QUO-NSA-00000036-1411', false, 100, 100, 1);

INSERT INTO public.quotation
(id, created_at, deleted, expired, linked_to_lpo, quotation_ref, hod_review, created_by_id, request_document_id, supplier_id)
VALUES(101, NOW(), false, false, true, 'QUO-NSA-00000020-1111', false, 100, 101, 2);

INSERT INTO public.quotation
(id, created_at, deleted, expired, linked_to_lpo, quotation_ref, hod_review, created_by_id, request_document_id, supplier_id)
VALUES(102, NOW(), false, false, true, 'QUO-NSA-00030026-1111', false, 100, 101, 2);

INSERT INTO public.quotation
(id, created_at, deleted, expired, linked_to_lpo, quotation_ref, hod_review, created_by_id, request_document_id, supplier_id)
VALUES(110, NOW(), false, false, false, 'QUO-PSA-00830026-1111', true, 100, 101, 2);

INSERT INTO public.quotation
(id, created_at, deleted, expired, linked_to_lpo, quotation_ref, hod_review, created_by_id, request_document_id, supplier_id)
VALUES(111, NOW(), false, false, true, 'QUO-PSA-00872126-1111', true, 100, 101, 2);

INSERT INTO public.quotation_comment
(id, created_date, description, process_with_comment, updated_date, employee_id, quotation_id)
VALUES(100, '2022-12-01 13:13:49.440', 'I believe this has to be reviewed by Seth as it is IT related.', 'REVIEW_QUOTATION_HOD', '2022-12-01 13:13:49.440', 100, 111);

INSERT INTO public.request_item_quotations (request_item_id, quotation_id) VALUES(100, 100);
INSERT INTO public.request_item_quotations (request_item_id, quotation_id) VALUES(103, 101);
INSERT INTO public.request_item_quotations (request_item_id, quotation_id) VALUES(103, 111);

INSERT INTO public.petty_cash_order
(id, created_date, last_modified_date, petty_cash_order_ref, requested_by, requested_by_phone_no, staff_id, created_by_id, last_modified_by_id)
VALUES(100, '2023-07-06 02:07:50.592', '2023-07-06 02:07:50.592', 'PTC-OIT-00000001-67', 'James ', '', '3oij', 100, 100);

INSERT INTO public.petty_cash_order
(id, created_date, last_modified_date, petty_cash_order_ref, requested_by, requested_by_phone_no, staff_id, created_by_id, last_modified_by_id)
VALUES(101, '2023-07-07 21:59:07.755', '2023-07-07 21:59:07.755', 'PTC-OIT-00000002-77', 'Jeff', '', 'Ps33', 100, 100);

INSERT INTO public.petty_cash
(id, amount, approval, approval_date, created_date, deleted, endorsement, endorsement_date, "name", paid, petty_cash_ref, purpose, quantity, staff_id, status, updated_date, created_by, department_id, petty_cash_order_id)
VALUES(100, 80.00, 'PENDING', NULL, '2023-07-06 02:07:50.627', false, 'PENDING', NULL, 'Brake fluid', false, 'PTC-OIT-00000001-67', 'Official use', 1, '3oij', 'PENDING', '2023-07-06 02:07:50.627', 100, 10, 100);

INSERT INTO public.petty_cash
(id, amount, approval, approval_date, created_date, deleted, endorsement, endorsement_date, "name", paid, petty_cash_ref, purpose, quantity, staff_id, status, updated_date, created_by, department_id, petty_cash_order_id)
VALUES(101, 500.00, 'PENDING', NULL, '2023-07-07 21:59:07.909', false, 'ENDORSED', '2023-07-07 22:16:59.712', 'Table', false, 'PTC-OIT-00000002-77', 'Official use', 1, 'Ps33', 'PENDING', '2023-07-07 22:16:59.765', 100, 10, 101);

-- Minimal GRN for payment draft tests (no LPO or invoice required)
INSERT INTO goods_received_note (id, approved_by_hod, supplier, created_by_id, created_date, updated_date, invoice_amount_payable)
VALUES(100, false, 1, 100, NOW(), NOW(), 5000.00);

-- Seeded payment (as DRAFT stage) for listing and approval tests
INSERT INTO payment (id, deleted, purchase_number, withholding_tax_amount, withholding_tax_percentage, payment_amount, payment_method, bank, cheque_number, created_by_id, goods_received_note_id, payment_status, stage, created_date)
VALUES(100, false, 'PO-TEST-001', 0.00, 0.00, 5000.00, 'CHEQUE', 'GCB Bank', 'CHQ-TEST-001', 10, 100, 'PARTIAL', 'DRAFT', NOW());

-- -----------------------------------------------------------------------
-- Request item status tracking test data
-- Scenario A: approved request item with LPO only (id=105)
-- Scenario B: approved request item with LPO + HOD-approved GRN (id=106)
-- Scenario C: approved request item with LPO + GRN + in-progress payment (id=107)
-- -----------------------------------------------------------------------

-- Approved request items
INSERT INTO public.request_item (id, approval, approval_date, created_date, currency, deleted, endorsement, endorsement_date, name, priority_level, purpose, quantity, reason, request_date, request_item_ref, request_review, request_type, status, supplied_by, total_price, unit_price, updated_date, employee_id, request_category, user_department, grn_id, receiving_store_id)
VALUES(105, 'APPROVED', NOW(), NOW(), NULL, false, 'ENDORSED', NOW(), 'Test Item LPO Only', 'NORMAL', 'Test', 1, 'FreshNeed', NOW(), 'RQI-TEST-00000105', NULL, 'GOODS_REQUEST', 'PROCESSED', 1, 100.00, 100.00, NOW(), 100, NULL, 11, NULL, 100);

INSERT INTO public.request_item (id, approval, approval_date, created_date, currency, deleted, endorsement, endorsement_date, name, priority_level, purpose, quantity, reason, request_date, request_item_ref, request_review, request_type, status, supplied_by, total_price, unit_price, updated_date, employee_id, request_category, user_department, grn_id, receiving_store_id)
VALUES(106, 'APPROVED', NOW(), NOW(), NULL, false, 'ENDORSED', NOW(), 'Test Item GRN Stage', 'NORMAL', 'Test', 1, 'FreshNeed', NOW(), 'RQI-TEST-00000106', NULL, 'GOODS_REQUEST', 'PROCESSED', 1, 100.00, 100.00, NOW(), 100, NULL, 11, NULL, 100);

INSERT INTO public.request_item (id, approval, approval_date, created_date, currency, deleted, endorsement, endorsement_date, name, priority_level, purpose, quantity, reason, request_date, request_item_ref, request_review, request_type, status, supplied_by, total_price, unit_price, updated_date, employee_id, request_category, user_department, grn_id, receiving_store_id)
VALUES(107, 'APPROVED', NOW(), NOW(), NULL, false, 'ENDORSED', NOW(), 'Test Item Payment Stage', 'NORMAL', 'Test', 1, 'FreshNeed', NOW(), 'RQI-TEST-00000107', NULL, 'GOODS_REQUEST', 'PROCESSED', 1, 100.00, 100.00, NOW(), 100, NULL, 11, NULL, 100);

-- LPOs (one per approved request item)
INSERT INTO local_purchase_order (id, deleted, supplier_id, created_at, created_date, last_modified_date, updated_date)
VALUES(100, false, 1, NOW(), NOW(), NOW(), NOW());

INSERT INTO local_purchase_order (id, deleted, supplier_id, created_at, created_date, last_modified_date, updated_date)
VALUES(101, false, 1, NOW(), NOW(), NOW(), NOW());

INSERT INTO local_purchase_order (id, deleted, supplier_id, created_at, created_date, last_modified_date, updated_date)
VALUES(102, false, 1, NOW(), NOW(), NOW(), NOW());

-- Link LPOs to request items
INSERT INTO local_purchase_order_request_items (local_purchase_order_id, request_items_id) VALUES(100, 105);
INSERT INTO local_purchase_order_request_items (local_purchase_order_id, request_items_id) VALUES(101, 106);
INSERT INTO local_purchase_order_request_items (local_purchase_order_id, request_items_id) VALUES(102, 107);

-- GRN for Scenario B (HOD approved) and C (with payment)
INSERT INTO goods_received_note (id, approved_by_hod, supplier, created_by_id, created_date, updated_date, invoice_amount_payable, local_purchase_order_id, date_of_approval_by_hod)
VALUES(101, true, 1, 100, NOW(), NOW(), 5000.00, 101, NOW());

INSERT INTO goods_received_note (id, approved_by_hod, supplier, created_by_id, created_date, updated_date, invoice_amount_payable, local_purchase_order_id)
VALUES(102, false, 1, 100, NOW(), NOW(), 5000.00, 102);

-- In-progress payment linked to GRN 102 (Scenario C)
INSERT INTO payment (id, deleted, purchase_number, withholding_tax_amount, withholding_tax_percentage, payment_amount, payment_method, bank, cheque_number, created_by_id, goods_received_note_id, payment_status, stage, created_date)
VALUES(101, false, 'PO-TEST-002', 0.00, 0.00, 5000.00, 'CHEQUE', 'GCB Bank', 'CHQ-TEST-002', 10, 102, 'PARTIAL', 'DRAFT', NOW());

-- Advance sequences past seeded IDs so test inserts don't collide with seed data.
-- TRUNCATE...RESTART IDENTITY resets SERIAL sequences to 1 but explicit-ID inserts don't advance them.
-- Standalone sequences (supplier_seq etc.) are not reset by TRUNCATE; reset here to a safe baseline.

-- SERIAL tables (sequences owned by column, reset by TRUNCATE RESTART IDENTITY)
SELECT setval(pg_get_serial_sequence('role', 'id'), MAX(id)) FROM role HAVING MAX(id) IS NOT NULL;
SELECT setval(pg_get_serial_sequence('privilege', 'id'), MAX(id)) FROM privilege HAVING MAX(id) IS NOT NULL;
SELECT setval(pg_get_serial_sequence('department', 'id'), MAX(id)) FROM department HAVING MAX(id) IS NOT NULL;
SELECT setval(pg_get_serial_sequence('employee', 'id'), MAX(id)) FROM employee HAVING MAX(id) IS NOT NULL;
SELECT setval(pg_get_serial_sequence('request_category', 'id'), MAX(id)) FROM request_category HAVING MAX(id) IS NOT NULL;
SELECT setval(pg_get_serial_sequence('request_item', 'id'), MAX(id)) FROM request_item HAVING MAX(id) IS NOT NULL;
SELECT setval(pg_get_serial_sequence('quotation', 'id'), MAX(id)) FROM quotation HAVING MAX(id) IS NOT NULL;
SELECT setval(pg_get_serial_sequence('quotation_comment', 'id'), MAX(id)) FROM quotation_comment HAVING MAX(id) IS NOT NULL;
SELECT setval(pg_get_serial_sequence('petty_cash', 'id'), MAX(id)) FROM petty_cash HAVING MAX(id) IS NOT NULL;
SELECT setval(pg_get_serial_sequence('goods_received_note', 'id'), MAX(id)) FROM goods_received_note HAVING MAX(id) IS NOT NULL;

-- Standalone sequences (not linked to a column; not reset by TRUNCATE RESTART IDENTITY)
-- Set to 1000 so the next generated ID (1001+ for inc=1, 1050+ for inc=50) is well above all seeded IDs.
SELECT setval('supplier_seq', 1000);
SELECT setval('payment_seq', 1000);
SELECT setval('store_seq', 1000);
SELECT setval('petty_cash_order_seq', 1000);
SELECT setval('request_document_seq', 1000);
