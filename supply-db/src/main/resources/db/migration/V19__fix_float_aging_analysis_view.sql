-- V19: Update float_aging_analysis view to expose retired column
-- The entity has a retired field that must exist in the view for Hibernate schema validation.
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
