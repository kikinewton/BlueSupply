-- V18: Cast float_payment_report view id to bigint to match entity mapping (long)
-- Must DROP and recreate because PostgreSQL disallows changing a view column type
-- with CREATE OR REPLACE VIEW.
DROP VIEW IF EXISTS public.float_payment_report;
CREATE VIEW public.float_payment_report AS
WITH cte_emp AS (
    SELECT e.id, e.full_name FROM employee e
),
cte_fp AS (
    SELECT fp.id, fp.amount, fp.created_date, fp.floats_id, fp.paid_by_id FROM float_payment fp
),
cte_flo AS (
    SELECT fo_1.id, fo_1.amount, fo_1.approval, fo_1.approval_date,
           fo_1.auditor_retirement_approval, fo_1.auditor_retirement_approval_date,
           fo_1.created_date, fo_1.deleted, fo_1.description, fo_1.endorsement,
           fo_1.endorsement_date, fo_1.flagged, fo_1.float_order_ref, fo_1.funds_received,
           fo_1.gm_retirement_approval, fo_1.gm_retirement_approval_date, fo_1.has_document,
           fo_1.requested_by, fo_1.requested_by_email, fo_1.requested_by_phone_no,
           fo_1.retired, fo_1.retirement_date, fo_1.staff_id, fo_1.status,
           fo_1.created_by_id, fo_1.department_id, fo_1.approved_by, fo_1.endorsed_by,
           fo_1.float_type
    FROM float_order fo_1
)
SELECT
    fo.id::bigint AS id,
    fo.float_order_ref,
    fo.amount AS requested_amount,
    fo.float_type,
    cte_fp.amount AS paid_amount,
    fo.created_date AS requested_date,
    fo.staff_id AS requester_staff_id,
    fo.requested_by,
    (SELECT cte_emp.full_name FROM cte_emp WHERE cte_emp.id = fo.created_by_id) AS created_by,
    (SELECT d.name FROM department d WHERE d.id = fo.department_id) AS department,
    fo.endorsement_date,
    (SELECT COALESCE(e.full_name, ''::character varying) FROM cte_emp e WHERE e.id = fo.endorsed_by) AS endorsed_by,
    fo.approval_date,
    (SELECT COALESCE(e.full_name, ''::character varying) FROM cte_emp e WHERE e.id = fo.approved_by) AS approved_by,
    cte_fp.created_date AS funds_allocated_date,
    (SELECT e.full_name FROM employee e WHERE e.id = cte_fp.paid_by_id) AS account_officer,
    fo.retirement_date,
    fo.retired
FROM cte_flo fo
JOIN cte_fp ON fo.id = cte_fp.floats_id;
