# Draft: Numeric ID → UUID Migration Analysis for BlueSupply

---

## Context

BlueSupply currently uses auto-incrementing numeric (`BIGINT`/`SERIAL`) primary keys across all 47 JPA entities, backed by PostgreSQL sequences. There is existing production data with established numeric IDs and foreign key relationships across a large relational schema (the V1 migration alone establishes most of the core schema). This draft evaluates whether migrating to UUIDs is worth pursuing.

---

## What the Migration Actually Involves

This is not a simple version bump. It touches every layer of the stack:

1. **Database** — alter primary key column types on all entity tables, update all foreign key columns referencing them, drop and recreate indexes and constraints, migrate existing numeric values to UUID equivalents (requires a mapping strategy)
2. **JPA Entities** — change `@Id` field types from `Long` to `UUID`, update `@GeneratedValue` strategy
3. **DTOs** — all 85 DTO classes that expose or accept an `id` field change type
4. **Repositories** — all 45 repositories change from `JpaRepository<Entity, Long>` to `JpaRepository<Entity, UUID>`
5. **Service layer** — all 42 service classes that pass IDs as method arguments
6. **Controllers** — all 22 controllers that accept IDs as `@PathVariable` or request parameters
7. **Flyway migrations** — new migration scripts for the schema changes and data migration
8. **API contracts** — any client (frontend, integrations) passing numeric IDs in URLs breaks

---

## Pros of Migrating to UUID

### Security
- Numeric IDs are sequential and predictable. An attacker who knows `/api/orders/104` exists can trivially probe `/api/orders/105`. UUIDs make enumeration attacks effectively impossible.
- Reduces the risk of insecure direct object reference (IDOR) vulnerabilities where endpoint authorization is weak, since guessing a UUID is computationally infeasible.

### Distributed & Offline ID Generation
- IDs can be generated client-side, in multiple services, or offline without coordinating with the database sequence. Useful if BlueSupply ever splits into microservices.
- Eliminates the single point of failure that a shared database sequence represents at high write volume.

### No Information Leakage
- Sequential IDs leak business intelligence — a competitor or user can infer how many orders, suppliers, or employees the system has by watching the IDs increment over time.

### Merge and Replication Safety
- If data ever needs to be merged across environments (staging → prod, multi-tenant setups), UUID collision is statistically impossible. Numeric IDs collide trivially.

---

## Cons and Pitfalls

### Performance Degradation (Significant)
- PostgreSQL's B-tree indexes work best with monotonically increasing keys. Random UUIDs (`UUID v4`) cause **index fragmentation** — each insert lands at a random position in the index tree, causing page splits and increasing write amplification.
- On large tables this can degrade insert performance by 20–40% and increase index size substantially.
- **Mitigation:** Use UUID v7 (time-ordered) or PostgreSQL's `gen_random_uuid()` with a `uuid_generate_v1mc()` approach. UUIDv7 preserves insert ordering and largely eliminates this problem. However, Spring's `@GeneratedValue` with `GenerationType.UUID` generates v4 by default — you would need a custom generator for v7.

### Storage Increase
- A `BIGINT` is 8 bytes. A UUID stored as `uuid` type in PostgreSQL is 16 bytes — double. Stored as `VARCHAR(36)` (common mistake) it is 36 bytes — 4.5x larger.
- Every foreign key column doubles in size. Every index doubles in size. For a schema with ~45 tables and many FK relationships, total storage impact is meaningful.
- **Mitigation:** Always use PostgreSQL's native `uuid` column type, never `VARCHAR`.

### Migration Complexity and Risk (High)
Because there is existing data, the migration cannot simply change column types. It requires:

1. **Adding a new UUID column** alongside the existing numeric ID on every table
2. **Populating UUID values** for every existing row across every table
3. **Updating all foreign key columns** to point to the new UUID values (requires joining on the old numeric ID during migration)
4. **Dropping old numeric columns**, renaming UUID columns, recreating constraints and indexes
5. **Doing this in the right dependency order** — parent tables before child tables

With ~45 tables and deeply nested foreign key relationships (requests → request items → quotations → LPOs → GRNs → payments), the migration scripts will be large, complex, and must be perfectly ordered. A single mistake means orphaned FK references or constraint violations.

This migration **cannot be run without downtime** unless carefully staged over multiple deployments with dual-write strategies, which adds further complexity.

### API Breaking Change
- Every URL in the system changes format. `/api/lpo/42` becomes `/api/lpo/018e1b3c-7f4a-7000-8a3b-2c4d5e6f7a8b`
- Any frontend, mobile app, or external integration that constructs URLs with numeric IDs breaks immediately.
- Bookmarked URLs, cached responses, and stored links in emails or notifications all break.

### Debugging Difficulty
- Numeric IDs are human-readable and easy to reference in logs, support tickets, and conversations. `Order #1042` is meaningful. A UUID is not.
- **Mitigation:** Keep a human-readable reference number (e.g., `LPO-2024-0042`) as a separate display field, separate from the primary key.

### JPA/Hibernate Caveats
- `@GeneratedValue(strategy = GenerationType.UUID)` was only introduced in Hibernate 6 / Spring Boot 3.x — BlueSupply is on Spring Boot 3.5 so this is supported, but the behavior differs from the old `GenerationType.AUTO` + sequence approach.
- Hibernate's first-level cache and dirty-checking behaviour can be subtly different with UUID keys.
- `@Version` optimistic locking still works fine with UUID keys.

---

## The Existing Data Problem in Detail

This is the hardest part. A simplified example of what a migration script must do for just two related tables:

```sql
-- Step 1: Add UUID columns
ALTER TABLE request_item ADD COLUMN uuid_id UUID DEFAULT gen_random_uuid();
ALTER TABLE quotation ADD COLUMN uuid_id UUID DEFAULT gen_random_uuid();
ALTER TABLE quotation ADD COLUMN request_item_uuid UUID;

-- Step 2: Populate the FK mapping
UPDATE quotation q
SET request_item_uuid = ri.uuid_id
FROM request_item ri
WHERE q.request_item_id = ri.id;

-- Step 3: Drop old FK, add new one
ALTER TABLE quotation DROP CONSTRAINT fk_quotation_request_item;
ALTER TABLE quotation ADD CONSTRAINT fk_quotation_request_item
    FOREIGN KEY (request_item_uuid) REFERENCES request_item(uuid_id);

-- Step 4: Drop old columns, rename new ones
ALTER TABLE quotation DROP COLUMN request_item_id;
ALTER TABLE quotation RENAME COLUMN request_item_uuid TO request_item_id;
ALTER TABLE request_item DROP COLUMN id;
ALTER TABLE request_item RENAME COLUMN uuid_id TO id;
```

Multiply this across every table and every FK relationship in the schema. The V1 migration has dozens of FK constraints. Getting the ordering wrong causes constraint violation failures that are hard to roll back from cleanly.

---

## Recommended Practices If You Proceed

1. **Use UUIDv7** not UUIDv4 — preserves time-ordering, avoids index fragmentation, still non-guessable. Requires a custom Hibernate `IdentifierGenerator`.
2. **Use PostgreSQL native `uuid` type** — never `VARCHAR`.
3. **Stage the migration** — add UUID columns first (nullable), backfill, then enforce constraints in a later deployment. Avoids a single massive risky migration.
4. **Keep a display reference number** — add a separate human-readable field like `reference_number` (`LPO-2024-0042`) so support and users still have something meaningful to communicate with.
5. **Version your Flyway scripts carefully** — each step (add column, backfill, swap constraints, drop old) should be a separate migration file so failures are granular and diagnosable.
6. **Test on a full data snapshot** — run the migration against a copy of production data before running it live. The migration will likely surface FK ordering issues that only appear with real data volumes.
7. **Plan for API versioning** — if there are external consumers of the API, version the endpoints so both numeric and UUID URLs work during a transition period.

---

## Verdict

| Factor | Recommendation |
|---|---|
| Greenfield project | ✅ Use UUID from the start |
| Small existing dataset (<10k rows), internal-only API | ✅ Migration is manageable |
| **Large existing dataset, external API consumers, no downtime tolerance** | ⚠️ High risk, questionable ROI |

**For BlueSupply specifically:** the migration is a significant engineering effort with meaningful risk and real performance trade-offs, for security gains that can largely be achieved through other means (proper authorization checks on every endpoint, which should exist regardless of ID type). The IDOR risk that UUIDs mitigate is better addressed by auditing endpoint-level authorization in the `security/` and `controller/` layers rather than by changing the ID scheme.

**Recommendation: Leave numeric IDs in place for now.** If UUID adoption is a firm requirement (e.g., for a future multi-service architecture or compliance reason), plan it as a dedicated project with a staged migration, a maintenance window, and thorough testing against a production data snapshot — not as a routine change alongside other work.

---

*Generated: 2026-02-18 | Branch: `add-claude-md` | Stack: Spring Boot 3.5 / PostgreSQL 12.9 / Flyway 9.17*
