DROP TABLE IF EXISTS employee cascade;
DROP TABLE IF EXISTS float cascade;
DROP TABLE IF EXISTS float_order cascade;
DROP TABLE IF EXISTS local_purchase_order cascade;
DROP TABLE IF EXISTS local_purchase_order_draft cascade;

DROP TABLE IF EXISTS petty_cash cascade;
DROP TABLE IF EXISTS request_item cascade;
DROP VIEW IF EXISTS float_aging_analysis cascade;
DROP VIEW IF EXISTS float_payment_report cascade;
DROP VIEW IF EXISTS petty_cash_payment_report cascade;
DROP VIEW IF EXISTS procured_item_report cascade;
DROP VIEW IF EXISTS request_per_current_month_per_department cascade;

DROP TABLE IF EXISTS department cascade;
DROP TABLE IF EXISTS request_item cascade;
DROP TABLE IF EXISTS role cascade;
DROP TABLE IF EXISTS employee cascade;
DROP TABLE IF EXISTS quotation cascade;

DROP TABLE IF EXISTS cancelled_request_item cascade;
DROP TABLE IF EXISTS cancel_payment cascade;
DROP TABLE IF EXISTS employee_role cascade;
DROP TABLE IF EXISTS float_grn cascade;
DROP TABLE IF EXISTS float_grn_comment cascade;
DROP TABLE IF EXISTS float_grn_floats cascade;
DROP TABLE IF EXISTS float_comment cascade;
DROP TABLE IF EXISTS float_order cascade;
DROP TABLE IF EXISTS float_order_supporting_document cascade;
DROP TABLE IF EXISTS float_payment cascade;
DROP TABLE IF EXISTS generated_quote cascade;
DROP TABLE IF EXISTS goods_received_note cascade;
DROP TABLE IF EXISTS goods_received_note_comment cascade;
DROP TABLE IF EXISTS invoice cascade;
DROP TABLE IF EXISTS local_purchase_order cascade;
DROP TABLE IF EXISTS local_purchase_order_request_items cascade;
DROP TABLE IF EXISTS local_purchase_order_draft cascade;
DROP TABLE IF EXISTS local_purchase_order_draft_request_items cascade;
DROP TABLE IF EXISTS payment cascade;
DROP TABLE IF EXISTS payment_draft cascade;
DROP TABLE IF EXISTS payment_draft_comment cascade;
DROP TABLE IF EXISTS payment_schedule cascade;
DROP TABLE IF EXISTS petty_cash cascade;
DROP TABLE IF EXISTS petty_cash_comment cascade;
DROP TABLE IF EXISTS petty_cash_order cascade;
DROP TABLE IF EXISTS petty_cash_order_supporting_document cascade;
DROP TABLE IF EXISTS petty_cash_payment cascade;
DROP TABLE IF EXISTS privilege cascade;

DROP TABLE IF EXISTS quotation_comment cascade;
DROP TABLE IF EXISTS request_item_quotations cascade;
DROP TABLE IF EXISTS request_item_suppliers cascade;
DROP TABLE IF EXISTS request_category cascade;
DROP TABLE IF EXISTS request_document cascade;
DROP TABLE IF EXISTS request_for_quotation cascade;
DROP TABLE IF EXISTS request_item_comment cascade;
DROP TABLE IF EXISTS role cascade;
DROP TABLE IF EXISTS roles_privileges cascade;

DROP TABLE IF EXISTS supplier cascade;
DROP TABLE IF EXISTS supplier_request_map cascade;
DROP TABLE IF EXISTS verification_token cascade;
DROP TABLE IF EXISTS store cascade;
--DROP TABLE IF EXISTS float_grn_floats cascade;
--DROP TABLE IF EXISTS float_payment cascade;
--DROP TABLE IF EXISTS local_purchase_order_request_items cascade;
--DROP TABLE IF EXISTS local_purchase_order_draft_request_items cascade;
--DROP TABLE IF EXISTS payment cascade;


DROP SEQUENCE IF EXISTS hibernate_sequence;