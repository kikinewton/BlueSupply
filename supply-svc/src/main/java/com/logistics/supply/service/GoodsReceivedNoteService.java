package com.logistics.supply.service;

import com.logistics.supply.dto.GoodsReceivedNoteDto;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.event.listener.GRNListener;
import com.logistics.supply.exception.GrnNotFoundException;
import com.logistics.supply.exception.NotFoundException;
import com.logistics.supply.interfaces.projections.GRNView;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.GoodsReceivedNoteRepository;
import com.logistics.supply.repository.PaymentDraftRepository;
import com.logistics.supply.util.FileGenerationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsReceivedNoteService {

  private final FileGenerationUtil fileGenerationUtil;
  private final GoodsReceivedNoteRepository goodsReceivedNoteRepository;
  private final SupplierService supplierService;
  private final LocalPurchaseOrderService localPurchaseOrderService;
  private final EmployeeService employeeService;
  private final PaymentDraftRepository paymentDraftRepository;
  private final RequestDocumentService requestDocumentService;
  private final InvoiceService invoiceService;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Value("${config.goodsReceivedNote.template}")
  String goodsReceivedNoteTemplate;

  private final SpringTemplateEngine templateEngine;

  public Page<GoodsReceivedNote> findAllGRN(int pageNo, int pageSize) {

      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return goodsReceivedNoteRepository.findAll(pageable);
  }

  public Page<GoodsReceivedNote> findBySupplier(int supplierId, Pageable pageable) {
    return goodsReceivedNoteRepository.findBySupplier(supplierId, pageable);
  }

  public long count() {
    return goodsReceivedNoteRepository.count() + 1;
  }

  public GoodsReceivedNote findGRNById(long grnId) {
    GoodsReceivedNote goodsReceivedNote =
        goodsReceivedNoteRepository
            .findById(grnId)
            .orElseThrow(() -> new GrnNotFoundException((int) grnId));
    String hodFullName =
        employeeService
            .getDepartmentHOD(goodsReceivedNote.getLocalPurchaseOrder().getDepartment())
            .getFullName();
    goodsReceivedNote.getLocalPurchaseOrder().setDepartmentHod(hodFullName);
    return goodsReceivedNote;
  }

  public GoodsReceivedNote findByInvoice(int invoiceId) {
    return goodsReceivedNoteRepository
        .findByInvoiceId(invoiceId)
        .orElseThrow(() -> new NotFoundException("GRN with invoice id: %s not found".formatted(invoiceId)));
  }

  public GoodsReceivedNote saveGRN(GoodsReceivedNote goodsReceivedNote) {

    log.info("Save the GRN in the service");
    GoodsReceivedNote savedGRN = goodsReceivedNoteRepository.save(goodsReceivedNote);
    sendCreateGRNEvent(savedGRN);
    return savedGRN;
  }

  private void sendCreateGRNEvent(GoodsReceivedNote goodsReceivedNote) {
    log.info("Send GRN created event");
    CompletableFuture.runAsync(() -> {

      GRNListener.GRNEvent grnEvent = new GRNListener.GRNEvent(this, goodsReceivedNote);
      applicationEventPublisher.publishEvent(grnEvent);
    });
  }

  @Transactional(rollbackFor = Exception.class)
  public GoodsReceivedNote updateGRN(int grnId, GoodsReceivedNoteDto grnDto) {
    GoodsReceivedNote grn = findGRNById(grnId);
    LocalPurchaseOrder lpo = localPurchaseOrderService.findLpoById(grnDto.getLpo().getId());
    Invoice invoice = invoiceService.findByInvoiceId(grnDto.getInvoice().getId());
    BeanUtils.copyProperties(grnDto, grn);
    grn.setLocalPurchaseOrder(lpo);
    grn.setInvoice(invoice);
      return goodsReceivedNoteRepository.save(grn);
  }

  public List<GoodsReceivedNote> findGRNWithoutHodApprovalPerDepartment(Department department) {
    return goodsReceivedNoteRepository.findByDepartmentAndApprovedByHodFalse(department.getId());
  }

  public Page<GRNView> findGrnWithPaymentDateExceeded(Pageable pageable) {
    return goodsReceivedNoteRepository.findGrnWithPaymentDateExceeded(pageable);
  }

  public List<GoodsReceivedNote> findGRNRequiringPaymentDate() {
    return goodsReceivedNoteRepository.findByPaymentDateIsNullAndApprovedByHodTrue();
  }

  public List<GoodsReceivedNote> findGRNWithoutCompletePayment() {

    log.info("Find GRNs without complete payment");
    List<GoodsReceivedNote> goodsReceivedNotes = goodsReceivedNoteRepository.grnWithoutCompletePayment();
    List<GoodsReceivedNote> list = new ArrayList<>();
    for (GoodsReceivedNote g : goodsReceivedNotes) {
      boolean paymentDraftExist = paymentDraftRepository.existsByGoodsReceivedNote(g);
      g.setHasPendingPaymentDraft(paymentDraftExist);
      list.add(g);
    }
    return list;
  }

  public List<GoodsReceivedNote> findNonApprovedGRN(RequestReview review) {
    return goodsReceivedNoteRepository.findByApprovedByHodFalse();
  }

  public File generatePdfOfGRN(int invoiceId) {

    log.info("Generate pdf of GRN for invoice id: {}", invoiceId);
    GoodsReceivedNote grn =
        goodsReceivedNoteRepository
            .findByInvoiceId(invoiceId)
            .orElseThrow(() -> new NotFoundException("GRN with invoice id: %s not found".formatted(invoiceId)));
    String supplierName = supplierService.findById(grn.getSupplier()).getName();

    DateTimeFormatter dTF = DateTimeFormatter.ofPattern("dd MMM uuuu");
    String deliveryDate = grn.getCreatedDate().format(dTF);

    Context context = new Context();
    String invoiceNo =
        grn.getInvoice().getInvoiceNumber() == null
            ? grn.getInvoice().getId().toString()
            : grn.getInvoice().getInvoiceNumber();
    context.setVariable("invoiceNo", invoiceNo);
    context.setVariable("supplier", supplierName);
    String grnId = grn.getGrnRef() == null ? String.valueOf(grn.getId()) : grn.getGrnRef();
    context.setVariable("grnId", grnId);
    context.setVariable("deliveryDate", deliveryDate);
    context.setVariable("receivedBy", grn.getCreatedBy().getFullName());
    context.setVariable("receivedItems", grn.getReceivedItems());
    String html = fileGenerationUtil.parseThymeleafTemplate(goodsReceivedNoteTemplate, context);
    String pdfName = deliveryDate.replace(" ", "").concat("GRN_").concat(grnId);
    return fileGenerationUtil.generatePdfFromHtml(html, pdfName).join();
  }



  @Transactional(rollbackFor = Exception.class)
  public GoodsReceivedNote approveGRN(long grnId, int employeeId, EmployeeRole employeeRole) {

    log.info("GRN approved by HOD with employeeId {}", employeeId);
    return goodsReceivedNoteRepository
        .findById(grnId)
        .map(
            x -> {
              if (employeeRole == EmployeeRole.ROLE_HOD) {
                return hodGRNApproval(employeeId, x);
              }
              throw new IllegalStateException("Unexpected value: " + employeeRole);
            })
        .orElseThrow(() -> new GrnNotFoundException((int) grnId));
  }

  private GoodsReceivedNote hodGRNApproval(int employeeId, GoodsReceivedNote goodsReceivedNote) {

    goodsReceivedNote.setApprovedByHod(true);
    goodsReceivedNote.setEmployeeHod(employeeId);
    goodsReceivedNote.setDateOfApprovalByHod(new Date());
    return goodsReceivedNoteRepository.save(goodsReceivedNote);
  }


}
