# Event Listener → Explicit Service Call Migration Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace all Spring application events and JPA @PostPersist side-effect listeners with direct service method calls so that every execution flow is traceable from controller through service in a single readable call stack.

**Architecture:** Existing listener classes are promoted to `@Service` beans with regular public methods. The services that currently call `applicationEventPublisher.publishEvent(...)` are updated to inject and call these notification services directly. Once all callers are migrated the event class and `@EventListener` annotation are deleted, leaving no implicit dispatch machinery.

**Tech Stack:** Spring Boot 3.5, Spring Data JPA, Lombok, JUnit 5 / Mockito, Testcontainers (PostgreSQL)

---

## Pros and Cons

### Pros

| # | Benefit |
|---|---------|
| 1 | **Readable execution flows** — a reader can follow controller → service → notification in a straight line without hunting for `@EventListener` methods |
| 2 | **Stack traces point to the cause** — exceptions in email sending appear in the same stack as the originating service call |
| 3 | **Simpler unit tests** — mock the notification service directly; no need to wire up `ApplicationEventPublisher` or verify event objects |
| 4 | **IDE "Find Usages" works** — calling `notifyEndorsed(items)` makes every callsite visible; `publishEvent(new BulkRequestItemEvent(...))` hides them |
| 5 | **Explicit async** — async behaviour is declared at the method boundary with `@Async` rather than buried inside a `CompletableFuture.runAsync` wrapper around `publishEvent` |
| 6 | **Transactional control** — you decide whether to call the notifier inside or outside the transaction boundary; currently `@EventListener` (non-transactional) fires synchronously inside the caller's transaction by default |
| 7 | **Easier onboarding** — a new engineer reading `RequestItemService.endorseItems()` can see that `requestItemNotifier.notifyEndorsed(items)` is called; today they must know to search for every `@EventListener(BulkRequestItemEvent.class)` |

### Cons

| # | Trade-off |
|---|-----------|
| 1 | **Tighter coupling** — `RequestItemService` now knows about `RequestItemNotifier`; previously each concern was decoupled through the event bus |
| 2 | **Services grow larger** — domain services gain notification dependencies; mitigated by keeping notification logic in dedicated notifier beans |
| 3 | **Adding new side effects requires modifying the originating service** — with events you can add a new listener without touching existing code |
| 4 | **Risk of missed call sites** — if a comment is persisted in two places and only one gains the explicit call, the other silently skips the notification; the current `@PostPersist` fires everywhere automatically |
| 5 | **DashboardSseEventListener** is legitimately multi-consumer (three different events, one broadcaster) — direct calls work but the fan-out nature of events suited it well |
| 6 | **`@PrePersist` data-computation callbacks** (`Payment.calculateWithholdingTax`, `LocalPurchaseOrderDraft.setDepartment`, `VerificationToken.setExpiry`, `FloatOrder.addFloatType`) should **not** be migrated — they enforce data integrity at the persistence boundary and are not side effects; this plan leaves them in place |

---

## What Is and Is Not in Scope

**In scope (side-effect listeners — cause external action or secondary DB write):**
- All 6 comment `@PostPersist` listeners (`FloatCommentListener`, `GRNCommentListener`, `PettyCashCommentListener`, `QuotationCommentListener`, `RequestItemCommentListener`, `PaymentDraftCommentListener`)
- `EmployeeListener` (welcome email on employee create)
- `VerificationEventListener` (password reset email on token create)
- `SupplierRequestListener` (creates RFQ record on `SupplierRequestMap` persist)
- `LpoDraftEventListener` (expires quotations + emails HODs on LPO draft persist)
- `GRNListener.handleCreateFloatGRN()` (@PostPersist on float GRN)
- All Spring application event classes (`BulkRequestItemEvent`, `ApproveRequestItemEvent`, `CancelRequestItemEvent`, `GRNEvent`, `FloatEvent`, `FloatRetirementEvent`, `FundsReceivedFloatEvent`, `FundsReceivedPettyCashEvent`, `PettyCashEvent`, `AssignQuotationRequestItemEvent`, `RoleChangeEvent`, `CancelPaymentEvent`, `EmployeeDisableEvent`, `AddLPOEvent`) and their listeners.
- `HodReviewEvent` and `HodReviewListener` — **dead code**: the event is defined as an inner class inside `HodReviewListener` and is never published by any service. Delete without replacement.

**Out of scope (keep as-is):**
- `@PrePersist`/`@PostUpdate` callbacks that only compute derived fields or write log statements
- `DashboardSseEventListener` — multi-event SSE broadcaster; keeping it event-driven is reasonable and is a separate concern
- `FullPaymentEventListener` — already fully commented out
- `FullPaymentEvent` — `DashboardSseEventListener.onPaymentCompleted` listens to it; the event is currently never published (a pre-existing gap), so keep the class until a payment-completion publish site is identified and wired to the dashboard broadcaster

---

## File Structure

### New files to create

| File | Responsibility |
|------|---------------|
| `service/notification/RequestItemNotifier.java` | Sends emails for request item created, endorsed, approved, cancelled |
| `service/notification/GrnNotifier.java` | Sends emails for GRN created, HOD endorsed, procurement advised; handles float GRN |
| `service/notification/FloatNotifier.java` | Sends emails for float order events (save, endorsement, retirement, funds received) |
| `service/notification/PettyCashNotifier.java` | Sends emails for petty cash endorsement, approval, funds received |
| `service/notification/QuotationNotifier.java` | Sends email for quotation assignment; handles RFQ creation via `SupplierRequestMap` |
| `service/notification/EmployeeNotifier.java` | Sends welcome email on create, disabled notification, role change notification |
| `service/notification/LpoNotifier.java` | Sends HOD review email; handles quotation expiry on LPO draft persist |
| `service/notification/CommentNotifier.java` | Single bean covering all six comment email types |
| `service/notification/VerificationNotifier.java` | Sends password reset token email |
| `service/notification/PaymentNotifier.java` | Saves cancelled payment record; sends payment-related emails |

### Files to modify

| File | Change |
|------|--------|
| `service/RequestItemService.java` | Inject `RequestItemNotifier`; replace all `publishEvent` calls |
| `service/GoodsReceivedNoteService.java` | Inject `GrnNotifier`; replace `publishEvent` |
| `service/FloatOrderService.java` | Inject `FloatNotifier`; replace all `publishEvent` calls |
| `service/PettyCashService.java` | Inject `PettyCashNotifier`; replace all `publishEvent` calls |
| `service/QuotationService.java` | Inject `QuotationNotifier`; replace `publishEvent` |
| `service/EmployeeService.java` | Inject `EmployeeNotifier`; replace all `publishEvent` calls |
| `service/FloatGRNService.java` | Inject `GrnNotifier`; replace `publishEvent` |
| `service/PaymentService.java` | Inject `PaymentNotifier`; replace `publishEvent` |
| Every service that creates a comment entity | Inject `CommentNotifier`; call after `repository.save()` |
| `service/EmployeeService.java` | Call `EmployeeNotifier.notifyWelcome()` after employee create |
| Wherever `VerificationToken` is created | Inject `VerificationNotifier`; call after token save |
| Wherever `SupplierRequestMap` is persisted | Inject `QuotationNotifier`; call `createRfq()` after save |
| Wherever `LocalPurchaseOrderDraft` is persisted | Inject `LpoNotifier`; call `onDraftCreated()` after save |

### Files to delete (after all callers migrated)

All classes in `event/` and `event/listener/` except `DashboardSseEventListener` and any `@PrePersist`/`@PostUpdate`-only entity listeners.

---

## Tasks

### Task 1: Create `RequestItemNotifier` and migrate `RequestItemService`

**Files:**
- Create: `supply-svc/src/main/java/com/logistics/supply/service/notification/RequestItemNotifier.java`
- Modify: `supply-svc/src/main/java/com/logistics/supply/service/RequestItemService.java`
- Test: `supply-svc/src/test/java/com/logistics/supply/service/notification/RequestItemNotifierTest.java`

The current `RequestItemEventListener` handles `BulkRequestItemEvent` (endorsed/created) and `ApproveRequestItemEventListener` handles `ApproveRequestItemEvent` (approved). `CancelRequestItemEventListener` handles `CancelRequestItemEvent`.

- [ ] **Step 1: Write the failing test for `RequestItemNotifier`**

```java
@ExtendWith(MockitoExtension.class)
class RequestItemNotifierTest {

    @Mock EmailSender emailSender;
    @Mock EmployeeRepository employeeRepository;
    // add other mocks that the existing listeners use

    @InjectMocks RequestItemNotifier notifier;

    @Test
    void notifyCreated_sendsEmailToHod() {
        RequestItem item = RequestItemFixture.pendingEndorsement();
        when(employeeRepository.findHodByDepartment(any())).thenReturn(Optional.of(EmployeeFixture.hod()));

        notifier.notifyCreated(List.of(item));

        verify(emailSender, atLeastOnce()).sendMail(any());
    }

    @Test
    void notifyEndorsed_sendsEmailToProcurementAndRequester() {
        RequestItem item = RequestItemFixture.endorsed();

        notifier.notifyEndorsed(List.of(item));

        verify(emailSender, times(2)).sendMail(any()); // procurement + requester
    }

    @Test
    void notifyApproved_sendsEmailToEmployee() {
        RequestItem item = RequestItemFixture.approved();

        notifier.notifyApproved(List.of(item));

        verify(emailSender, atLeastOnce()).sendMail(any());
    }

    @Test
    void notifyCancelled_sendsEmailToEmployee() {
        CancelledRequestItem cancelled = CancelledRequestItemFixture.aDefault();

        notifier.notifyCancelled(List.of(cancelled));

        verify(emailSender, atLeastOnce()).sendMail(any());
    }
}
```

- [ ] **Step 2: Run test — expect compile failure** (`RequestItemNotifier` does not exist)

```bash
./mvnw test -pl supply-svc -Dtest=RequestItemNotifierTest
```

- [ ] **Step 3: Create `RequestItemNotifier`**

  Copy the bodies of `RequestItemEventListener`, `ApproveRequestItemEventListener`, and `CancelRequestItemEventListener` into `RequestItemNotifier` as regular `@Async` public methods named `notifyCreated`, `notifyEndorsed`, `notifyApproved`, `notifyCancelled`. Remove all `@EventListener` annotations. Annotate the class `@Service @RequiredArgsConstructor`.

```java
@Service
@RequiredArgsConstructor
public class RequestItemNotifier {

    private final EmailSender emailSender;
    private final EmployeeRepository employeeRepository;
    // … same deps as the three listeners

    @Async
    public void notifyCreated(List<RequestItem> items) {
        // body from RequestItemEventListener.handleRequestItemEvent()
    }

    @Async
    public void notifyEndorsed(List<RequestItem> items) {
        // body from RequestItemEventListener.handleEndorseRequestItemEvent()
    }

    @Async
    public void notifyApproved(List<RequestItem> items) {
        // body from ApproveRequestItemEventListener.handleApproval()
    }

    @Async
    public void notifyCancelled(List<CancelledRequestItem> cancelled) {
        // body from CancelRequestItemEventListener.handleCancelRequestItemEvent()
    }
}
```

- [ ] **Step 4: Run test — expect PASS**

```bash
./mvnw test -pl supply-svc -Dtest=RequestItemNotifierTest
```

- [ ] **Step 5: Update `RequestItemService` and `RequestItem` entity**

  Inject `RequestItemNotifier`. Replace:
  - `sendRequestItemsCreatedEvent()` → `requestItemNotifier.notifyCreated(items)`
  - `sendEndorsedItemsEvent()` → `requestItemNotifier.notifyEndorsed(items)`
  - `sendApprovedItemsEvent()` → `requestItemNotifier.notifyApproved(items)`
  - `sendCancelledRequestItemEvent()` → `requestItemNotifier.notifyCancelled(cancelled)`

  Remove `ApplicationEventPublisher` injection if `RequestItemService` no longer uses it elsewhere.

  Also remove `@EntityListeners(RequestItemEventListener.class)` from `supply-svc/src/main/java/com/logistics/supply/model/RequestItem.java` line 32. Leaving this annotation pointing at a deleted class causes a Spring context startup failure at the end of Task 10.

- [ ] **Step 6: Run full test suite — expect PASS**

```bash
./mvnw test -pl supply-svc
```

- [ ] **Step 7: Commit**

```bash
git add supply-svc/src/main/java/com/logistics/supply/service/notification/RequestItemNotifier.java \
        supply-svc/src/main/java/com/logistics/supply/service/RequestItemService.java \
        supply-svc/src/test/java/com/logistics/supply/service/notification/RequestItemNotifierTest.java
git commit -m "refactor(request-item): replace event publish with explicit RequestItemNotifier calls"
```

---

### Task 2: Create `GrnNotifier` and migrate `GoodsReceivedNoteService` + `FloatGRNService`

**Files:**
- Create: `supply-svc/src/main/java/com/logistics/supply/service/notification/GrnNotifier.java`
- Modify: `supply-svc/src/main/java/com/logistics/supply/service/GoodsReceivedNoteService.java`
- Modify: `supply-svc/src/main/java/com/logistics/supply/service/FloatGRNService.java`
- Test: `supply-svc/src/test/java/com/logistics/supply/service/notification/GrnNotifierTest.java`

Source listeners: `GRNListener` (`event/listener/GRNListener.java` — 4 handler methods including one `@PostPersist`).

`GRNListener` has two distinct scopes that must not be conflated:
- **GoodsReceivedNote scope**: `handleEvent(GRNEvent)` fires when a GRN is created; `handleHodEndorseGRN(GRNEvent)` fires when HOD approves the GRN. Both listen to `GRNEvent`, distinguished by condition on `grn.isApprovedByHod()`.
- **FloatGRN scope**: `handleCreateFloatGRN(@PostPersist on FloatGRN)` fires when a float GRN is persisted (sends **one** email to store manager only); `handleProcurementAdvise(FloatGRNEvent)` fires when `FloatGRN.isApprovedByStoreManager() == true` and sends email to the **auditor** (role `ROLE_AUDITOR`), not procurement.

Note: `HodReviewEvent` (listened to by `HodReviewListener`) is **never published** by any service — it is dead code. Do **not** add a `notifyHodReview` method or look for a `HodReviewEvent` publisher. `HodReviewListener` will be deleted in Task 10.

- [ ] **Step 1: Write the failing test for `GrnNotifier`**

```java
@ExtendWith(MockitoExtension.class)
class GrnNotifierTest {

    @Mock EmailSender emailSender;
    @Mock EmployeeRepository employeeRepository;

    @InjectMocks GrnNotifier notifier;

    @Test
    void notifyGrnCreated_sendsToProcurementManager() {
        GoodsReceivedNote grn = GoodsReceivedNoteFixture.aDefault();
        notifier.notifyGrnCreated(grn);
        verify(emailSender, atLeastOnce()).sendMail(any());
    }

    @Test
    void notifyHodEndorsed_sendsToHod() {
        GoodsReceivedNote grn = GoodsReceivedNoteFixture.hodEndorsed();
        notifier.notifyHodEndorsed(grn);
        verify(emailSender, atLeastOnce()).sendMail(any());
    }

    @Test
    void notifyFloatGrnApprovedByStoreManager_sendsToAuditor() {
        // handleProcurementAdvise sends to ROLE_AUDITOR, not to procurement
        FloatGRN floatGrn = FloatGRNFixture.approvedByStoreManager();
        notifier.notifyFloatGrnApprovedByStoreManager(floatGrn);
        verify(emailSender, atLeastOnce()).sendMail(any());
    }

    @Test
    void notifyFloatGrnCreated_sendsToStoreManagerOnly() {
        // handleCreateFloatGRN sends ONE email (to store manager only)
        FloatGRN floatGrn = FloatGRNFixture.aDefault();
        notifier.notifyFloatGrnCreated(floatGrn);
        verify(emailSender, times(1)).sendMail(any());
    }
}
```

- [ ] **Step 2: Run test — expect compile failure**

```bash
./mvnw test -pl supply-svc -Dtest=GrnNotifierTest
```

- [ ] **Step 3: Create `GrnNotifier`**

  Pull handler bodies from `event/listener/GRNListener.java` into four methods, all `@Async`:
  - `notifyGrnCreated(GoodsReceivedNote)` — from `handleEvent`
  - `notifyHodEndorsed(GoodsReceivedNote)` — from `handleHodEndorseGRN`
  - `notifyFloatGrnCreated(FloatGRN)` — from `handleCreateFloatGRN` (sends to store manager)
  - `notifyFloatGrnApprovedByStoreManager(FloatGRN)` — from `handleProcurementAdvise` (sends to auditor)

- [ ] **Step 4: Run test — expect PASS**

```bash
./mvnw test -pl supply-svc -Dtest=GrnNotifierTest
```

- [ ] **Step 5: Update `GoodsReceivedNoteService` and `FloatGRNService`**

  - `GoodsReceivedNoteService.sendCreateGRNEvent()` → `grnNotifier.notifyGrnCreated(grn)`
  - Find where GRN HOD-endorsement is saved (grep for `approvedByHod` or `hodEndorse` in service) → `grnNotifier.notifyHodEndorsed(grn)`
  - `FloatGRNService.notifyAuditor()` — this method currently publishes `FloatGRNEvent`; replace with `grnNotifier.notifyFloatGrnApprovedByStoreManager(floatGrn)`
  - Find where `FloatGRN` is first persisted (the `@PostPersist` trigger site) — likely `floatGRNRepository.save(floatGrn)` in `FloatGRNService` — add `grnNotifier.notifyFloatGrnCreated(floatGrn)` after the save call

- [ ] **Step 6: Run full test suite — expect PASS**

```bash
./mvnw test -pl supply-svc
```

- [ ] **Step 7: Commit**

```bash
git commit -m "refactor(grn): replace GRNListener events with explicit GrnNotifier calls"
```

---

### Task 3: Create `FloatNotifier` and migrate `FloatOrderService`

**Files:**
- Create: `supply-svc/src/main/java/com/logistics/supply/service/notification/FloatNotifier.java`
- Modify: `supply-svc/src/main/java/com/logistics/supply/service/FloatOrderService.java`
- Test: `supply-svc/src/test/java/com/logistics/supply/service/notification/FloatNotifierTest.java`

Source listeners:
- `event/FloatListener.java` — lives in the `event/` root package, **not** in `event/listener/`
- `event/listener/FloatRetirementListener.java`
- `event/listener/FundsReceivedFloatListener.java`

Also note: `FloatOrderService.sendEventForGeneralManagerFloatOrderRetirement()` currently calls `applicationEventPublisher.publishEvent(floatOrder)` — publishing the raw `FloatOrder` entity, **not** a `FloatRetirementEvent`. No listener handles a bare `FloatOrder`, so this call is currently a no-op. Replacing it with `floatNotifier.notifyRetirementToGm(order)` is not just a refactor — it fixes pre-existing dead behaviour.

- [ ] **Step 1: Write failing tests**

```java
@ExtendWith(MockitoExtension.class)
class FloatNotifierTest {

    @Mock EmailSender emailSender;
    @Mock FloatPaymentRepository floatPaymentRepository;
    @Mock EmployeeRepository employeeRepository;

    @InjectMocks FloatNotifier notifier;

    @Test
    void notifyFloatSaved_sendsEndorsementEmailToHod() {
        FloatOrder order = FloatOrderFixture.pendingEndorsement();
        notifier.notifyFloatSaved(order);
        verify(emailSender, atLeastOnce()).sendMail(any());
    }

    @Test
    void notifyFloatEndorsed_sendsApprovalEmailToRequester() {
        FloatOrder order = FloatOrderFixture.endorsed();
        notifier.notifyFloatEndorsed(order);
        verify(emailSender, atLeastOnce()).sendMail(any());
    }

    @Test
    void notifyFundsReceived_createsFloatPaymentRecord() {
        FloatOrder order = FloatOrderFixture.approved();
        notifier.notifyFundsReceived(order);
        verify(floatPaymentRepository).save(any(FloatPayment.class));
    }

    @Test
    void notifyRetirementToAuditor_sendsEmail() {
        FloatOrder order = FloatOrderFixture.retired();
        notifier.notifyRetirementToAuditor(order);
        verify(emailSender, atLeastOnce()).sendMail(any());
    }

    @Test
    void notifyRetirementToGm_sendsEmail() {
        FloatOrder order = FloatOrderFixture.retiredAndAuditorApproved();
        notifier.notifyRetirementToGm(order);
        verify(emailSender, atLeastOnce()).sendMail(any());
    }
}
```

- [ ] **Step 2: Run test — expect compile failure**

```bash
./mvnw test -pl supply-svc -Dtest=FloatNotifierTest
```

- [ ] **Step 3: Create `FloatNotifier`**

  Extract bodies from `FloatListener`, `FloatRetirementListener`, and `FundsReceivedFloatListener`. `notifyFundsReceived` performs the DB write currently in `FundsReceivedFloatListener.addFloatPayment()`.

- [ ] **Step 4: Run test — expect PASS**

```bash
./mvnw test -pl supply-svc -Dtest=FloatNotifierTest
```

- [ ] **Step 5: Update `FloatOrderService`**

  Replace all `publishEvent` calls:
  - `sendFloatOrderSavedEvent()` → `floatNotifier.notifyFloatSaved(order)`
  - `sendFloatOrderFundsAllocatedEvent()` → `floatNotifier.notifyFundsReceived(order)`
  - `sendEventForRetirementApprovalForAuditor()` → `floatNotifier.notifyRetirementToAuditor(order)`
  - `sendEventForGeneralManagerFloatOrderRetirement()` → `floatNotifier.notifyRetirementToGm(order)`
  - Float endorsed → `floatNotifier.notifyFloatEndorsed(order)` (find caller)

- [ ] **Step 6: Run full test suite**

```bash
./mvnw test -pl supply-svc
```

- [ ] **Step 7: Commit**

```bash
git commit -m "refactor(float): replace FloatListener events with explicit FloatNotifier calls"
```

---

### Task 4: Create `PettyCashNotifier` and migrate `PettyCashService`

**Files:**
- Create: `supply-svc/src/main/java/com/logistics/supply/service/notification/PettyCashNotifier.java`
- Modify: `supply-svc/src/main/java/com/logistics/supply/service/PettyCashService.java`
- Test: `supply-svc/src/test/java/com/logistics/supply/service/notification/PettyCashNotifierTest.java`

Source listeners:
- `event/PettyCashListener.java` — lives in the `event/` root package, **not** in `event/listener/`
- `event/listener/FundsReceivedPettyCashListener.java`

- [ ] **Step 1: Write failing tests**

```java
@ExtendWith(MockitoExtension.class)
class PettyCashNotifierTest {

    @Mock EmailSender emailSender;
    @Mock PettyCashPaymentRepository pettyCashPaymentRepository;
    @Mock EmployeeRepository employeeRepository;

    @InjectMocks PettyCashNotifier notifier;

    @Test
    void notifyEndorsed_sendsEmailToHod() {
        Set<PettyCash> items = PettyCashFixture.defaultSet();
        notifier.notifyEndorsed(items);
        verify(emailSender, atLeastOnce()).sendMail(any());
    }

    @Test
    void notifyApproved_sendsEmailToGm() {
        Set<PettyCash> items = PettyCashFixture.defaultSet();
        notifier.notifyApproved(items);
        verify(emailSender, atLeastOnce()).sendMail(any());
    }

    @Test
    void notifyFundsReceived_createsPettyCashPaymentRecords() {
        Set<PettyCash> items = PettyCashFixture.defaultSet();
        notifier.notifyFundsReceived(items);
        verify(pettyCashPaymentRepository, atLeastOnce()).save(any());
    }
}
```

- [ ] **Step 2: Run test — expect compile failure**

```bash
./mvnw test -pl supply-svc -Dtest=PettyCashNotifierTest
```

- [ ] **Step 3: Create `PettyCashNotifier`**

  Pull bodies from `PettyCashListener.sendHODEmail`, `PettyCashListener.sendGMEmail`, and `FundsReceivedPettyCashListener.addPettyCashPayment`.

- [ ] **Step 4: Run test — expect PASS**

```bash
./mvnw test -pl supply-svc -Dtest=PettyCashNotifierTest
```

- [ ] **Step 5: Update `PettyCashService`**

  Replace `sendPettyCashOrderEvent()` and `sendFundAllocatedEvent()` with direct `pettyCashNotifier` calls.

- [ ] **Step 6: Run full test suite**

```bash
./mvnw test -pl supply-svc
```

- [ ] **Step 7: Commit**

```bash
git commit -m "refactor(petty-cash): replace PettyCashListener events with explicit PettyCashNotifier calls"
```

---

### Task 5: Create `QuotationNotifier` and migrate `QuotationService` + remove `SupplierRequestListener`

**Files:**
- Create: `supply-svc/src/main/java/com/logistics/supply/service/notification/QuotationNotifier.java`
- Modify: `supply-svc/src/main/java/com/logistics/supply/service/QuotationService.java`
- Modify: Wherever `SupplierRequestMap` is persisted (find via grep)
- Test: `supply-svc/src/test/java/com/logistics/supply/service/notification/QuotationNotifierTest.java`

Source listeners: `AssignQuotationEventListener`, `SupplierRequestListener`.

- [ ] **Step 1: Locate `SupplierRequestMap` persistence**

```bash
./mvnw -pl supply-svc -q && grep -rn "SupplierRequestMap" supply-svc/src/main/java --include="*.java" | grep -i "save\|persist"
```

- [ ] **Step 2: Write failing tests**

```java
@ExtendWith(MockitoExtension.class)
class QuotationNotifierTest {

    @Mock EmailSender emailSender;
    @Mock RequestForQuotationRepository rfqRepository;
    @Mock EmployeeRepository employeeRepository;

    @InjectMocks QuotationNotifier notifier;

    @Test
    void notifyQuotationAssigned_sendsEmailToGmAndHod() {
        RequestItem item = RequestItemFixture.withQuotation();
        notifier.notifyQuotationAssigned(List.of(item));
        verify(emailSender, times(2)).sendMail(any()); // GM + HOD
    }

    @Test
    void createRfqForSupplierRequest_savesRfqRecord() {
        SupplierRequestMap map = SupplierRequestMapFixture.aDefault();
        notifier.createRfqForSupplierRequest(map);
        verify(rfqRepository).save(any(RequestForQuotation.class));
    }
}
```

- [ ] **Step 3: Run test — expect compile failure**

```bash
./mvnw test -pl supply-svc -Dtest=QuotationNotifierTest
```

- [ ] **Step 4: Create `QuotationNotifier`**

  `notifyQuotationAssigned` — body from `AssignQuotationEventListener`.
  `createRfqForSupplierRequest` — body from `SupplierRequestListener.addRFQ`.

- [ ] **Step 5: Run test — expect PASS**

```bash
./mvnw test -pl supply-svc -Dtest=QuotationNotifierTest
```

- [ ] **Step 6: Update `QuotationService` and `SupplierRequestMap` persist site**

  In `QuotationService.sendAssignItemsEvent()` → `quotationNotifier.notifyQuotationAssigned(items)`.
  In the service that calls `supplierRequestMapRepository.save(map)` → add `quotationNotifier.createRfqForSupplierRequest(map)` immediately after the save call.

- [ ] **Step 7: Run full test suite**

```bash
./mvnw test -pl supply-svc
```

- [ ] **Step 8: Commit**

```bash
git commit -m "refactor(quotation): replace AssignQuotationEvent + SupplierRequestListener with QuotationNotifier"
```

---

### Task 6: Create `EmployeeNotifier` and migrate `EmployeeService`

**Files:**
- Create: `supply-svc/src/main/java/com/logistics/supply/service/notification/EmployeeNotifier.java`
- Modify: `supply-svc/src/main/java/com/logistics/supply/service/EmployeeService.java`
- Modify: `supply-svc/src/main/java/com/logistics/supply/model/Employee.java` (remove `@EntityListeners(EmployeeListener.class)`)
- Test: `supply-svc/src/test/java/com/logistics/supply/service/notification/EmployeeNotifierTest.java`

Source listeners: `EmployeeListener` (@PostPersist), `EmployeeDisabledEventListener`, `RoleChangeEventListener`.

- [ ] **Step 1: Write failing tests**

```java
@ExtendWith(MockitoExtension.class)
class EmployeeNotifierTest {

    @Mock EmailSender emailSender;
    @Mock EmployeeRepository employeeRepository;

    @InjectMocks EmployeeNotifier notifier;

    @Test
    void notifyWelcome_sendsWelcomeEmail() {
        Employee employee = EmployeeFixture.aDefault();
        notifier.notifyWelcome(employee);
        verify(emailSender).sendMail(any());
    }

    @Test
    void notifyDisabled_sendsEmailToHodAndEmployee() {
        Employee employee = EmployeeFixture.disabled();
        notifier.notifyDisabled(employee);
        verify(emailSender, times(2)).sendMail(any());
    }

    @Test
    void notifyRoleChanged_sendsEmailToHodAndGm() {
        Employee employee = EmployeeFixture.withUpdatedRole();
        notifier.notifyRoleChanged(employee);
        verify(emailSender, times(2)).sendMail(any());
    }
}
```

- [ ] **Step 2: Run test — expect compile failure**

```bash
./mvnw test -pl supply-svc -Dtest=EmployeeNotifierTest
```

- [ ] **Step 3: Create `EmployeeNotifier`**

  Pull bodies from `EmployeeListener.sendEmployeeEmail`, `EmployeeDisabledEventListener.sendMailOnEmployeeDisable`, and `RoleChangeEventListener.sendGMMailOnRoleChange`.

- [ ] **Step 4: Run test — expect PASS**

```bash
./mvnw test -pl supply-svc -Dtest=EmployeeNotifierTest
```

- [ ] **Step 5: Update `EmployeeService`**

  After `employeeRepository.save(employee)` in the create method → `employeeNotifier.notifyWelcome(employee)`.
  In `sendDisabledEmailNotification()` → `employeeNotifier.notifyDisabled(employee)`.
  In role update method → `employeeNotifier.notifyRoleChanged(employee)`.

- [ ] **Step 6: Remove `@EntityListeners(EmployeeListener.class)` from `Employee.java`**

- [ ] **Step 7: Run full test suite**

```bash
./mvnw test -pl supply-svc
```

- [ ] **Step 8: Commit**

```bash
git commit -m "refactor(employee): replace EmployeeListener events with explicit EmployeeNotifier calls"
```

---

### Task 7: Create `LpoNotifier` and remove `LpoDraftEventListener`

**Files:**
- Create: `supply-svc/src/main/java/com/logistics/supply/service/notification/LpoNotifier.java`
- Modify: Wherever `LocalPurchaseOrderDraft` is persisted (find via grep)
- Modify: `supply-svc/src/main/java/com/logistics/supply/model/LocalPurchaseOrderDraft.java` (remove `@EntityListeners(LpoDraftEventListener.class)`)
- Test: `supply-svc/src/test/java/com/logistics/supply/service/notification/LpoNotifierTest.java`

Source listener: `LpoDraftEventListener` (@PostPersist — expires quotations + sends HOD review emails).

- [ ] **Step 1: Locate LPO draft save site**

```bash
grep -rn "localPurchaseOrderDraftRepository.save\|lpoDraftRepository.save" supply-svc/src/main/java --include="*.java"
```

- [ ] **Step 2: Write failing tests**

```java
@ExtendWith(MockitoExtension.class)
class LpoNotifierTest {

    @Mock EmailSender emailSender;
    @Mock QuotationRepository quotationRepository;
    @Mock EmployeeRepository employeeRepository;

    @InjectMocks LpoNotifier notifier;

    @Test
    void onDraftCreated_expiresNonLinkedQuotations() {
        LocalPurchaseOrderDraft draft = LpoDraftFixture.withLinkedQuotations();
        notifier.onDraftCreated(draft);
        verify(quotationRepository, atLeastOnce()).save(any(Quotation.class));
    }

    @Test
    void onDraftCreated_sendsReviewEmailToHods() {
        LocalPurchaseOrderDraft draft = LpoDraftFixture.withLinkedQuotations();
        notifier.onDraftCreated(draft);
        verify(emailSender, atLeastOnce()).sendMail(any());
    }
}
```

- [ ] **Step 3: Run test — expect compile failure**

```bash
./mvnw test -pl supply-svc -Dtest=LpoNotifierTest
```

- [ ] **Step 4: Create `LpoNotifier`**

  Pull full body from `LpoDraftEventListener.expireQuotations` into `onDraftCreated`. Mark `@Async`.

  **Circular dependency warning:** `LpoDraftEventListener` uses `@Lazy` on its `QuotationService` and `RequestItemService` injections to break a startup cycle (both of those services eventually depend on `LocalPurchaseOrderDraftService`, which will call `LpoNotifier`). `LpoNotifier` must do the same. Use one of these two approaches:

  Option A — keep `@Lazy` (minimal change):
  ```java
  @Service
  @RequiredArgsConstructor
  public class LpoNotifier {
      @Lazy private final QuotationService quotationService;
      @Lazy private final RequestItemService requestItemService;
      private final EmailSender emailSender;
      private final EmployeeRepository employeeRepository;
  }
  ```

  Option B — inject repositories directly instead of services (removes the cycle entirely):
  Inject `QuotationRepository` and `RequestItemRepository` and replicate only the narrow query logic rather than calling service methods. Choose this if the methods called on `QuotationService`/`RequestItemService` are simple repository delegations.

- [ ] **Step 5: Run test — expect PASS**

```bash
./mvnw test -pl supply-svc -Dtest=LpoNotifierTest
```

- [ ] **Step 6: Update LPO draft save site**

  After the `repository.save(draft)` call → `lpoNotifier.onDraftCreated(draft)`.
  Remove `@EntityListeners(LpoDraftEventListener.class)` from `LocalPurchaseOrderDraft.java`.

- [ ] **Step 7: Run full test suite**

```bash
./mvnw test -pl supply-svc
```

- [ ] **Step 8: Commit**

```bash
git commit -m "refactor(lpo): replace LpoDraftEventListener with explicit LpoNotifier call"
```

---

### Task 8: Create `CommentNotifier` and migrate all 6 comment service classes

**Files:**
- Create: `supply-svc/src/main/java/com/logistics/supply/service/notification/CommentNotifier.java`
- Modify: Every service class that persists comment entities (find via grep below)
- Modify: Remove `@EntityListeners(...)` from comment model classes
- Test: `supply-svc/src/test/java/com/logistics/supply/service/notification/CommentNotifierTest.java`

Source listeners: `FloatCommentListener`, `GRNCommentListener`, `PettyCashCommentListener`, `QuotationCommentListener`, `RequestItemCommentListener`, `PaymentDraftCommentListener`.

- [ ] **Step 1: Find all comment save sites**

```bash
grep -rn "Comment\|comment" supply-svc/src/main/java/com/logistics/supply/service --include="*.java" | grep "\.save("
```

- [ ] **Step 2: Write failing tests**

```java
@ExtendWith(MockitoExtension.class)
class CommentNotifierTest {

    @Mock EmailSender emailSender;
    @Mock EmployeeRepository employeeRepository;
    @Mock CacheService cacheService;

    @InjectMocks CommentNotifier notifier;

    @Test
    void notifyFloatComment_sendsEmailToRequester() {
        FloatComment comment = FloatCommentFixture.aDefault();
        notifier.notifyFloatComment(comment);
        verify(emailSender).sendMail(any());
    }

    @Test
    void notifyGrnComment_routesEmailByCommentType() {
        GoodsReceivedNoteComment comment = GrnCommentFixture.aDefault();
        notifier.notifyGrnComment(comment);
        verify(emailSender).sendMail(any());
    }

    @Test
    void notifyRequestItemComment_sendsEmailToCreator() {
        RequestItemComment comment = RequestItemCommentFixture.aDefault();
        notifier.notifyRequestItemComment(comment);
        verify(emailSender).sendMail(any());
    }

    @Test
    void notifyPettyCashComment_sendsEmailToRequester() {
        PettyCashComment comment = PettyCashCommentFixture.aDefault();
        notifier.notifyPettyCashComment(comment);
        verify(emailSender).sendMail(any());
    }

    @Test
    void notifyQuotationComment_routesEmailByType() {
        QuotationComment comment = QuotationCommentFixture.aDefault();
        notifier.notifyQuotationComment(comment);
        verify(emailSender).sendMail(any());
    }

    @Test
    void notifyPaymentDraftComment_routesEmailByReviewStage() {
        PaymentDraftComment comment = PaymentDraftCommentFixture.aDefault();
        notifier.notifyPaymentDraftComment(comment);
        verify(emailSender).sendMail(any());
    }
}
```

- [ ] **Step 3: Run test — expect compile failure**

```bash
./mvnw test -pl supply-svc -Dtest=CommentNotifierTest
```

- [ ] **Step 4: Create `CommentNotifier`**

  One method per comment type, pulling bodies from the six listeners. Each method is `@Async`.

- [ ] **Step 5: Run test — expect PASS**

```bash
./mvnw test -pl supply-svc -Dtest=CommentNotifierTest
```

- [ ] **Step 6: Update each comment service**

  For each service that saves a comment:
  ```java
  // before:
  XxxComment saved = commentRepository.save(comment);
  // after:
  XxxComment saved = commentRepository.save(comment);
  commentNotifier.notifyXxxComment(saved);
  ```

  Remove `@EntityListeners(XxxCommentListener.class)` from each comment model class.

- [ ] **Step 7: Run full test suite**

```bash
./mvnw test -pl supply-svc
```

- [ ] **Step 8: Commit**

```bash
git commit -m "refactor(comments): replace 6 @PostPersist comment listeners with explicit CommentNotifier calls"
```

---

### Task 9: Create `VerificationNotifier` + `PaymentNotifier` and clean up remaining listeners

**Files:**
- Create: `supply-svc/src/main/java/com/logistics/supply/service/notification/VerificationNotifier.java`
- Create: `supply-svc/src/main/java/com/logistics/supply/service/notification/PaymentNotifier.java`
- Modify: Wherever `VerificationToken` is saved
- Modify: `supply-svc/src/main/java/com/logistics/supply/service/PaymentService.java`
- Modify: `supply-svc/src/main/java/com/logistics/supply/model/VerificationToken.java` (remove `@EntityListeners`)
- Test: `supply-svc/src/test/java/com/logistics/supply/service/notification/VerificationNotifierTest.java`
- Test: `supply-svc/src/test/java/com/logistics/supply/service/notification/PaymentNotifierTest.java`

Source listeners: `VerificationEventListener`, `CancelPaymentEventListener`.

- [ ] **Step 1: Write failing tests for both notifiers**

```java
// VerificationNotifierTest
@Test
void notifyPasswordReset_sendsTokenEmail() {
    VerificationToken token = new VerificationToken();
    token.setToken("abc-123");
    token.setEmployee(EmployeeFixture.aDefault());
    notifier.notifyPasswordReset(token);
    verify(emailSender).sendMail(any());
}

// PaymentNotifierTest
@Test
void notifyPaymentCancelled_savesCancelledRecord() {
    Payment payment = PaymentFixture.aDefault();
    notifier.notifyPaymentCancelled(payment);
    verify(cancelledPaymentRepository).save(any());
}
```

- [ ] **Step 2: Run test — expect compile failure**

```bash
./mvnw test -pl supply-svc -Dtest=VerificationNotifierTest+PaymentNotifierTest
```

- [ ] **Step 3: Create both notifiers**

  `VerificationNotifier.notifyPasswordReset` — body from `VerificationEventListener.sendVerificationToken`.
  `PaymentNotifier.notifyPaymentCancelled` — body from `CancelPaymentEventListener.cancelPayment`.

- [ ] **Step 4: Run test — expect PASS**

```bash
./mvnw test -pl supply-svc -Dtest=VerificationNotifierTest+PaymentNotifierTest
```

- [ ] **Step 5: Update call sites**

  Find where `VerificationToken` is saved → add `verificationNotifier.notifyPasswordReset(token)` after save, remove `@EntityListeners` from `VerificationToken`.
  In `PaymentService.cancelPayment()` → `paymentNotifier.notifyPaymentCancelled(payment)`.

- [ ] **Step 6: Run full test suite**

```bash
./mvnw test -pl supply-svc
```

- [ ] **Step 7: Commit**

```bash
git commit -m "refactor(auth,payment): replace VerificationEventListener and CancelPaymentListener with explicit notifier calls"
```

---

### Task 10: Delete all migrated event and listener classes

**Files to delete:**

Event classes:
- `event/BulkRequestItemEvent.java`
- `event/ApproveRequestItemEvent.java`
- `event/CancelRequestItemEvent.java`
- `event/FloatEvent.java`
- `event/PettyCashEvent.java`
- `event/AssignQuotationRequestItemEvent.java`
- `event/RoleChangeEvent.java`
- `event/AddLPOEvent.java`
- Inner event classes deleted with their listener files: `GRNEvent`, `FloatGRNEvent` (in `GRNListener`), `FloatRetirementEvent` (in `FloatRetirementListener`), `FundsReceivedFloatEvent` (in `FundsReceivedFloatListener`), `FundsReceivedPettyCashEvent` (in `FundsReceivedPettyCashListener`), `CancelPaymentEvent` (in `CancelPaymentEventListener`), `EmployeeDisableEvent` (in `EmployeeDisabledEventListener`), `HodReviewEvent` (in `HodReviewListener` — never published, dead code)

**Do NOT delete** `event/FullPaymentEvent.java` — `DashboardSseEventListener.onPaymentCompleted` still listens to it. This event is currently never published (a pre-existing gap); leave it in place.

Listener classes in `event/` root:
- `event/FloatListener.java`
- `event/PettyCashListener.java`
- `event/AssignQuotationEventListener.java`
- `event/ApproveRequestItemEventListener.java`
- `event/RequestItemEventListener.java`

Listener classes in `event/listener/`:
- `event/listener/CancelRequestItemEventListener.java`
- `event/listener/GRNListener.java`
- `event/listener/FloatRetirementListener.java`
- `event/listener/FundsReceivedFloatListener.java`
- `event/listener/FundsReceivedPettyCashListener.java`
- `event/listener/RoleChangeEventListener.java`
- `event/listener/CancelPaymentEventListener.java`
- `event/listener/EmployeeDisabledEventListener.java`
- `event/listener/HodReviewListener.java` (dead — never received an event)
- `event/listener/FullPaymentEventListener.java` (already commented out)
- `event/listener/EmployeeListener.java`
- `event/listener/FloatCommentListener.java`
- `event/listener/GRNCommentListener.java`
- `event/listener/PettyCashCommentListener.java`
- `event/listener/QuotationCommentListener.java`
- `event/listener/RequestItemCommentListener.java`
- `event/listener/PaymentDraftCommentListener.java`
- `event/listener/SupplierRequestListener.java`
- `event/listener/LpoDraftEventListener.java`
- `event/listener/VerificationEventListener.java`

**Keep:**
- `event/listener/DashboardSseEventListener.java` — multi-event SSE broadcaster
- `event/FullPaymentEvent.java` — still referenced by `DashboardSseEventListener`

- [ ] **Step 1: Verify no remaining references to deleted classes**

```bash
grep -rn "BulkRequestItemEvent\|ApproveRequestItemEvent\|CancelRequestItemEvent\|FloatEvent\|PettyCashEvent\|AssignQuotationRequestItemEvent\|RoleChangeEvent\|AddLPOEvent\|FloatRetirementEvent" \
  supply-svc/src/main/java --include="*.java"
```

Expected: zero matches. (`FullPaymentEvent` is intentionally kept; `DashboardSseEventListener` references it — that is correct.)

- [ ] **Step 2: Verify `ApplicationEventPublisher` is no longer injected in non-Dashboard services**

```bash
grep -rn "ApplicationEventPublisher" supply-svc/src/main/java --include="*.java"
```

Expected: only `DashboardSseEventListener` or its publisher bean.

- [ ] **Step 3: Delete files**

```bash
git rm supply-svc/src/main/java/com/logistics/supply/event/BulkRequestItemEvent.java \
       supply-svc/src/main/java/com/logistics/supply/event/ApproveRequestItemEvent.java \
       # ... all listed above
```

- [ ] **Step 4: Run full test suite — expect PASS**

```bash
./mvnw test -pl supply-svc
```

- [ ] **Step 5: Commit**

```bash
git commit -m "chore: delete all migrated event classes and listener beans"
```

---

### Task 11: Final verification — integration test pass and build clean

- [ ] **Step 1: Run full build including tests**

```bash
./mvnw clean package -pl supply-svc
```

Expected: `BUILD SUCCESS`, zero test failures.

- [ ] **Step 2: Verify no unused imports remain in notifier classes**

```bash
./mvnw -pl supply-svc compile 2>&1 | grep -i "warning\|unused"
```

- [ ] **Step 3: Confirm `DashboardSseEventListener` still compiles with its two active event handlers**

  `ApproveRequestItemEvent` and `BulkRequestItemEvent` are still published (by `RequestItemService` via its new notifier calls — the dashboard broadcaster still fires via those events). `FullPaymentEvent` is kept but **not currently published** anywhere; `onPaymentCompleted` is a dormant handler. Verify the class compiles and the SSE endpoint still streams for the two active events:

  ```bash
  grep -n "FullPaymentEvent\|ApproveRequestItemEvent\|BulkRequestItemEvent" \
    supply-svc/src/main/java/com/logistics/supply/event/listener/DashboardSseEventListener.java
  ```

- [ ] **Step 4: Final commit**

```bash
git commit -m "refactor: complete event-listener to explicit-service-call migration"
```
