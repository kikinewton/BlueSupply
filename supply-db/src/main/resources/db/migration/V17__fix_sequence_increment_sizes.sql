-- V17: Align sequence increment sizes with Hibernate's default allocationSize (50)
-- Hibernate 6 validates that the DB sequence INCREMENT matches the entity allocationSize.
-- These sequences were created with INCREMENT BY 1 but the JPA mappings expect 50.

ALTER SEQUENCE IF EXISTS public.store_seq INCREMENT BY 50;
ALTER SEQUENCE IF EXISTS public.petty_cash_order_seq INCREMENT BY 50;
ALTER SEQUENCE IF EXISTS public.request_document_seq INCREMENT BY 50;
ALTER SEQUENCE IF EXISTS public.generated_quote_seq INCREMENT BY 50;
