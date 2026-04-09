-- float_payment_report and float_aging_analysis reference float_order_ref;
-- petty_cash_payment_report references petty_cash_ref.
-- All three must be dropped before the column types can be altered.

DROP VIEW IF EXISTS public.float_payment_report;
DROP VIEW IF EXISTS public.float_aging_analysis;
DROP VIEW IF EXISTS public.petty_cash_payment_report;

ALTER TABLE float_order ALTER COLUMN float_order_ref TYPE varchar(50);
ALTER TABLE petty_cash  ALTER COLUMN petty_cash_ref  TYPE varchar(50);
ALTER TABLE petty_cash_order  ALTER COLUMN petty_cash_order_ref  TYPE varchar(50);


-- Recreate float_aging_analysis (from V19)
CREATE OR REPLACE VIEW public.float_aging_analysis AS
SELECT
    f.float_order_ref AS float_ref,
    upper(f.staff_id::text) AS staff_id,
    upper(f.description::text) AS item_description,
    f.amount AS estimated_amount,
    (SELECT upper(d.name::text) FROM department d WHERE d.id = f.department_id) AS department,
    (SELECT upper(e.full_name::text) FROM employee e WHERE e.id = f.created_by_id) AS employee,
    upper(f.requested_by::text) AS requested_by,
    upper(f.requested_by_phone_no::text) AS requested_by_phone_no,
    upper(f.requested_by_email::text) AS requested_by_email,
    f.created_date,
    (SELECT CURRENT_DATE - f.created_date) AS ageing_value,
    f.retired
FROM float_order f
WHERE f.deleted = false;

-- Recreate float_payment_report (from V18)
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

-- Recreate petty_cash_payment_report (from V1)
CREATE OR REPLACE VIEW public.petty_cash_payment_report AS
WITH pc_cte AS (
    SELECT pc.id, pc.amount, pc.approval, pc.approval_date, pc.created_date,
           pc.endorsement, pc.endorsement_date, pc.name, pc.paid, pc.petty_cash_ref,
           pc.purpose, pc.quantity, pc.status, pc.updated_date, pc.created_by,
           pc.department_id, pc.petty_cash_order_id
    FROM petty_cash pc
    WHERE deleted = false
),
emp_cte AS (
    SELECT e.id, e.created_at, e.email, e.enabled, e.first_name, e.full_name,
           e.last_login, e.last_name, e.password, e.phone_no, e.updated_at,
           e.department_id, e.changed_default_password
    FROM employee e
),
dep_cte AS (
    SELECT d2.id, d2.created_date, d2.description, d2.name, d2.updated_date
    FROM department d2
),
pyt_cte AS (
    SELECT pcp.id, pcp.amount, pcp.created_date, pcp.paid_by_id, pcp.petty_cash_id
    FROM petty_cash_payment pcp
)
SELECT
    date(pyt_cte.created_date) AS payment_date,
    pc_cte.name AS petty_cash_description,
    pc_cte.petty_cash_ref,
    pc_cte.purpose,
    pc_cte.quantity,
    pc_cte.amount,
    pc_cte.quantity::numeric * pc_cte.amount AS total_cost,
    emp_cte.full_name AS requested_by,
    emp_cte.email AS requested_by_email,
    dep_cte.name AS department,
    (SELECT emp_cte_1.full_name FROM emp_cte emp_cte_1 WHERE emp_cte_1.id = pyt_cte.paid_by_id) AS paid_by
FROM pc_cte
JOIN emp_cte ON pc_cte.created_by = emp_cte.id
JOIN dep_cte ON pc_cte.department_id = dep_cte.id
JOIN pyt_cte ON pc_cte.id = pyt_cte.petty_cash_id;