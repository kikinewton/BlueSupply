-- V24: Add supplier performance and payment aging views for enhanced dashboard metrics.

-- Supplier performance view: aggregates LPO count, average delivery days,
-- and payment completion rate per supplier.
CREATE OR REPLACE VIEW supplier_performance_view AS
SELECT s.id                                                                             AS supplier_id,
       s.name                                                                           AS supplier_name,
       COUNT(DISTINCT lpo.id)                                                           AS total_lpos,
       COALESCE(
               AVG(
                       (EXTRACT(EPOCH FROM grn.created_date) - EXTRACT(EPOCH FROM lpo.created_date))
                           / 86400.0
               )::INTEGER,
               0
       )                                                                                AS avg_delivery_days,
       ROUND(
               COUNT(CASE WHEN p.payment_status = 'COMPLETED' THEN 1 END) * 100.0
                   / NULLIF(COUNT(DISTINCT grn.id), 0),
               2
       )                                                                                AS payment_completion_rate
FROM supplier s
         LEFT JOIN local_purchase_order lpo ON lpo.supplier_id = s.id AND lpo.deleted = false
         LEFT JOIN goods_received_note grn ON grn.local_purchase_order_id = lpo.id
         LEFT JOIN payment p ON p.goods_received_note_id = grn.id AND p.deleted = false
GROUP BY s.id, s.name;

-- Payment aging view: outstanding (PENDING/PARTIAL) payments broken into aging buckets.
CREATE OR REPLACE VIEW payment_aging_analysis AS
SELECT p.id,
       (select s.name from supplier s where s.id = grn.supplier)                      AS supplier_name,
       p.payment_amount,
       p.payment_status,
       p.stage,
       GREATEST(EXTRACT(DAY FROM NOW() - p.created_date)::INTEGER, 0)                 AS days_outstanding,
       CASE
           WHEN EXTRACT(DAY FROM NOW() - p.created_date) <= 30 THEN '0-30 days'
           WHEN EXTRACT(DAY FROM NOW() - p.created_date) <= 60 THEN '31-60 days'
           WHEN EXTRACT(DAY FROM NOW() - p.created_date) <= 90 THEN '61-90 days'
           ELSE '90+ days'
           END                                                                          AS aging_bucket
FROM payment p
         JOIN goods_received_note grn ON grn.id = p.goods_received_note_id
WHERE p.payment_status IN ('PENDING', 'PARTIAL')
  AND p.deleted = false;
