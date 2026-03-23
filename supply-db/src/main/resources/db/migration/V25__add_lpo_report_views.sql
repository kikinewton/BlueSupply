-- V25: Add LPO-focused analytical views for procurement reports and MCP tools.

-- procurement_funnel_view: single-row snapshot of how many items are stuck at each pipeline stage.
CREATE OR REPLACE VIEW procurement_funnel_view AS
SELECT 1                                                                                                  AS id,
       (SELECT COUNT(*)
        FROM request_item
        WHERE endorsement = 'PENDING'
          AND deleted = false)                                                                             AS pending_endorsement,
       (SELECT COUNT(*)
        FROM request_item
        WHERE endorsement = 'ENDORSED'
          AND status = 'PENDING'
          AND deleted = false)                                                                             AS endorsed_pending_processing,
       (SELECT COUNT(*)
        FROM request_item
        WHERE status = 'PROCESSED'
          AND request_review = 'PENDING'
          AND deleted = false)                                                                             AS pending_hod_review,
       (SELECT COUNT(*)
        FROM local_purchase_order_draft
        WHERE id NOT IN (SELECT local_purchase_order_draft_id
                         FROM local_purchase_order
                         WHERE local_purchase_order_draft_id IS NOT NULL)
          AND deleted = false)                                                                             AS lpo_drafts_awaiting_approval,
       (SELECT COUNT(*)
        FROM local_purchase_order lpo
        WHERE lpo.id NOT IN (SELECT local_purchase_order_id FROM goods_received_note)
          AND lpo.deleted = false)                                                                         AS approved_lpos_without_grn;

-- lpo_aging_view: GM-approved LPOs that have not yet received goods, bucketed by days elapsed since LPO creation.
CREATE OR REPLACE VIEW lpo_aging_view AS
SELECT lpo.id,
       lpo.lpo_ref,
       d.name                                                                                             AS department,
       s.name                                                                                             AS supplier_name,
       lpo.created_date,
       GREATEST(CURRENT_DATE - lpo.created_date::date, 0)                                                AS days_without_grn,
       CASE
           WHEN CURRENT_DATE - lpo.created_date::date <= 7 THEN '0-7 days'
           WHEN CURRENT_DATE - lpo.created_date::date <= 14 THEN '8-14 days'
           WHEN CURRENT_DATE - lpo.created_date::date <= 30 THEN '15-30 days'
           ELSE '30+ days'
           END                                                                                            AS aging_bucket
FROM local_purchase_order lpo
         JOIN department d ON d.id = lpo.department_id
         JOIN supplier s ON s.id = lpo.supplier_id
WHERE lpo.id NOT IN (SELECT local_purchase_order_id FROM goods_received_note)
  AND lpo.deleted = false;

-- spend_by_category_view: total spend and item count per request category on processed request items.
CREATE OR REPLACE VIEW spend_by_category_view AS
SELECT rc.id,
       rc.name                           AS category_name,
       COALESCE(SUM(ri.total_price), 0)  AS total_spend,
       COUNT(ri.id)                      AS item_count
FROM request_category rc
         LEFT JOIN request_item ri
                   ON ri.request_category = rc.id
                       AND ri.status = 'PROCESSED'
                       AND ri.deleted = false
GROUP BY rc.id, rc.name;

-- supplier_award_rate_view: per-supplier quotation submission count vs LPOs awarded, with total LPO value.
CREATE OR REPLACE VIEW supplier_award_rate_view AS
SELECT s.id,
       s.name                                                                                              AS supplier_name,
       COUNT(DISTINCT q.id)                                                                               AS quotations_submitted,
       COUNT(DISTINCT lpo.id)                                                                             AS lpos_awarded,
       ROUND(
               COUNT(DISTINCT lpo.id) * 100.0 / NULLIF(COUNT(DISTINCT q.id), 0),
               2
       )                                                                                                  AS award_rate_pct,
       COALESCE(
               (SELECT SUM(ri.total_price)
                FROM local_purchase_order_request_items lri
                         JOIN request_item ri ON ri.id = lri.request_items_id
                         JOIN local_purchase_order l2 ON l2.id = lri.local_purchase_order_id
                WHERE l2.supplier_id = s.id
                  AND l2.deleted = false),
               0
       )                                                                                                  AS total_lpo_value
FROM supplier s
         LEFT JOIN quotation q ON q.supplier_id = s.id AND q.deleted = false
         LEFT JOIN local_purchase_order lpo ON lpo.supplier_id = s.id AND lpo.deleted = false
GROUP BY s.id, s.name;
