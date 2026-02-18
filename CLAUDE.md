# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build all modules (from project root)
./mvnw clean package

# Build skipping tests (matches CI behavior)
./mvnw -B package -Dmaven.test.skip=true

# Run tests
./mvnw test

# Run a single test class
./mvnw test -pl supply-svc -Dtest=CommonHelperTest

# Run the application
./mvnw spring-boot:run -pl supply-svc

# Run after building
java -jar supply-svc/target/build.jar

# Start the PostgreSQL dependency
docker-compose -f supply-compose.yaml up -d

# Run database migrations manually
./mvnw flyway:migrate -pl supply-db
```

## Project Modules

This is a two-module Maven project:

- **supply-svc** — Main Spring Boot 3.5 application (port 8080). Entry point: `com.logistics.supply.SupplyApplication`
- **supply-db** — Flyway-based database migration runner. Entry point: `com.logistics.supply.db.DBMigration`. Also contains `DatabaseBackupScheduler` (daily midnight cron).

## Architecture

### supply-svc Package Layout

The service follows a layered architecture under `com.logistics.supply`:

| Package | Role |
|---|---|
| `controller/` | 22 REST controllers — one per domain entity |
| `service/` | 42 service classes — business logic and workflow orchestration |
| `repository/` | 45 Spring Data JPA repositories |
| `model/` | 47 JPA entities |
| `dto/` | 85 DTOs and mappers/converters (large; prefer checking here for request/response shapes) |
| `event/` + `event/listener/` | Spring application events for decoupled side-effects (emails, notifications) |
| `specification/` | JPA Criteria API specifications for dynamic queries |
| `exception/` | 39 domain-specific exceptions, handled globally in `errorhandling/` |
| `annotation/` + `annotation/validator/` | Custom JSR-303 validation annotations |
| `security/` + `security/config/` + `auth/` | Spring Security with OAuth2 + JWT (jjwt 0.9.1) |
| `enums/` | 20 domain enumerations — check here for valid status/type values |

### Procurement Workflow

The core domain is a multi-stage procurement approval pipeline:

1. User creates a request item (Goods / Service / Project & Works)
2. HOD endorses → Procurement processes → RFQ sent to suppliers
3. Suppliers return quotations → Quotation evaluation → LPO draft created
4. HOD + General Manager approve LPO → Purchase order finalized
5. Delivery verified → GRN issued → Invoice processed
6. Payment draft → Auditor review → Finance Manager → GM final approval

Role-based access controls enforce which endpoints each stage actor can call.

### Document Generation

Thymeleaf HTML templates (in `supply-svc/src/main/resources/templates/`) are rendered to PDF via Flying Saucer / iText7 / html2pdf, and to Excel via Apache POI. Templates include LPO, GRN, quotation requests, and report tables.

### Database

- PostgreSQL 12.9 (local dev: host `localhost:5432`, db `bluesupplydb`)
- Schema managed by Flyway; migration scripts live in `supply-db/src/main/resources/db/migration/` (currently V1–V12)
- New schema changes require a new numbered Flyway migration file

## Key Dependencies

- Spring Boot 3.5.0, Spring Security, Spring Data JPA, Spring OAuth2
- PostgreSQL JDBC 42.5.4
- Flyway 9.17.0
- JWT: jjwt 0.9.1
- PDF: Flying Saucer + iText7 + html2pdf
- Excel: Apache POI 5.2.2
- HTTP logging: Zalando Logbook
- Metrics: Micrometer + Prometheus
- Lombok (used extensively — always enable annotation processing)

## CI/CD

GitHub Actions (`.github/workflows/maven.yml`) triggers on pushes/PRs to `main`, runs on `ubuntu-latest` with JDK 15, and builds with `mvn -B package --file pom.xml -Dmaven.test.skip=true`.
