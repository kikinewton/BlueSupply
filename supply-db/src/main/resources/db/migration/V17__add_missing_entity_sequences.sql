-- V17: Create sequences for entities using AbstractAuditable (GenerationType.AUTO)
-- Hibernate 6 requires named sequences ({table}_seq) for each entity using AUTO generation.
-- All sequences use INCREMENT BY 50 to match Hibernate's default allocationSize.

CREATE SEQUENCE IF NOT EXISTS public.cancel_payment_seq START WITH 1 INCREMENT BY 50 NO MINVALUE NO MAXVALUE CACHE 1;
CREATE SEQUENCE IF NOT EXISTS public.invoice_seq START WITH 1 INCREMENT BY 50 NO MINVALUE NO MAXVALUE CACHE 1;
CREATE SEQUENCE IF NOT EXISTS public.local_purchase_order_seq START WITH 1 INCREMENT BY 50 NO MINVALUE NO MAXVALUE CACHE 1;
CREATE SEQUENCE IF NOT EXISTS public.local_purchase_order_draft_seq START WITH 1 INCREMENT BY 50 NO MINVALUE NO MAXVALUE CACHE 1;
CREATE SEQUENCE IF NOT EXISTS public.payment_seq START WITH 1 INCREMENT BY 50 NO MINVALUE NO MAXVALUE CACHE 1;
CREATE SEQUENCE IF NOT EXISTS public.supplier_seq START WITH 1 INCREMENT BY 50 NO MINVALUE NO MAXVALUE CACHE 1;
