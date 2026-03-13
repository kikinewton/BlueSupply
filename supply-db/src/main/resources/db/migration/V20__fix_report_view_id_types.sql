-- V20: Cast id columns to bigint in report views to match entity mappings (long)
-- payment.id and request_item.id are int4 but entities map them as long (bigint).
-- DROP + CREATE required because PostgreSQL disallows changing a view column type
-- with CREATE OR REPLACE VIEW.
-- payment_report also retains the FULLY_APPROVED filter introduced in V16.

DROP VIEW IF EXISTS public.procured_item_report;
CREATE VIEW public.procured_item_report AS
SELECT
    ri.id::bigint AS id,
    ri.request_item_ref,
    ri.name,
    ri.reason,
    ri.purpose,
    ri.quantity,
    ri.total_price,
    ri.created_date,
    (SELECT e.full_name FROM employee e WHERE e.id = ri.employee_id) AS requested_by,
    (SELECT e.email FROM employee e WHERE e.id = ri.employee_id) AS requested_by_email,
    grn.created_date AS grn_issued_date,
    (SELECT d.name FROM department d WHERE d.id = ri.user_department) AS user_department,
    (SELECT rc.name FROM request_category rc WHERE rc.id = ri.request_category) AS category,
    (SELECT s.name FROM supplier s WHERE s.id = ri.supplied_by) AS supplied_by
FROM request_item ri
JOIN local_purchase_order_request_items lpori ON lpori.request_items_id = ri.id
JOIN goods_received_note grn ON grn.local_purchase_order_id = lpori.local_purchase_order_id
WHERE ri.deleted = false;

DROP VIEW IF EXISTS public.payment_report;
CREATE VIEW public.payment_report AS
SELECT
    p.id::bigint AS id,
    (SELECT s.name FROM supplier s WHERE s.id = grn.supplier) AS supplier,
    (SELECT i.invoice_number FROM invoice i WHERE i.id = grn.invoice_id) AS invoice_no,
    (SELECT s.account_number FROM supplier s WHERE s.id = grn.supplier) AS account_number,
    (SELECT e.full_name FROM employee e WHERE e.id = p.employee_gm_id) AS approved_by_gm,
    (SELECT e.full_name FROM employee e WHERE e.id = p.employee_fm_id) AS verified_by_fm,
    (SELECT e.full_name FROM employee e WHERE e.id = p.employee_auditor_id) AS checked_by_auditor,
    p.cheque_number,
    p.purchase_number,
    date(grn.payment_date) AS payment_due_date,
    p.payment_amount AS payable_amount,
    p.withholding_tax_percentage,
    p.withholding_tax_amount,
    p.payment_status,
    date(p.created_date) AS payment_date
FROM payment p
JOIN goods_received_note grn ON p.goods_received_note_id = grn.id
WHERE p.stage = 'FULLY_APPROVED'
  AND p.deleted = false;
