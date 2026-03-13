
# BlueSupply

## Project Structure

This is a multi-module Maven project:

| Module | Purpose |
|--------|---------|
| `supply-db` | Flyway database migrations and scheduled backups |
| `supply-svc` | Spring Boot procurement service (REST API) |

---

## Building

Requires Java 17, Maven, and Docker Desktop (for tests).

Build and install all modules from the project root:

```bash
mvn install
```

Maven builds `supply-db` first (reactor order), then `supply-svc`. The `supply-db` thin JAR is installed to the local Maven repository and placed on `supply-svc`'s test classpath so Flyway can run the real migrations during tests.

---

## Running Tests

From the project root (builds both modules in order):

```bash
mvn test
```

To run only `supply-svc` tests after a prior build of `supply-db`:

```bash
mvn install -pl supply-db && mvn test -pl supply-svc
```

Tests spin up a PostgreSQL Testcontainer, apply all Flyway migrations from `supply-db`, then run the full test suite against the migrated schema. Docker Desktop must be running.

To run a single test class:

```bash
mvn test -pl supply-svc -Dtest=AuthControllerTest
```

---

## Running in Production

**1. Apply database migrations**

```bash
java -jar supply-db/target/migration-exec.jar
```

Configure the database connection via environment variables:

| Variable | Description |
|----------|-------------|
| `DB_URL` | JDBC URL, e.g. `jdbc:postgresql://host:5432/bluesupplydb` |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |

**2. Start the service**

```bash
java -jar supply-svc/target/build.jar
```

| Variable | Description |
|----------|-------------|
| `DB_URL` | Same JDBC URL as above |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET_KEY` | JWT signing key (min 32 chars) |
| `MAIL_HOST` | SMTP host |
| `MAIL_USERNAME` | SMTP username |
| `MAIL_PASSWORD` | SMTP password |
| `SUPERADMIN_EMAIL` | Email address for the bootstrapped super-admin account |

Always run migrations before starting the service after any upgrade.

---

# Manual
The system can be used to make 3 types of requests:
* LPO request - Goods request, Service Request, Project & Works (involves GRN)
* Float order - This request HOD approval
* Petty cash - Use your money and the company reimburses


## LPO request flow
1. **User Request:** The user initiates a request, specifying their requirements. 
    This could be for goods, services, or any other resources needed by the user department. 
    Additionally, the user selects the user department and the receiving store where the 
    requested items will be delivered.

2. **HOD Endorsement:** The request is then forwarded to the Head of Department (HOD) of the user department.
   The HOD reviews the request and decides whether to endorse or cancel it. If the HOD approves the request, 
   it moves forward to the next step. If the HOD cancels the request, it is terminated at this stage.

3. **Request Sent to Procurement:** The endorsed request is sent to the procurement department for further processing. 
   The procurement team receives the request and prepares to initiate the procurement process.

4. **Request for Quotation (RFQ) Generation:** After the request has been forwarded to the procurement department, 
   they generate a Request for Quotation (RFQ) document. 
   The RFQ is a formal request sent to selected suppliers or vendors to provide quotes for the requested 
   items or services.
   Here's how this step typically works:
    * The procurement department creates an RFQ document that includes all the necessary details about the requested 
    items, such as specifications, quantity, delivery requirements, and any other relevant information.
    * Based on their knowledge of potential suppliers and market research, the procurement team selects a list of 
    suitable suppliers who can fulfill the request.
    * The RFQ is then shared with these selected suppliers, either through email, a procurement platform, or any other 
      communication channel established with the suppliers.
   
5. **RFQ Sent to Suppliers:** The RFQ document is shared with the selected suppliers through email, procurement platforms, 
   or other communication channels established with suppliers. Suppliers review the requirements, determine pricing 
   and availability, and prepare their quotations.

6. **Quotations Attached to Supplier and Request Item:** The procurement attaches the quotation to the related supplier 
   and request item

7. **Quotation Evaluation and Draft LPO:** The procurement team evaluates the received quotations, compares prices 
   and terms, and assesses supplier capabilities to select the most suitable offer. The procurement prepares a 
   draft purchase order or contract based on the selected supplier(s) and their quotation(s), detailing the quantity, 
   price, delivery terms, and other relevant information. 
   A draft LPO shared after review of the selected quotation by the HOD of the user department of the request and 
   approval of request by the General manager.

8. **HOD Review and General Manager Approval:** The HOD reviews the selected suppliers' quotations and provides their 
   input on the draft LPO. After the HOD's review and approval of the request, it is forwarded to the 
   General Manager for final approval.

9. **Purchase Order/Contract Finalization:** Once approved, the procurement department sends the purchase order or 
   contract to the selected supplier(s) for acknowledgment and acceptance. Upon receiving the supplier's acceptance, 
   the procurement finalizes the purchase order or contract and records it in the system.

10. **Notification and LPO Sharing:** The procurement department informs the user department and the receiving store 
    about the approved purchase order or contract. The LPO is shared with the respective stores, and the 
    receiving store prepares to receive the requested items based on the information provided in the LPO

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
    which involves verifying the invoice, entering the PN number and preparing the payment cheque.

16. **Auditor Checks:** The auditor checks all the documents related to the payment, including quotations, 
    LPO, GRN, and the invoice, to ensure compliance with financial procedures and accuracy.

17. **Chief Accountant/Financial Manager Endorsement:** After the auditor's review, the chief accountant or 
    financial manager endorses the initiated payment, confirming its accuracy and compliance with financial policies.

18. **General Manager Approval:** The General Manager provides final approval for the payment, 
    signifying the completion of the cycle.



## Petty Cash
1. User creates a request
2. Request is sent to HOD of user 
3. 
