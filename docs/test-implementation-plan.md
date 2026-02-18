# Test Implementation Plan — BlueSupply

*Generated: 2026-02-18 | Tied to: `SECURITY_REMEDIATION.md`*

The goal of this plan is to add tests **before** the remaining security remediation phases are
executed, so each phase has a regression safety net. Tests are ordered to match the remediation
execution order (Phase 4 → Phase 3 → Phase 2 → Phase 1).

---

## How Tests Are Written in This Project

All tests follow this pattern — new tests must conform to it:

```java
@IntegrationTest                          // spins up full Spring Boot + Postgres TestContainer
class ExampleControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "kikinewton@gmail.com", roles = "ROLE_NAME")
    void shouldDoSomething() throws Exception {
        SomeDto body = SomeFixture.build();

        mockMvc.perform(post("/api/endpoint")
                .content(objectMapper.writeValueAsString(body))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}
```

- `@IntegrationTest` handles DB cleanup + reinit via `cleanup_script.sql` + `init_script.sql` before each test
- `@WithMockUser` replaces JWT; use the username of a seeded test employee and the role matching `@PreAuthorize` on the endpoint
- Seeded test data is in `src/test/resources/sql/init_script.sql` — IDs start at 100+ to avoid collisions
- Fixtures live in `src/test/java/com/logistics/supply/fixture/`

---

## Phase 4 Safety Net — iText7 & html2pdf Upgrade

**Covers:** `com.itextpdf:itext7-core` 7.2.3 → 7.2.6 and `com.itextpdf:html2pdf` 4.0.3 → 4.0.6

These libraries handle PDF generation via the `html2pdf` conversion pipeline. Tests must confirm
that PDF bytes are still produced correctly after the upgrade. No PDF content parsing is needed —
confirming a non-empty, valid PDF byte stream is sufficient.

### 4a. `ReportControllerTest` — Excel Export Endpoints

**File to create:** `src/test/java/com/logistics/supply/controller/ReportControllerTest.java`

These tests cover the 6 reporting endpoints that generate Excel files via Apache POI. The test
strategy is to call each endpoint with `download=true` and assert:
- HTTP 200
- `Content-Type: application/octet-stream`
- `Content-Disposition` header contains the expected filename
- Response body is non-empty (valid file bytes returned)

```
Tests to write:

1. shouldDownloadProcuredItemsReportAsExcel()
   - GET /res/procurement/procuredItemsReport?download=true
   - @WithMockUser(roles = "ROLE_PROCUREMENT_MANAGER")
   - Assert: status 200, Content-Type octet-stream, body non-empty

2. shouldFetchProcuredItemsReportAsJson()
   - GET /res/procurement/procuredItemsReport (no download param)
   - @WithMockUser(roles = "ROLE_PROCUREMENT_MANAGER")
   - Assert: status 200, $.status = "SUCCESS"

3. shouldDownloadPaymentReportAsExcel()
   - GET /res/accounts/paymentReport?download=true
   - @WithMockUser(roles = "ROLE_AUDITOR")
   - Assert: status 200, Content-Type octet-stream, body non-empty

4. shouldDownloadPettyCashPaymentReportAsExcel()
   - GET /res/accounts/pettyCashPaymentReport?download=true
   - @WithMockUser(roles = "ROLE_AUDITOR")
   - Assert: status 200, body non-empty

5. shouldFetchFloatAgeingAnalysisReport()
   - GET /res/accounts/floatAgeingAnalysisReport
   - @WithMockUser(roles = "ROLE_AUDITOR")
   - Assert: status 200, $.status = "SUCCESS"

6. shouldDownloadGrnReportAsExcel()
   - GET /res/stores/grnReport?download=true
   - @WithMockUser(roles = "ROLE_STORE_MANAGER")
   - Assert: status 200, body non-empty
```

**Notes:**
- Check `init_script.sql` — it already seeds views (`grn_report`, `payment_report`, etc.) used by
  these endpoints, so the queries should return data without additional seeding
- For date-range filters, use a wide range like `periodStart=2020-01-01&periodEnd=2030-01-01`

---

## Phase 3 Safety Net — Flying Saucer PDF XXE Fix

**Covers:** `org.xhtmlrenderer:flying-saucer-pdf` 9.1.22 → 9.3.x

Flying Saucer renders the 8 Thymeleaf HTML templates to PDF. The upgrade changes the
internal rendering engine from unmaintained iText 2.1.7 to OpenPDF. Tests must confirm
each template still renders to a valid PDF after the upgrade.

### 3a. `PdfGenerationTest` — Template Rendering

**File to create:** `src/test/java/com/logistics/supply/service/PdfGenerationTest.java`

This is a **service-level** integration test (not a controller test) because PDF generation is
triggered internally, not directly via an HTTP response. Inject the service(s) that call
Flying Saucer and invoke them directly, then assert the byte array is a valid PDF.

```
Tests to write:

1. shouldGenerateLpoPdf()
   - Inject: LocalPurchaseOrderService (or whichever service calls Flying Saucer for LPO)
   - Call the method that generates LPO PDF using a seeded LPO ID from init_script.sql
   - Assert: returned byte[] is non-null, length > 0, starts with PDF magic bytes "%PDF"

2. shouldGenerateGrnPdf()
   - Inject: GoodsReceivedNoteService
   - Call the GRN PDF generation method with a seeded GRN ID
   - Assert: returned byte[] starts with "%PDF"

3. shouldGenerateQuotationRequestPdf()
   - Inject: QuotationService (or ProcurementService — whichever builds quotation request PDFs)
   - Assert: returned byte[] starts with "%PDF"
```

**How to assert valid PDF bytes:**
```java
byte[] pdf = service.generateLpoPdf(lpoId);
assertNotNull(pdf);
assertTrue(pdf.length > 0);
// "%PDF" is the PDF magic header
assertEquals("%PDF", new String(pdf, 0, 4));
```

**Notes:**
- Find the exact service method names by searching for `flying-saucer` or `ITextRenderer` in the source
- If PDF generation is triggered via an HTTP download endpoint, use the controller test approach
  instead (assert `Content-Type: application/pdf` and non-empty body)

---

## Phase 2 Safety Net — jjwt Upgrade

**Covers:** `io.jsonwebtoken:jjwt` 0.9.1 → `jjwt-api/jjwt-impl/jjwt-jackson` 0.12.6

The jjwt upgrade changes the API significantly. Tests must pin the current behaviour of
JWT generation and validation so regressions are caught immediately after the upgrade.

### 2a. `JwtServiceTest` — Token Lifecycle

**File to create:** `src/test/java/com/logistics/supply/auth/JwtServiceTest.java`

This is a **unit test** (no `@IntegrationTest`) — inject `JwtServiceImpl` or `TokenProvider`
directly using `@ExtendWith(SpringExtension.class)` with mocked dependencies.

```
Tests to write:

1. shouldGenerateTokenForEmployee()
   - Build a mock UserDetails / Employee with a test email
   - Call jwtService.generateToken(userDetails)
   - Assert: returned String is non-null, non-empty, has 3 dot-separated parts (header.payload.signature)

2. shouldExtractUsernameFromToken()
   - Generate a token, then call jwtService.extractUsername(token)
   - Assert: extracted username equals the email used to generate the token

3. shouldValidateTokenForCorrectUser()
   - Generate a token for user A
   - Call jwtService.isTokenValid(token, userDetailsA)
   - Assert: returns true

4. shouldRejectTokenForWrongUser()
   - Generate a token for user A
   - Call jwtService.isTokenValid(token, userDetailsB)
   - Assert: returns false

5. shouldDetectExpiredToken()
   - Generate a token with a past expiry (override expiry via reflection or test config property)
   - Call jwtService.isTokenValid(token, userDetails)
   - Assert: returns false (or throws ExpiredJwtException — document whichever the service does)

6. shouldRejectTamperedToken()
   - Generate a token, modify the payload segment (e.g. append a character)
   - Call jwtService.isTokenValid(tampered, userDetails)
   - Assert: returns false
```

### 2b. `AuthControllerTest` — Token Flow End-to-End

**File to extend:** `src/test/java/com/logistics/supply/controller/AuthControllerTest.java`

Add to the existing test class:

```
Tests to add:

7. shouldReturnJwtTokenOnLogin()
   - POST /auth/login with credentials of a seeded employee (e.g. kikinewton@gmail.com)
   - Assert: status 200, response body contains a non-empty token field
   - Store the token value for documentation purposes (not for use in other tests —
     other tests use @WithMockUser)

8. shouldRejectLoginWithWrongPassword()
   - POST /auth/login with valid email + wrong password
   - Assert: status 401 or 403

9. shouldRejectLoginWithUnknownEmail()
   - POST /auth/login with a non-existent email
   - Assert: status 401 or 403
```

**Note:** After the jjwt upgrade, tests 1–6 will need small syntax updates (the `JwtServiceImpl`
method signatures will change). The tests themselves define the expected contract — the
implementation should be updated to make them pass.

---

## Phase 1 Safety Net — OAuth2 Stack Replacement

**Covers:** Removing `spring-security-oauth2-autoconfigure:2.6.8`, migrating to Spring Boot 3.x
native security. This is the highest-risk change — it touches the entire authentication layer.

### 1a. `SecurityAccessControlTest` — Role Enforcement

**File to create:** `src/test/java/com/logistics/supply/security/SecurityAccessControlTest.java`

This test class pins the RBAC rules for every controller. It does not test business logic —
only that the correct HTTP status is returned when the wrong role (or no auth) calls an endpoint.

```
Tests to write (one group per protected controller):

Payment endpoints:
1. shouldDenyPaymentCancellationWithoutAccountOfficerRole()
   - PUT /api/payments/100/cancelCheque with @WithMockUser(roles = "ROLE_REGULAR")
   - Assert: status 403

2. shouldAllowPaymentCancellationWithAccountOfficerRole()
   - PUT /api/payments/100/cancelCheque with @WithMockUser(roles = "ROLE_ACCOUNT_OFFICER")
   - Assert: status is NOT 403 (200 or 404 are both acceptable — confirms access was granted)

PaymentDraft endpoints:
3. shouldDenyPaymentDraftCreationForNonAccountOfficer()
   - POST /api/paymentDraft with @WithMockUser(roles = "ROLE_HOD")
   - Assert: status 403

4. shouldDenyDraftApprovalForRegularUser()
   - PUT /api/paymentDraft/1/approval with @WithMockUser(roles = "ROLE_REGULAR")
   - Assert: status 403

5. shouldAllowDraftApprovalForAuditor()
   - PUT /api/paymentDraft/1/approval with @WithMockUser(roles = "ROLE_AUDITOR")
   - Assert: status is NOT 403

LPO endpoints:
6. shouldDenyLpoDraftFetchForRegularUser()
   - GET /api/localPurchaseOrderDrafts with @WithMockUser(roles = "ROLE_REGULAR")
   - Assert: status 403

Request endpoints:
7. shouldDenyHodEndorsementForNonHod()
   - PUT /api/requestItems/{id}/hodEndorsement with @WithMockUser(roles = "ROLE_REGULAR")
   - Assert: status 403

Unauthenticated access:
8. shouldDenyAllProtectedEndpointsWithoutAuth()
   - GET /api/payments (no @WithMockUser — anonymous request)
   - Assert: status 401 or 403
   Repeat for: GET /api/paymentDrafts, GET /api/localPurchaseOrderDrafts
```

### 1b. Extend Existing Tests With Negative Cases

For each of the 16 controllers that already have tests, add at least one negative-role test.
For example in `QuotationControllerTest`, add:

```
shouldDenyQuotationCreationForHodRole()
  - POST /api/quotations with @WithMockUser(roles = "ROLE_HOD")
  - Assert: status 403
```

This baseline ensures that after the OAuth2 migration, access control rules are not silently
widened (the common failure mode when migrating security config).

---

## Additional Coverage (Not Tied to Security Fixes)

These fill the remaining gaps identified in `test-coverage-and-docs-analysis.md` and should be
done after the security safety nets are in place.

### PaymentController — Happy Path Tests

**File to create:** `src/test/java/com/logistics/supply/controller/PaymentControllerTest.java`

```
1. shouldFetchPaymentById()
   - GET /api/payments/100 (seed a payment with id=100 in init_script.sql)
   - @WithMockUser(roles = "ROLE_ACCOUNT_OFFICER")
   - Assert: status 200, $.status = "SUCCESS", $.data.id = 100

2. shouldFetchPaymentsBySupplier()
   - GET /api/payments/supplier/1 (supplier id=1 is seeded)
   - Assert: status 200, $.status = "SUCCESS"

3. shouldFetchPaymentsByInvoiceNumber()
   - GET /api/payments/invoice/INV-001 (seed an invoice number in init_script.sql)
   - Assert: status 200

4. shouldCancelChequePayment()
   - PUT /api/payments/100/cancelCheque
   - @WithMockUser(roles = "ROLE_ACCOUNT_OFFICER")
   - Assert: status 200, $.status = "SUCCESS"
   - Follow-up GET /api/payments/100 to confirm status changed
```

### PaymentDraftController — Approval Workflow

**File to create:** `src/test/java/com/logistics/supply/controller/PaymentDraftControllerTest.java`

```
1. shouldCreatePaymentDraft()
   - POST /api/paymentDraft with PaymentDraftDTO (GRN id from init_script.sql)
   - @WithMockUser(username = "kikinewton@gmail.com", roles = "ROLE_ACCOUNT_OFFICER")
   - Assert: status 200, $.status = "SUCCESS", $.data.id is not null
   - Store returned draft ID for subsequent tests

2. shouldFetchPaymentDraftsAsAuditor()
   - GET /api/paymentDrafts
   - @WithMockUser(roles = "ROLE_AUDITOR")
   - Assert: status 200, $.status = "SUCCESS"

3. shouldApprovePaymentDraftAsAuditor()
   - PUT /api/paymentDraft/{seedDraftId}/approval
   - @WithMockUser(username = "kikinewton@gmail.com", roles = "ROLE_AUDITOR")
   - Assert: status 200, $.status = "SUCCESS"

4. shouldApprovePaymentDraftAsFinancialManager()
   - PUT /api/paymentDraft/{seedDraftId}/approval
   - @WithMockUser(roles = "ROLE_FINANCIAL_MANAGER")
   - Assert: status 200

5. shouldApprovePaymentDraftAsGeneralManager()
   - PUT /api/paymentDraft/{seedDraftId}/approval
   - @WithMockUser(roles = "ROLE_GENERAL_MANAGER")
   - Assert: status 200

6. shouldDeletePaymentDraft()
   - DELETE /api/paymentDrafts/{seedDraftId}
   - @WithMockUser(roles = "ROLE_ACCOUNT_OFFICER")
   - Assert: status 200

7. shouldFetchGrnsWithoutPayment()
   - GET /api/paymentDraft/grnWithoutPayment?paymentStatus=PARTIAL
   - @WithMockUser(roles = "ROLE_ACCOUNT_OFFICER")
   - Assert: status 200
```

**Note:** Tests 3–5 represent the full approval chain. Each test needs a fresh draft (DB is cleared
between tests), so each test should create its own draft in a `@BeforeEach` setup step rather
than depending on the previous test's state.

### SupplierControllerTest

**File to create:** `src/test/java/com/logistics/supply/controller/SupplierControllerTest.java`

```
1. shouldCreateSupplier()
   - POST /api/suppliers
   - @WithMockUser(roles = "ROLE_PROCUREMENT_MANAGER")
   - Body: SupplierDto with name, email, phone, address, etc.
   - Assert: status 200, $.data.name matches input

2. shouldFetchAllSuppliers()
   - GET /api/suppliers
   - @WithMockUser(roles = "ROLE_PROCUREMENT_MANAGER")
   - Assert: status 200, seeded suppliers (id=1,2) appear in results

3. shouldFetchSupplierById()
   - GET /api/suppliers/1
   - Assert: status 200, $.data.id = 1

4. shouldUpdateSupplier()
   - PUT /api/suppliers/1 with updated name field
   - Assert: status 200, $.data.name = updated value

5. shouldFetchUnregisteredSuppliers()
   - GET /api/suppliers?unRegisteredSuppliers=true
   - Assert: status 200, supplier id=1 ("Jilorm Ventures", unregistered) appears
```

### Fill Empty Test Class Stubs

**`GRNControllerTest`** — replace the empty stub with:
```
1. shouldFetchAllGRNs()
   - GET /api/goodsReceivedNotes
   - @WithMockUser(roles = "ROLE_STORE_OFFICER")
   - Assert: status 200, $.status = "SUCCESS"
```

**`InvoiceControllerTest`** — replace the empty stub with:
```
1. shouldFetchAllInvoices()
   - GET /api/invoices
   - @WithMockUser(roles = "ROLE_ACCOUNT_OFFICER")
   - Assert: status 200, $.status = "SUCCESS"

2. shouldFetchInvoiceById()
   - GET /api/invoices/100 (seed an invoice in init_script.sql)
   - Assert: status 200, $.data.id = 100
```

---

## Database Seeding Requirements

Some tests above need data not currently in `init_script.sql`. Add the following to the init script:

```sql
-- Payment (for PaymentControllerTest)
INSERT INTO public.payment (id, payment_amount, payment_method, cheque_number, bank, status)
VALUES (100, 5000.00, 'CHEQUE', 'CHQ-001', 'GCB Bank', 'PENDING');

-- Invoice (for InvoiceControllerTest)
INSERT INTO public.invoice (id, invoice_no, amount, supplier_id)
VALUES (100, 'INV-001', 5000.00, 1);

-- GRN (if not already seeded)
-- Check init_script.sql — if grn table exists but is empty, add:
INSERT INTO public.goods_received_note (id, serial_number, supplier_id, created_by)
VALUES (100, 'GRN-001', 1, 2);
```

**Before adding:** verify the exact column names and required fields by reading the corresponding
Flyway migration script in `supply-db/src/main/resources/db/migration/`.

---

## Implementation Order

Implement in this order to align with the security remediation sequence:

```
Step 1  →  ReportControllerTest          (safety net for Phase 4 iText7 upgrade)
Step 2  →  PdfGenerationTest             (safety net for Phase 3 Flying Saucer upgrade)
Step 3  →  JwtServiceTest                (safety net for Phase 2 jjwt upgrade)
Step 4  →  AuthControllerTest additions  (safety net for Phase 2)
Step 5  →  SecurityAccessControlTest     (safety net for Phase 1 OAuth2 migration)
Step 6  →  PaymentControllerTest         (financial coverage)
Step 7  →  PaymentDraftControllerTest    (financial workflow coverage)
Step 8  →  SupplierControllerTest        (master data coverage)
Step 9  →  Fill GRNControllerTest stub
Step 10 →  Fill InvoiceControllerTest stub
```

**Do Steps 1–5 before starting any security remediation phase.** Steps 6–10 can be done
in parallel with or after the security fixes.
