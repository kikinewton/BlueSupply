
# BlueSupply

A multi-stage procurement approval platform built with Spring Boot 3.5. It manages the full lifecycle of procurement requests — from employee request creation through HOD endorsement, RFQ, quotation evaluation, LPO issuance, goods receipt, and multi-stage payment approval — with role-based access control, PDF/Excel document generation, email notifications, and an AI-powered procurement assistant.

---

## Table of Contents

- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Building](#building)
- [Running Tests](#running-tests)
- [Quick Start (Local)](#quick-start-local)
- [Running in Production](#running-in-production)
- [Environment Variables](#environment-variables)
- [Architecture](#architecture)
- [API Overview](#api-overview)
- [Security & Authentication](#security--authentication)
- [Running with Ollama (Local LLM Chat)](#running-with-ollama-local-llm-chat)
- [MCP Server](#mcp-server)
- [Document Generation](#document-generation)
- [Database Migrations](#database-migrations)
- [CI/CD](#cicd)
- [Metrics & Observability](#metrics--observability)
- [Manual](#manual)

---

## Project Structure

```
BlueSupply/
├── supply-svc/           # Spring Boot REST API (port 8080)
├── supply-db/            # Flyway migrations + daily backup scheduler
├── scripts/
│   └── supply-entrypoint.sh   # GPU-aware Ollama model selector
├── supply-compose.yaml   # Local PostgreSQL 12.9 via Docker Compose
├── pom.xml               # Multi-module Maven build
└── .github/workflows/    # CI pipeline
```

This is a multi-module Maven project:

| Module | Purpose |
|--------|---------|
| `supply-db` | Flyway database migrations and scheduled backups |
| `supply-svc` | Spring Boot procurement service (REST API) |

### supply-svc package layout

| Package | Role |
|---|---|
| `controller/` | 22 REST controllers — one per domain |
| `service/` | 40 service classes — business logic and workflow orchestration |
| `repository/` | 46 Spring Data JPA repositories |
| `model/` | 48 JPA entities |
| `dto/` | 64 DTOs and mappers |
| `event/` + `event/listener/` | 35 events / 15 listeners for async side-effects (email, notifications, SSE) |
| `specification/` | JPA Criteria specifications for dynamic queries |
| `exception/` | 38 domain exceptions with global `@ControllerAdvice` handler |
| `mcp/` | 7 read-only MCP tools for AI integration |
| `enums/` | 22 domain enumerations |
| `annotation/validator/` | Custom JSR-303 validators (password, description, request item) |
| `security/` + `auth/` | Spring Security with JWT (jjwt 0.12.6) |

---

## Prerequisites

| Dependency | Version |
|---|---|
| Java | 17 |
| Maven | 3.9+ (or use `./mvnw`) |
| PostgreSQL | 12.9+ |
| Docker Desktop | Required for tests (TestContainers); optional for running the app |
| Ollama | Optional — for AI chat features |

---

## Building

Requires Java 17, Maven, and Docker Desktop (for tests).

Build and install all modules from the project root:

```bash
./mvnw install
```

Maven builds `supply-db` first (reactor order), then `supply-svc`. The `supply-db` thin JAR is installed to the local Maven repository and placed on `supply-svc`'s test classpath so Flyway can run the real migrations during tests.

To build without running tests (matches CI behavior):

```bash
./mvnw -B package -Dmaven.test.skip=true
```

---

## Running Tests

From the project root (builds both modules in order):

```bash
./mvnw test
```

To run only `supply-svc` tests after a prior build of `supply-db`:

```bash
./mvnw install -pl supply-db && ./mvnw test -pl supply-svc
```

Tests spin up a PostgreSQL Testcontainer, apply all Flyway migrations from `supply-db`, then run the full test suite against the migrated schema. Docker Desktop must be running.

To run a single test class:

```bash
./mvnw test -pl supply-svc -Dtest=AuthControllerTest
```

---

## Quick Start (Local)

```bash
# 1. Start PostgreSQL
docker-compose -f supply-compose.yaml up -d

# 2. Build (skipping tests)
./mvnw -B package -Dmaven.test.skip=true

# 3. Start the application
java -jar supply-svc/target/build.jar
```

The API is available at `http://localhost:8080`.
Swagger UI: `http://localhost:8080/swagger-ui/index.html`

---

## Running in Production

**1. Apply database migrations**

```bash
java -jar supply-db/target/migration-exec.jar
```

**2. Start the service**

```bash
java -jar supply-svc/target/build.jar
```

Always run migrations before starting the service after any upgrade.

---

## Environment Variables

All variables use `${VAR:default}` notation in `application.properties`.

### Database

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/bluesupplydb` | JDBC connection URL |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `password` | Database password |

### JWT & Security

| Variable | Default | Description |
|---|---|---|
| `JWT_SECRET_KEY` | *(placeholder)* | **Required in production.** HMAC signing key — must be ≥ 32 characters |
| `JWT_ISSUER` | `bluesupply` | JWT `iss` claim |
| `JWT_VALIDITY_SECONDS` | `86400` | Token lifetime in seconds (24 hours) |
| `JKS_PASSWORD` | `bsupply` | Java keystore password |
| `CORS_ALLOWED_ORIGINS` | `https://etornamtechnologies.github.io,http://localhost:4000,http://localhost:4200` | Comma-separated allowed CORS origins |

### Mail (SMTP)

| Variable | Default | Description |
|---|---|---|
| `MAIL_HOST` | `smtp.gmail.com` | SMTP server host |
| `MAIL_PORT` | `587` | SMTP port (STARTTLS) |
| `MAIL_USERNAME` | *(empty)* | SMTP username / sender address |
| `MAIL_PASSWORD` | *(empty)* | SMTP password or app password |
| `PROCUREMENT_EMAIL` | `procurement@company.com` | Default procurement team inbox |

### File Storage

| Variable | Default | Description |
|---|---|---|
| `file.uploadDirectory` | `~/BSupplyUploads` | Upload directory for request documents |
| `file.lpoDirectory` | `~/lpo` | Generated LPO PDF output directory |

### AI (Ollama)

| Variable | Default | Description |
|---|---|---|
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama server URL |
| `OLLAMA_MODEL` | `qwen2.5:7b` | Chat model. Auto-overridden to `llama3.1:8b` when a GPU is detected |

### Application

| Variable | Default | Description |
|---|---|---|
| `SUPERADMIN_EMAIL` | *(empty)* | Email for the bootstrapped super-admin account |
| `LOGIN_URL` | `http://localhost:4200/` | Frontend login URL embedded in email links |

---

## Architecture

### Layered Design

```
HTTP Request
    │
    ▼
Controller  (@RestController — DTO validation via @Valid / custom JSR-303)
    │
    ▼
Service     (business logic, workflow orchestration)
    │  Spring Events → async email/notification dispatch
    ▼
Repository  (Spring Data JPA + native SQL queries)
    │
    ▼
PostgreSQL  (schema managed by Flyway)
```

### Caching

Guava-backed in-memory cache with configurable TTL (default: 30 seconds, `caching.expiration.time`). Cache eviction is triggered on LPO save, GRN save/approve, and payment save/approve.

### Async Processing

Spring `@Async` with multiple thread pool executors (core: 5, max: 20, queue: 100). Used for email dispatch, bulk operations, and report generation.

### Event-Driven Side Effects

Domain events (`RequestItemEvent`, `PaymentEvent`, `GRNEvent`, etc.) decouple workflow stages from email and notification dispatch. 15 listeners in `event/listener/` handle email sending and real-time SSE dashboard pushes asynchronously.

---

## API Overview

All `/api/**` endpoints require a valid JWT Bearer token in the `Authorization` header.

| Area | Route Prefix | Description |
|---|---|---|
| Auth | `/auth` | Login, signup, password reset |
| Requests | `/api/requestItems` | CRUD for procurement requests; filter by department, status, name |
| Procurement | `/api/procurement` | RFQ, quotation mapping, LPO draft |
| LPO | `/api/lpos` | Create, approve, download LPO PDF |
| GRN | `/api/goodsReceivedNotes` | Receive goods, HOD/GM approval, float GRNs |
| Quotations | `/api/quotations` | Upload quotation docs, evaluate, assign supplier |
| Payments | `/api/payments` | Payment history, cancel cheque |
| Payment Drafts | `/api/paymentDraft` | Draft create/update, multi-stage approval |
| Float | `/api/floats` | Float requests and retirement |
| Petty Cash | `/api/pettyCash` | Petty cash requests and approvals |
| Dashboard | `/api/dashboard` | KPIs, supplier performance, payment aging, trends; SSE stream at `/api/dashboard/stream` |
| Reports | `/api/reports` | Excel export for payment history and petty cash |
| Employees | `/api/employees` | Employee CRUD, role assignment |
| Suppliers | `/api/suppliers` | Supplier CRUD, performance tracking |
| Departments | `/api/departments` | Department CRUD |
| Chat | `/api/chat` | AI procurement assistant (SSE stream) |
| Documents | `/res/requestDocuments` | Upload and download request attachments |

Full interactive API reference: `http://localhost:8080/swagger-ui/index.html`

---

## Security & Authentication

### Login Flow

```bash
POST /auth/login
Content-Type: application/json

{ "email": "user@example.com", "password": "your-password" }
```

The response includes a `token` field. Pass it on all subsequent requests:

```
Authorization: Bearer <token>
```

### Public Endpoints

| Path | Description |
|---|---|
| `/auth/**` | Login, signup, password reset |
| `/res/**` | Static resources and document downloads |
| `/swagger-ui/**`, `/v3/api-docs/**` | API documentation |

### Roles

| Role | Key Responsibilities |
|---|---|
| `ROLE_REGULAR` | Create requests, view own items |
| `ROLE_HOD` | Endorse requests, approve LPOs, endorse GRNs |
| `ROLE_PROCUREMENT_OFFICER` | Generate RFQs, evaluate quotations, create LPOs |
| `ROLE_PROCUREMENT_MANAGER` | Oversee procurement, manage suppliers |
| `ROLE_GENERAL_MANAGER` | Final LPO approval, final payment approval |
| `ROLE_ACCOUNT_OFFICER` | Draft payments |
| `ROLE_AUDITOR` | Review payment drafts |
| `ROLE_FINANCIAL_MANAGER` | Financial payment approval |
| `ROLE_STORE_MANAGER` | Manage stores, approve float GRNs |
| `ROLE_ADMIN` | System administration |

### JWT Configuration

Tokens are signed with HMAC-SHA (jjwt 0.12.6). The signing key is sourced from `JWT_SECRET_KEY` — set this to a random string of at least 32 characters in production. Tokens expire after `JWT_VALIDITY_SECONDS` (default: 24 hours).

### Password Policy

Validated by Passay: minimum 8 characters, at least one uppercase letter. Stored as BCrypt hashes.

---

## Running with Ollama (Local LLM Chat)

The service exposes `POST /api/chat` — a streaming endpoint that lets users ask natural language questions about procurement data. It requires a local [Ollama](https://ollama.com) instance.

### Quick start (Docker Compose)

**1. Start Postgres and Ollama**

```bash
docker-compose -f supply-compose.yaml up -d db ollama
```

**2. Pull the model (run once)**

The init service auto-detects GPU/CPU and pulls the appropriate model:

```bash
docker-compose -f supply-compose.yaml --profile init up ollama-init
```

| Hardware | Model pulled | RAM required |
|---|---|---|
| No GPU (CPU only) | `qwen2.5:7b` | ~6 GB |
| NVIDIA / AMD GPU | `llama3.1:8b` | ~8 GB |

Model download is ~4–6 GB and is cached in the `ollama_data` Docker volume — only needed once.

**3. Start the service**

```bash
./mvnw spring-boot:run -pl supply-svc
```

Or via Docker Compose (requires building the image first):

```bash
docker-compose -f supply-compose.yaml build supply-svc
docker-compose -f supply-compose.yaml up -d
```

### Sending a chat request

Obtain a JWT token first, then stream a question:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"<email>","password":"<password>"}' | jq -r '.token')

curl -X POST http://localhost:8080/api/chat \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"message":"Which supplier has the best delivery time?"}' \
  --no-buffer
```

The response streams back as Server-Sent Events. The `--no-buffer` flag shows tokens in real time.

### Notes

- The first request after Ollama starts is slow (~10–30 s) while the model loads into memory. Subsequent requests are fast.
- The LLM has access to 7 read-only procurement tools (see [MCP Server](#mcp-server) below). It cannot modify any data.
- To force a specific model regardless of GPU, set `OLLAMA_MODEL=<model>` before starting.

---

## MCP Server

BlueSupply exposes 7 read-only procurement tools over HTTP/SSE using the [Model Context Protocol](https://modelcontextprotocol.io). Connect any MCP-compatible client (e.g. Claude Desktop) to `http://localhost:8080/sse`.

| Tool | Description |
|---|---|
| `getDashboardMetrics` | Current snapshot: GRNs today, payments due, spend by department |
| `trackRequestStatus(id)` | Full workflow status with per-stage timestamps for a request item |
| `getSupplierPerformance` | LPOs issued, average delivery days, payment completion rate per supplier |
| `getPaymentAging` | Outstanding payments bucketed by age (0–30, 31–60, 61–90, 90+ days) |
| `getProcurementCycleTime` | Average endorsement and approval cycle time per department |
| `getCancellationRate` | Cancellation and rejection rates per department |
| `getMonthlyTrends(months)` | Request count and spend for the past N months |

All MCP tool calls require a valid JWT token. All operations are read-only.

**Claude Desktop configuration:**

```json
{
  "mcpServers": {
    "bluesupply": {
      "url": "http://localhost:8080/sse",
      "headers": {
        "Authorization": "Bearer <your-jwt-token>"
      }
    }
  }
}
```

---

## Document Generation

The following documents are generated as PDFs from Thymeleaf HTML templates (Flying Saucer / iText7):

| Document | Template | Trigger |
|---|---|---|
| Local Purchase Order (LPO) | `LPOTemplate.html` | LPO finalized and approved |
| Goods Received Note (GRN) | `GRNTemplate.html` | Goods received and verified |
| Quotation Request (RFQ) | `QuotationRequest.html` | Sent to suppliers |
| Generated Quote | `GeneratedQuoteTemplate.html` | Internal quotation summary |
| Supplier Request List | `RequestListForSuppliers.html` | Bulk RFQ dispatch |

Excel reports (Apache POI) are available for payment history and petty cash via `GET /api/reports`.

Generated PDFs are stored in `file.lpoDirectory` (`~/lpo` by default). Uploaded documents go to `file.uploadDirectory` (`~/BSupplyUploads` by default).

---

## Database Migrations

Schema is managed by Flyway. Migrations live in `supply-db/src/main/resources/db/migration/` and are applied automatically on startup. The current schema version is **V24**.

| Milestone | Versions | Description |
|---|---|---|
| Initial schema | V1 | Core tables: requests, quotations, LPO, GRN, payments, invoices, floats, petty cash |
| Store management | V2–V7 | Store table additions and refinements |
| Sequences | V9–V12, V21 | Auto-increment sequences for all entities |
| Payment consolidation | V13–V16 | Merged payment draft into payment table |
| Report views | V17–V20 | Corrected type issues in views |
| Stage tracking | V23 | Added `request_review_date` to request items |
| Dashboard views | V24 | Materialized views for KPI aggregation |

To add a schema change, create a new numbered migration file:

```
supply-db/src/main/resources/db/migration/V25__describe_your_change.sql
```

Then rebuild `supply-db` before starting the service.

---

## CI/CD

GitHub Actions (`.github/workflows/maven.yml`) triggers on pushes and pull requests to `main`:

1. Checks out code
2. Sets up JDK 17 (Temurin distribution)
3. Caches Maven dependencies
4. Builds with `mvn -B package --file pom.xml -Dmaven.test.skip=true`

Tests are skipped in CI because integration tests use TestContainers and require Docker-in-Docker. Run tests locally with `./mvnw test`.

---

## Metrics & Observability

| Endpoint | Description |
|---|---|
| `GET /actuator/health` | Application health check |
| `GET /actuator/prometheus` | Prometheus-format metrics (Micrometer) |

**HTTP request logging** — Zalando Logbook logs request/response bodies for HTTP 400+ responses. `Authorization` headers and `password` query parameters are automatically redacted.

**JPA auditing** — All entity mutations record the authenticated user and timestamp via Spring Data JPA `@CreatedBy` / `@LastModifiedBy`.

---

# Manual

The system supports three types of procurement requests:

- **LPO request** — Goods request, Service Request, Project & Works (involves GRN)
- **Float order** — Requires HOD approval
- **Petty cash** — Employee uses personal funds and the company reimburses


## LPO Request Flow

1. **User Request:** The user initiates a request, specifying their requirements.
   This could be for goods, services, or any other resources needed by the user department.
   Additionally, the user selects the user department and the receiving store where the
   requested items will be delivered.

2. **HOD Endorsement:** The request is forwarded to the Head of Department (HOD) of the user department.
   The HOD reviews the request and decides whether to endorse or cancel it. If the HOD approves the request,
   it moves forward to the next step. If the HOD cancels the request, it is terminated at this stage.

3. **Request Sent to Procurement:** The endorsed request is sent to the procurement department for further processing.
   The procurement team receives the request and prepares to initiate the procurement process.

4. **Request for Quotation (RFQ) Generation:** After the request has been forwarded to the procurement department,
   they generate a Request for Quotation (RFQ) document.
   The RFQ is a formal request sent to selected suppliers or vendors to provide quotes for the requested
   items or services.
   Here's how this step typically works:
   - The procurement department creates an RFQ document that includes all the necessary details about the requested
     items, such as specifications, quantity, delivery requirements, and any other relevant information.
   - Based on their knowledge of potential suppliers and market research, the procurement team selects a list of
     suitable suppliers who can fulfill the request.
   - The RFQ is then shared with these selected suppliers, either through email, a procurement platform, or any other
     communication channel established with the suppliers.

5. **RFQ Sent to Suppliers:** The RFQ document is shared with the selected suppliers through email, procurement platforms,
   or other communication channels established with suppliers. Suppliers review the requirements, determine pricing
   and availability, and prepare their quotations.

6. **Quotations Attached to Supplier and Request Item:** The procurement attaches the quotation to the related supplier
   and request item.

7. **Quotation Evaluation and Draft LPO:** The procurement team evaluates the received quotations, compares prices
   and terms, and assesses supplier capabilities to select the most suitable offer. The procurement prepares a
   draft purchase order or contract based on the selected supplier(s) and their quotation(s), detailing the quantity,
   price, delivery terms, and other relevant information.
   A draft LPO is shared after review of the selected quotation by the HOD of the user department of the request and
   approval of request by the General Manager.

8. **HOD Review and General Manager Approval:** The HOD reviews the selected suppliers' quotations and provides their
   input on the draft LPO. After the HOD's review and approval of the request, it is forwarded to the
   General Manager for final approval.

9. **Purchase Order/Contract Finalization:** Once approved, the procurement department sends the purchase order or
   contract to the selected supplier(s) for acknowledgment and acceptance. Upon receiving the supplier's acceptance,
   the procurement finalizes the purchase order or contract and records it in the system.

10. **Notification and LPO Sharing:** The procurement department informs the user department and the receiving store
    about the approved purchase order or contract. The LPO is shared with the respective stores, and the
    receiving store prepares to receive the requested items based on the information provided in the LPO.

11. **Delivery and Verification:** The supplier delivers the items to the receiving store as per the agreed terms.
    The receiving store inspects and verifies the delivered items against the LPO, checking for quantity,
    quality, and any damages or discrepancies.

12. **Goods/Service Received Note:** If the received items are in accordance with the LPO, the receiving store acknowledges
    the receipt by issuing a Goods or Service Received Note (GRN). This note confirms the acceptance of the
    delivered items and allows the supplier to submit an invoice.

13. **HOD Review of Received Items:** The Head of Department reviews the items received by the store to ensure they
    meet the requirements and match the requested specifications.

14. **Payment Processing:** The supplier issues an invoice with a specified payment due date. The finance/accounts
    department must process payment by the said date.

15. **Accounts Officer Payment Process:** The accounts officer is responsible for initiating the payment process,
    which involves verifying the invoice, entering the PN number, and preparing the payment cheque.

16. **Auditor Checks:** The auditor checks all the documents related to the payment, including quotations,
    LPO, GRN, and the invoice, to ensure compliance with financial procedures and accuracy.

17. **Chief Accountant/Financial Manager Endorsement:** After the auditor's review, the chief accountant or
    financial manager endorses the initiated payment, confirming its accuracy and compliance with financial policies.

18. **General Manager Approval:** The General Manager provides final approval for the payment,
    signifying the completion of the cycle.


## Float Order Flow

1. User creates a float request specifying the amount and purpose.
2. Request is sent to the HOD of the user's department for review.
3. HOD approves or rejects the float request.
4. Approved floats are disbursed and tracked in the system.
5. Upon completion of the float purpose, the user submits a Float GRN with receipts for retirement.
6. The store manager reviews and approves the Float GRN.
7. The auditor reviews the float retirement documents to confirm compliance.


## Petty Cash Flow

1. User creates a petty cash request specifying amount and justification.
2. Request is sent to the HOD of the user's department for approval.
3. Approved petty cash is processed for reimbursement by the accounts officer.
4. The auditor reviews petty cash records for compliance.
