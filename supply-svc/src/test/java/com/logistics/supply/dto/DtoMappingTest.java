package com.logistics.supply.dto;

import com.logistics.supply.enums.*;
import com.logistics.supply.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Documents and verifies the field-by-field mappings performed by every static
 * {@code toDto()} factory method in the DTO layer.
 *
 * <p>These tests serve as a safety net during the replacement of
 * {@link org.springframework.beans.BeanUtils#copyProperties} with explicit
 * field assignments. Each test must pass before <em>and</em> after the
 * replacement; any regression means a field was dropped or misassigned.
 */
class DtoMappingTest {

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static Department department(int id, String name, String description) {
        Department d = new Department();
        d.setId(id);
        d.setName(name);
        d.setDescription(description);
        return d;
    }

    private static Supplier supplier(int id) {
        Supplier s = new Supplier();
        s.setId(id);
        s.setName("Acme Ltd");
        s.setPhoneNo("0244000001");
        s.setLocation("Accra");
        s.setDescription("Reliable vendor");
        s.setEmail("acme@example.com");
        s.setAccountNumber("1234567890");
        s.setBank("GCB Bank");
        s.setRegistered(true);
        return s;
    }

    private static Employee employee(int id, Department dept) {
        Employee e = new Employee();
        e.setId(id);
        e.setFirstName("John");
        e.setLastName("Doe");
        e.setPhoneNo("0244000002");
        e.setEmail("john.doe@example.com");
        e.setDepartment(dept);
        Role role = new Role("ROLE_HOD");
        e.setRoles(List.of(role));
        return e;
    }

    private static RequestDocument requestDocument(String fileName) {
        RequestDocument rd = new RequestDocument();
        // RequestDocument extends AbstractAuditable — setId is protected, use reflection
        ReflectionTestUtils.setField(rd, "id", 1);
        rd.setFileName(fileName);
        rd.setDocumentType("PDF");
        rd.setDocumentFormat("A4");
        return rd;
    }

    /** Sets the id on an AbstractAuditable entity whose setId() is protected. */
    private static <T> void setId(T entity, Object id) {
        ReflectionTestUtils.setField(entity, "id", id);
    }

    // -------------------------------------------------------------------------
    // DepartmentDto
    // -------------------------------------------------------------------------

    @Test
    void departmentToDto_mapsNameAndDescription() {
        Department dept = department(1, "Finance", "Finance department");

        DepartmentDto dto = DepartmentDto.toDto(dept);

        assertThat(dto.getName()).isEqualTo("Finance");
        assertThat(dto.getDescription()).isEqualTo("Finance department");
    }

    @Test
    void departmentToDto_nullDescriptionStaysNull() {
        Department dept = department(2, "IT", null);

        DepartmentDto dto = DepartmentDto.toDto(dept);

        assertThat(dto.getName()).isEqualTo("IT");
        assertThat(dto.getDescription()).isNull();
    }

    // -------------------------------------------------------------------------
    // SupplierDto
    // -------------------------------------------------------------------------

    @Test
    void supplierToDto_mapsAllFields() {
        Supplier s = supplier(5);

        SupplierDto dto = SupplierDto.toDto(s);

        assertThat(dto.getId()).isEqualTo(5);
        assertThat(dto.getName()).isEqualTo("Acme Ltd");
        assertThat(dto.getPhoneNo()).isEqualTo("0244000001");
        assertThat(dto.getLocation()).isEqualTo("Accra");
        assertThat(dto.getDescription()).isEqualTo("Reliable vendor");
        assertThat(dto.getEmail()).isEqualTo("acme@example.com");
        assertThat(dto.getAccountNumber()).isEqualTo("1234567890");
        assertThat(dto.getBank()).isEqualTo("GCB Bank");
        assertThat(dto.isRegistered()).isTrue();
    }

    // -------------------------------------------------------------------------
    // EmployeeMinorDto
    // -------------------------------------------------------------------------

    @Test
    void employeeMinorDtoToDto_mapsAllScalarFields() {
        Department dept = department(10, "HR", "Human Resources");
        Employee emp = employee(3, dept);

        EmployeeMinorDto dto = EmployeeMinorDto.toDto(emp);

        assertThat(dto.getId()).isEqualTo(3);
        assertThat(dto.getFirstName()).isEqualTo("John");
        assertThat(dto.getLastName()).isEqualTo("Doe");
        assertThat(dto.getPhoneNo()).isEqualTo("0244000002");
        assertThat(dto.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void employeeMinorDtoToDto_mapsFirstRoleName() {
        Department dept = department(10, "HR", "HR");
        Employee emp = employee(3, dept);

        EmployeeMinorDto dto = EmployeeMinorDto.toDto(emp);

        assertThat(dto.getRole()).isEqualTo("ROLE_HOD");
    }

    @Test
    void employeeMinorDtoToDto_mapsDepartmentNameAndDescription() {
        Department dept = department(10, "HR", "Human Resources");
        Employee emp = employee(3, dept);

        EmployeeMinorDto dto = EmployeeMinorDto.toDto(emp);

        assertThat(dto.getDepartment()).isNotNull();
        assertThat(dto.getDepartment().getName()).isEqualTo("HR");
        assertThat(dto.getDepartment().getDescription()).isEqualTo("Human Resources");
    }

    // -------------------------------------------------------------------------
    // FloatDto
    // -------------------------------------------------------------------------

    @Test
    void floatDtoToDto_mapsAllFields() {
        Floats f = new Floats();
        f.setId(7);
        f.setItemDescription("Petrol");
        f.setEstimatedUnitPrice(new BigDecimal("15.50"));
        f.setQuantity(4);

        FloatDto dto = FloatDto.toDto(f);

        assertThat(dto.getId()).isEqualTo(7);
        assertThat(dto.getItemDescription()).isEqualTo("Petrol");
        assertThat(dto.getEstimatedUnitPrice()).isEqualByComparingTo("15.50");
        assertThat(dto.getQuantity()).isEqualTo(4);
    }

    // -------------------------------------------------------------------------
    // FloatOrderDto  (dto package version)
    // -------------------------------------------------------------------------

    @Test
    void floatOrderDtoToDto_mapsScalarFields() {
        FloatOrder fo = buildFloatOrder();

        FloatOrderDto dto = FloatOrderDto.toDto(fo);

        assertThat(dto.getId()).isEqualTo(20);
        assertThat(dto.getStaffId()).isEqualTo("S001");
        assertThat(dto.getAmount()).isEqualByComparingTo("500.00");
        assertThat(dto.getDescription()).isEqualTo("Office supplies float");
        assertThat(dto.getFloatOrderRef()).isEqualTo("FOR-001");
        assertThat(dto.getCreatedDate()).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(dto.isFundsReceived()).isTrue();
        assertThat(dto.getApproval()).isEqualTo(RequestApproval.APPROVED);
        assertThat(dto.getStatus()).isEqualTo(RequestStatus.PROCESSED);
    }

    @Test
    void floatOrderDtoToDto_mapsDepartmentViaNestedToDto() {
        FloatOrder fo = buildFloatOrder();

        FloatOrderDto dto = FloatOrderDto.toDto(fo);

        assertThat(dto.getDepartment()).isNotNull();
        assertThat(dto.getDepartment().getName()).isEqualTo("Finance");
    }

    @Test
    void floatOrderDtoToDto_mapsCreatedByViaNestedToDto() {
        FloatOrder fo = buildFloatOrder();

        FloatOrderDto dto = FloatOrderDto.toDto(fo);

        assertThat(dto.getCreatedBy()).isNotNull();
        assertThat(dto.getCreatedBy().getFirstName()).isEqualTo("John");
    }

    // -------------------------------------------------------------------------
    // FloatOrder.FloatOrderDto  (model inner class – duplicate of above)
    // -------------------------------------------------------------------------

    @Test
    void floatOrderModelInnerDtoToDto_mapsScalarFields() {
        FloatOrder fo = buildFloatOrder();

        FloatOrder.FloatOrderDto dto = FloatOrder.FloatOrderDto.toDto(fo);

        assertThat(dto.getId()).isEqualTo(20);
        assertThat(dto.getStaffId()).isEqualTo("S001");
        assertThat(dto.getAmount()).isEqualByComparingTo("500.00");
        assertThat(dto.getFloatOrderRef()).isEqualTo("FOR-001");
        assertThat(dto.isFundsReceived()).isTrue();
        assertThat(dto.getApproval()).isEqualTo(RequestApproval.APPROVED);
        assertThat(dto.getStatus()).isEqualTo(RequestStatus.PROCESSED);
    }

    // -------------------------------------------------------------------------
    // FloatGrnDto
    // -------------------------------------------------------------------------

    @Test
    void floatGrnDtoToDto_mapsScalarFields() {
        FloatGRN grn = new FloatGRN();
        grn.setId(9L);
        grn.setApprovedByStoreManager(true);
        Date approvalDate = new Date();
        grn.setDateOfApprovalByStoreManager(approvalDate);
        grn.setEmployeeStoreManager(55);
        grn.setFloatOrderId(20);
        Date created = new Date();
        grn.setCreatedDate(created);
        Date updated = new Date();
        grn.setUpdateDate(updated);

        Department dept = department(10, "Finance", "Finance");
        Employee emp = employee(3, dept);
        grn.setCreatedBy(emp);

        FloatGrnDto dto = FloatGrnDto.toDto(grn);

        assertThat(dto.getId()).isEqualTo(9);
        assertThat(dto.isApprovedByStoreManager()).isTrue();
        assertThat(dto.getDateOfApprovalByStoreManager()).isEqualTo(approvalDate);
        assertThat(dto.getEmployeeStoreManager()).isEqualTo(55);
        assertThat(dto.getFloatOrderId()).isEqualTo(20);
        assertThat(dto.getCreatedDate()).isEqualTo(created);
        assertThat(dto.getUpdateDate()).isEqualTo(updated);
    }

    @Test
    void floatGrnDtoToDto_mapsCreatedByViaNestedToDto() {
        FloatGRN grn = new FloatGRN();
        grn.setId(9L);
        Department dept = department(10, "Finance", "Finance");
        Employee emp = employee(3, dept);
        grn.setCreatedBy(emp);

        FloatGrnDto dto = FloatGrnDto.toDto(grn);

        assertThat(dto.getCreatedBy()).isNotNull();
        assertThat(dto.getCreatedBy().getFirstName()).isEqualTo("John");
    }

    // -------------------------------------------------------------------------
    // QuotationMinorDto
    // -------------------------------------------------------------------------

    @Test
    void quotationMinorDtoToDto_mapsScalarFields() {
        Quotation q = new Quotation();
        q.setId(11);
        q.setQuotationRef("QUO-001");
        q.setHodReview(true);
        q.setAuditorReview(false);
        Date createdAt = new Date();
        q.setCreatedAt(createdAt);
        Date hodDate = new Date();
        q.setHodReviewDate(hodDate);
        Date auditorDate = new Date();
        q.setAuditorReviewDate(auditorDate);

        Supplier s = supplier(5);
        q.setSupplier(s);

        RequestDocument rd = requestDocument("quotation.pdf");
        q.setRequestDocument(rd);

        QuotationMinorDto dto = QuotationMinorDto.toDto(q);

        assertThat(dto.getId()).isEqualTo(11);
        assertThat(dto.getQuotationRef()).isEqualTo("QUO-001");
        assertThat(dto.isHodReview()).isTrue();
        assertThat(dto.isAuditorReview()).isFalse();
        assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
        assertThat(dto.getHodReviewDate()).isEqualTo(hodDate);
        assertThat(dto.getAuditorReviewDate()).isEqualTo(auditorDate);
    }

    @Test
    void quotationMinorDtoToDto_mapsSupplierNameExplicitly() {
        Quotation q = buildQuotation();

        QuotationMinorDto dto = QuotationMinorDto.toDto(q);

        assertThat(dto.getSupplier()).isEqualTo("Acme Ltd");
    }

    @Test
    void quotationMinorDtoToDto_mapsFileNameFromRequestDocument() {
        Quotation q = buildQuotation();

        QuotationMinorDto dto = QuotationMinorDto.toDto(q);

        assertThat(dto.getFileName()).isEqualTo("quotation.pdf");
    }

    @Test
    void quotationMinorDtoToDto_mapsRequestDocumentDto() {
        Quotation q = buildQuotation();

        QuotationMinorDto dto = QuotationMinorDto.toDto(q);

        assertThat(dto.getRequestDocument()).isNotNull();
        assertThat(dto.getRequestDocument().getFileName()).isEqualTo("quotation.pdf");
    }

    // -------------------------------------------------------------------------
    // LpoMinorDto
    // -------------------------------------------------------------------------

    @Test
    void lpoMinorDtoToDto_mapsScalarFields() {
        LocalPurchaseOrder lpo = new LocalPurchaseOrder();
        setId(lpo, 30);
        lpo.setLpoRef("LPO-2024-001");
        lpo.setIsApproved(true);
        Date delivery = new Date();
        lpo.setDeliveryDate(delivery);
        Date created = new Date();
        lpo.setCreatedAt(created);
        lpo.setRequestItems(Set.of());

        LpoMinorDto dto = LpoMinorDto.toDto(lpo);

        assertThat(dto.getId()).isEqualTo(30);
        assertThat(dto.getLpoRef()).isEqualTo("LPO-2024-001");
        assertThat(dto.getIsApproved()).isTrue();
        assertThat(dto.getDeliveryDate()).isEqualTo(delivery);
        assertThat(dto.getCreatedAt()).isEqualTo(created);
    }

    @Test
    void lpoMinorDtoToDto2_mapsScalarFields() {
        LocalPurchaseOrder lpo = new LocalPurchaseOrder();
        setId(lpo, 31);
        lpo.setLpoRef("LPO-2024-002");
        lpo.setIsApproved(false);
        lpo.setRequestItems(Set.of());

        LpoMinorDto dto = LpoMinorDto.toDto2(lpo);

        assertThat(dto.getId()).isEqualTo(31);
        assertThat(dto.getLpoRef()).isEqualTo("LPO-2024-002");
        assertThat(dto.getIsApproved()).isFalse();
    }

    // -------------------------------------------------------------------------
    // LpoDraftDto
    // -------------------------------------------------------------------------

    @Test
    void lpoDraftDtoToDto_mapsScalarFields() {
        LocalPurchaseOrderDraft draft = new LocalPurchaseOrderDraft();
        setId(draft, 40);
        draft.setSupplierId(5);
        Date delivery = new Date();
        draft.setDeliveryDate(delivery);
        Date created = new Date();
        draft.setCreatedAt(created);
        draft.setRequestItems(Set.of());

        LpoDraftDto dto = LpoDraftDto.toDto(draft);

        assertThat(dto.getId()).isEqualTo(40);
        assertThat(dto.getSupplierId()).isEqualTo(5);
        assertThat(dto.getDeliveryDate()).isEqualTo(delivery);
        assertThat(dto.getCreatedAt()).isEqualTo(created);
    }

    // -------------------------------------------------------------------------
    // RequestDocumentDto
    // -------------------------------------------------------------------------

    @Test
    void requestDocumentDtoToDto_mapsScalarFields() {
        RequestDocument rd = requestDocument("invoice.pdf");
        setId(rd, 12);

        RequestDocumentDto dto = RequestDocumentDto.toDto(rd);

        assertThat(dto.getId()).isEqualTo(12);
        assertThat(dto.getFileName()).isEqualTo("invoice.pdf");
        assertThat(dto.getDocumentType()).isEqualTo("PDF");
        assertThat(dto.getDocumentFormat()).isEqualTo("A4");
    }

    @Test
    void requestDocumentDtoToDto_createdByIsNullWhenNotSet() {
        RequestDocument rd = requestDocument("doc.pdf");
        // createdBy not set → Optional.empty()

        RequestDocumentDto dto = RequestDocumentDto.toDto(rd);

        assertThat(dto.getCreatedBy()).isNull();
        assertThat(dto.getCreatedDate()).isNull();
    }

    // -------------------------------------------------------------------------
    // GrnMinorDto
    // -------------------------------------------------------------------------

    @Test
    void grnMinorDtoToDto_mapsScalarFields() {
        GoodsReceivedNote grn = buildGoodsReceivedNote();

        GrnMinorDto dto = GrnMinorDto.toDto(grn);

        assertThat(dto.getId()).isEqualTo(50);
        assertThat(dto.getInvoiceAmountPayable()).isEqualByComparingTo("1200.00");
        assertThat(dto.getGrnRef()).isEqualTo("GRN-001");
    }

    @Test
    void grnMinorDtoToDto_mapsCreatedDateAsLocalDateTime() {
        GoodsReceivedNote grn = buildGoodsReceivedNote();
        LocalDateTime ts = LocalDateTime.of(2024, 3, 1, 10, 0);
        grn.setCreatedDate(ts);

        GrnMinorDto dto = GrnMinorDto.toDto(grn);

        assertThat(dto.getCreatedDate()).isEqualTo(ts);
    }

    @Test
    void grnMinorDtoToDto_invoiceMinorDto_mapsInvoiceNumber() {
        GoodsReceivedNote grn = buildGoodsReceivedNote();

        GrnMinorDto dto = GrnMinorDto.toDto(grn);

        assertThat(dto.getInvoice()).isNotNull();
        assertThat(dto.getInvoice().getInvoiceNumber()).isEqualTo("INV-2024-001");
    }

    @Test
    void grnMinorDtoToDto_invoiceMinorDto_mapsSupplierViaNestedDto() {
        GoodsReceivedNote grn = buildGoodsReceivedNote();

        GrnMinorDto dto = GrnMinorDto.toDto(grn);

        assertThat(dto.getInvoice().getSupplier()).isNotNull();
        assertThat(dto.getInvoice().getSupplier().getName()).isEqualTo("Acme Ltd");
    }

    @Test
    void grnMinorDtoToDto_invoiceMinorDto_mapsInvoiceDocumentFileName() {
        GoodsReceivedNote grn = buildGoodsReceivedNote();

        GrnMinorDto dto = GrnMinorDto.toDto(grn);

        assertThat(dto.getInvoice().getInvoiceDocument()).isEqualTo("invoice.pdf");
    }

    // -------------------------------------------------------------------------
    // PaymentDraftMinorDto
    // -------------------------------------------------------------------------

    @Test
    void paymentDraftMinorDtoToDto_mapsScalarFields() {
        // NOTE: BeanUtils skips Integer→int for AbstractAuditable generic getId(); id is
        // currently 0. The explicit replacement will fix this to 60.
        Payment payment = buildPayment();

        PaymentDraftMinorDto dto = PaymentDraftMinorDto.toDto(payment);

        assertThat(dto.getId()).isEqualTo(60);
        assertThat(dto.getPurchaseNumber()).isEqualTo("PO-001");
        assertThat(dto.getPaymentAmount()).isEqualByComparingTo("1000.00");
        assertThat(dto.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(dto.getPaymentMethod()).isEqualTo(PaymentMethod.CHEQUE);
        assertThat(dto.getChequeNumber()).isEqualTo("CHQ-12345");
        assertThat(dto.getBank()).isEqualTo("GCB Bank");
        assertThat(dto.getWithholdingTaxPercentage()).isEqualByComparingTo("0.05");
    }

    @Test
    void paymentDraftMinorDtoToDto_mapsGoodsReceivedNoteViaNestedToDto() {
        Payment payment = buildPayment();

        PaymentDraftMinorDto dto = PaymentDraftMinorDto.toDto(payment);

        assertThat(dto.getGoodsReceivedNote()).isNotNull();
        assertThat(dto.getGoodsReceivedNote().getGrnRef()).isEqualTo("GRN-001");
    }

    // -------------------------------------------------------------------------
    // PettyCashDto  (documents the existing NPE bug)
    // -------------------------------------------------------------------------

    @Test
    void pettyCashDtoToDto_throwsNullPointerBecauseItemIsNull() {
        PettyCash pc = new PettyCash();
        pc.setId(1);
        pc.setName("Gasoline");
        pc.setQuantity(2);
        pc.setAmount(BigDecimal.TEN);

        // PettyCashDto.item is never initialised — Spring Assert.notNull rejects null target
        assertThatThrownBy(() -> PettyCashDto.toDto(pc))
                .isInstanceOf(NullPointerException.class);
    }

    // -------------------------------------------------------------------------
    // PettyCash.PettyCashMinorDto
    // -------------------------------------------------------------------------

    @Test
    void pettyCashMinorDtoToDto_mapsScalarFields() {
        PettyCash pc = new PettyCash();
        pc.setId(70);
        pc.setStaffId("P4993");
        pc.setStatus(RequestStatus.PROCESSED);
        pc.setApproval(RequestApproval.APPROVED);
        pc.setEndorsement(EndorsementStatus.ENDORSED);
        pc.setPettyCashRef("PC-001");

        PettyCash.PettyCashMinorDto dto = PettyCash.PettyCashMinorDto.toDto(pc);

        assertThat(dto.getId()).isEqualTo(70);
        assertThat(dto.getStaffId()).isEqualTo("P4993");
        assertThat(dto.getStatus()).isEqualTo(RequestStatus.PROCESSED);
        assertThat(dto.getApproval()).isEqualTo(RequestApproval.APPROVED);
        assertThat(dto.getEndorsement()).isEqualTo(EndorsementStatus.ENDORSED);
        assertThat(dto.getPettyCashRef()).isEqualTo("PC-001");
    }

    @Test
    void pettyCashMinorDtoToDto_mapsCreatedByWhenPresent() {
        PettyCash pc = new PettyCash();
        pc.setId(70);
        Department dept = department(10, "Finance", "Finance");
        Employee emp = employee(3, dept);
        pc.setCreatedBy(emp);

        PettyCash.PettyCashMinorDto dto = PettyCash.PettyCashMinorDto.toDto(pc);

        assertThat(dto.getCreatedBy()).isNotNull();
        assertThat(dto.getCreatedBy().getFirstName()).isEqualTo("John");
    }

    @Test
    void pettyCashMinorDtoToDto_createdByIsNullWhenNotSet() {
        PettyCash pc = new PettyCash();
        pc.setId(70);

        PettyCash.PettyCashMinorDto dto = PettyCash.PettyCashMinorDto.toDto(pc);

        assertThat(dto.getCreatedBy()).isNull();
    }

    // -------------------------------------------------------------------------
    // PettyCashOrder.PettyCashOrderDto  (model inner class)
    // -------------------------------------------------------------------------

    @Test
    void pettyCashOrderInnerDtoToDto_mapsScalarFields() {
        // NOTE: BeanUtils skips Integer→int for AbstractAuditable generic getId(); id is
        // currently 0. The explicit replacement will fix this to 80.
        PettyCashOrder order = new PettyCashOrder();
        setId(order, 80);
        order.setStaffId("S0042");
        order.setPettyCashOrderRef("PCO-001");

        PettyCashOrder.PettyCashOrderDto dto = PettyCashOrder.PettyCashOrderDto.toDto(order);

        assertThat(dto.getId()).isEqualTo(80);
        assertThat(dto.getStaffId()).isEqualTo("S0042");
        assertThat(dto.getPettyCashOrderRef()).isEqualTo("PCO-001");
    }

    @Test
    void pettyCashOrderInnerDtoToDto_pettyCashIsNullDueToBug() {
        // The forEach loop creates ItemDtos but never adds them to pettyCashOrderDTO.pettyCash
        PettyCashOrder order = new PettyCashOrder();
        setId(order, 80);
        PettyCash pc = new PettyCash();
        pc.setId(1);
        pc.setName("Fuel");
        pc.setQuantity(1);
        pc.setAmount(BigDecimal.TEN);
        order.addPettyCash(pc);

        PettyCashOrder.PettyCashOrderDto dto = PettyCashOrder.PettyCashOrderDto.toDto(order);

        assertThat(dto.getPettyCash()).isNull();
    }

    // -------------------------------------------------------------------------
    // PettyCashOrderDto  (standalone dto-package class)
    // -------------------------------------------------------------------------

    @Test
    void pettyCashOrderDtoToDto_mapsScalarFields() {
        // NOTE: BeanUtils skips Integer→int for AbstractAuditable generic getId(); id is
        // currently 0. The explicit replacement will fix this to 81.
        PettyCashOrder order = new PettyCashOrder();
        setId(order, 81);
        order.setStaffId("S0043");
        order.setPettyCashOrderRef("PCO-002");

        PettyCashOrder.PettyCashOrderDto dto = PettyCashOrderDto.toDto(order);

        assertThat(dto.getId()).isEqualTo(81);
        assertThat(dto.getStaffId()).isEqualTo("S0043");
        assertThat(dto.getPettyCashOrderRef()).isEqualTo("PCO-002");
    }

    // -------------------------------------------------------------------------
    // RequestItemDto
    // -------------------------------------------------------------------------

    @Test
    void requestItemDtoToDto_mapsScalarFields() {
        RequestItem item = buildRequestItem();

        RequestItemDto dto = RequestItemDto.toDto(item);

        assertThat(dto.getId()).isEqualTo(90);
        assertThat(dto.getName()).isEqualTo("Laptop");
        assertThat(dto.getPurpose()).isEqualTo("For new hire");
        assertThat(dto.getQuantity()).isEqualTo(2);
        assertThat(dto.getStatus()).isEqualTo(RequestStatus.PROCESSED);
        assertThat(dto.getApproval()).isEqualTo(RequestApproval.APPROVED);
        assertThat(dto.getEndorsement()).isEqualTo(EndorsementStatus.ENDORSED);
        assertThat(dto.getRequestType()).isEqualTo(RequestType.GOODS_REQUEST);
        assertThat(dto.getCurrency()).isEqualTo("GHS");
        assertThat(dto.getUnitPrice()).isEqualByComparingTo("2500.00");
        assertThat(dto.getTotalPrice()).isEqualByComparingTo("5000.00");
        assertThat(dto.getRequestItemRef()).isEqualTo("RI-001");
    }

    @Test
    void requestItemDtoToDto_mapsEmployeeViaNestedToDto() {
        RequestItem item = buildRequestItem();

        RequestItemDto dto = RequestItemDto.toDto(item);

        assertThat(dto.getEmployee()).isNotNull();
        assertThat(dto.getEmployee().getFirstName()).isEqualTo("John");
        assertThat(dto.getEmployee().getDepartment().getName()).isEqualTo("Finance");
    }

    @Test
    void requestItemDtoToDto_mapsReceivingStoreNameOnly() {
        RequestItem item = buildRequestItem();

        RequestItemDto dto = RequestItemDto.toDto(item);

        assertThat(dto.getReceivingStore()).isNotNull();
        assertThat(dto.getReceivingStore().getName()).isEqualTo("Main Store");
    }

    @Test
    void requestItemDtoToDto_mapsUserDepartmentViaNestedToDto() {
        RequestItem item = buildRequestItem();

        RequestItemDto dto = RequestItemDto.toDto(item);

        assertThat(dto.getUserDepartment()).isNotNull();
        assertThat(dto.getUserDepartment().getName()).isEqualTo("Finance");
    }

    @Test
    void requestItemDtoToDto_suppliersAreNull_whenEmptyCollectionOnItem() {
        // The guard `!isEmpty()` in toDto() means an empty input set is never assigned;
        // the DTO's suppliers field stays null rather than an empty set.
        RequestItem item = buildRequestItem();
        item.setSuppliers(Set.of());

        RequestItemDto dto = RequestItemDto.toDto(item);

        assertThat(dto.getSuppliers()).isNull();
    }

    // -------------------------------------------------------------------------
    // Builders for complex entities
    // -------------------------------------------------------------------------

    private FloatOrder buildFloatOrder() {
        FloatOrder fo = new FloatOrder();
        fo.setId(20);
        fo.setStaffId("S001");
        fo.setAmount(new BigDecimal("500.00"));
        fo.setDescription("Office supplies float");
        fo.setFloatOrderRef("FOR-001");
        fo.setCreatedDate(LocalDate.of(2024, 1, 15));
        fo.setFundsReceived(true);
        fo.setApproval(RequestApproval.APPROVED);
        fo.setStatus(RequestStatus.PROCESSED);
        fo.setRetirementDate(new Date());

        Department dept = department(10, "Finance", "Finance");
        fo.setDepartment(dept);

        Employee emp = employee(3, dept);
        fo.setCreatedBy(emp);

        return fo;
    }

    private GoodsReceivedNote buildGoodsReceivedNote() {
        GoodsReceivedNote grn = new GoodsReceivedNote();
        grn.setId(50L);
        grn.setInvoiceAmountPayable(new BigDecimal("1200.00"));
        grn.setGrnRef("GRN-001");

        Department dept = department(10, "Finance", "Finance");
        Employee emp = employee(3, dept);
        grn.setCreatedBy(emp);

        Invoice invoice = new Invoice();
        setId(invoice, 15);
        invoice.setInvoiceNumber("INV-2024-001");
        invoice.setSupplier(supplier(5));
        invoice.setInvoiceDocument(requestDocument("invoice.pdf"));
        grn.setInvoice(invoice);

        return grn;
    }

    private Payment buildPayment() {
        Payment payment = new Payment();
        setId(payment, 60);
        payment.setPurchaseNumber("PO-001");
        payment.setPaymentAmount(new BigDecimal("1000.00"));
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaymentMethod(PaymentMethod.CHEQUE);
        payment.setChequeNumber("CHQ-12345");
        payment.setBank("GCB Bank");
        payment.setWithholdingTaxPercentage(new BigDecimal("0.05"));
        payment.setWithholdingTaxAmount(new BigDecimal("50.00"));
        payment.setStage(PaymentStage.DRAFT);
        payment.setGoodsReceivedNote(buildGoodsReceivedNote());
        return payment;
    }

    private Quotation buildQuotation() {
        Quotation q = new Quotation();
        q.setId(11);
        q.setQuotationRef("QUO-001");
        q.setSupplier(supplier(5));
        RequestDocument rd = requestDocument("quotation.pdf");
        q.setRequestDocument(rd);
        return q;
    }

    private RequestItem buildRequestItem() {
        Department dept = department(10, "Finance", "Finance");
        Employee emp = employee(3, dept);

        Store store = new Store();
        store.setName("Main Store");

        RequestItem item = new RequestItem();
        item.setId(90);
        item.setName("Laptop");
        item.setPurpose("For new hire");
        item.setQuantity(2);
        item.setStatus(RequestStatus.PROCESSED);
        item.setApproval(RequestApproval.APPROVED);
        item.setEndorsement(EndorsementStatus.ENDORSED);
        item.setRequestType(RequestType.GOODS_REQUEST);
        item.setCurrency("GHS");
        item.setUnitPrice(new BigDecimal("2500.00"));
        item.setTotalPrice(new BigDecimal("5000.00"));
        item.setRequestItemRef("RI-001");
        item.setEmployee(emp);
        item.setUserDepartment(dept);
        item.setReceivingStore(store);
        item.setSuppliers(Set.of());
        return item;
    }
}
