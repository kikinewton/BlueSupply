# Database Migration Service

## Overview

Standalone Spring Boot application that applies Flyway schema migrations to the PostgreSQL database and runs scheduled daily backups.

The migration scripts in `src/main/resources/db/migration/` are the single source of truth for the database schema. They are also used by `supply-svc` integration tests via a Maven `test`-scope dependency on this module.

## Build output

The `spring-boot-maven-plugin` is configured with `<classifier>exec</classifier>`, so the build produces two JARs:

| File | Purpose |
|------|---------|
| `target/migration.jar` | Thin JAR (primary artifact) — used as a test dependency by `supply-svc` |
| `target/migration-exec.jar` | Executable fat JAR — used in production |

## Running migrations in production

```bash
java -jar target/migration-exec.jar
```

Required environment variables:

| Variable | Description |
|----------|-------------|
| `DB_URL` | JDBC URL, e.g. `jdbc:postgresql://host:5432/bluesupplydb` |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |

Always run migrations before starting `supply-svc` after any upgrade.

## Adding a new migration

Create a new file in `src/main/resources/db/migration/` following the Flyway naming convention:

```
V{next_version}__{short_description}.sql
```

Example: `V18__add_invoice_table.sql`

The next version after the current highest (`V17`) is `V18`.
