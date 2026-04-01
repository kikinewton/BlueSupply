-- V16: Update payment_report view to show only FULLY_APPROVED payments
-- This preserves the pre-migration behavior where only settled payments
-- appeared in financial reports.

DROP VIEW IF EXISTS public.payment_report;
CREATE VIEW public.payment_report AS
SELECT
    p.id,
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
