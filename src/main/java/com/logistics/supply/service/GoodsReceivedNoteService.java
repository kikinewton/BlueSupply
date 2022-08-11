package com.logistics.supply.service;

import com.logistics.supply.dto.GoodsReceivedNoteDTO;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.*;
import com.logistics.supply.util.FileGenerationUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.GRN_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsReceivedNoteService {

  private final FileGenerationUtil fileGenerationUtil;
  private final GoodsReceivedNoteRepository goodsReceivedNoteRepository;
  private final SupplierRepository supplierRepository;
  private final LocalPurchaseOrderRepository localPurchaseOrderRepository;

  private final EmployeeService employeeService;
  private final PaymentDraftRepository paymentDraftRepository;
  private final InvoiceRepository invoiceRepository;

  @Value("${config.goodsReceivedNote.template}")
  String goodsReceivedNoteTemplate;

  private final SpringTemplateEngine templateEngine;

  public List<GoodsReceivedNote> findAllGRN(int pageNo, int pageSize) {
    List<GoodsReceivedNote> goodsReceivedNotes = new ArrayList<>();
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      goodsReceivedNotes.addAll(goodsReceivedNoteRepository.findAll(pageable).getContent());
      return goodsReceivedNotes;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return goodsReceivedNotes;
  }

  public List<GoodsReceivedNote> findBySupplier(int supplierId) {
    return goodsReceivedNoteRepository.findBySupplier(supplierId);
  }

  public long count() {
    return goodsReceivedNoteRepository.count() + 1;
  }

  public GoodsReceivedNote findGRNById(long grnId) throws GeneralException {
    GoodsReceivedNote goodsReceivedNote =
        goodsReceivedNoteRepository
            .findById(grnId)
            .orElseThrow(() -> new GeneralException(GRN_NOT_FOUND, HttpStatus.NOT_FOUND));
    String hodFullName =
        employeeService
            .getDepartmentHOD(goodsReceivedNote.getLocalPurchaseOrder().getDepartment())
            .getFullName();
    goodsReceivedNote.getLocalPurchaseOrder().setDepartmentHod(hodFullName);
    return goodsReceivedNote;
  }

  @SneakyThrows
  public GoodsReceivedNote findByInvoice(int invoiceId) {
    return goodsReceivedNoteRepository
        .findByInvoiceId(invoiceId)
        .orElseThrow(() -> new GeneralException(GRN_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  public GoodsReceivedNote saveGRN(GoodsReceivedNote goodsReceivedNote) {
    return goodsReceivedNoteRepository.save(goodsReceivedNote);
  }

  //  @SneakyThrows
  @Transactional(rollbackFor = Exception.class)
  public GoodsReceivedNote updateGRN(int grnId, GoodsReceivedNoteDTO grnDto)
      throws GeneralException {
    GoodsReceivedNote grn = findGRNById(grnId);
    LocalPurchaseOrder lpo = localPurchaseOrderRepository.findById(grnDto.getLpo().getId()).get();
    Invoice invoice = invoiceRepository.findById(grnDto.getInvoice().getId()).get();
    BeanUtils.copyProperties(grnDto, grn);
    grn.setLocalPurchaseOrder(lpo);
    grn.setInvoice(invoice);

    try {
      return goodsReceivedNoteRepository.save(grn);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException("UPDATE GRN FAILED", HttpStatus.BAD_REQUEST);
  }

  public List<GoodsReceivedNote> findGRNWithoutHodApprovalPerDepartment(Department department) {
    List<GoodsReceivedNote> goodsReceivedNotes = findNonApprovedGRN(RequestReview.HOD_REVIEW);
    List<GoodsReceivedNote> grnForDepartment =
        goodsReceivedNotes.stream()
            .filter(
                g ->
                    g.getReceivedItems().stream()
                        .anyMatch(x -> x.getUserDepartment().equals(department)))
            .collect(Collectors.toList());
    if (grnForDepartment.isEmpty()) return new ArrayList<>();
    return grnForDepartment;
  }

  public List<GoodsReceivedNote> findGRNRequiringPaymentDate() {
    return goodsReceivedNoteRepository.findByPaymentDateIsNullAndApprovedByHodTrue();
  }

  public List<GoodsReceivedNote> findGRNWithoutCompletePayment() {
    List<GoodsReceivedNote> list = new ArrayList<>();
    try {
      list.addAll(goodsReceivedNoteRepository.grnWithoutCompletePayment());
      return list.stream()
          .map(
              g -> {
                boolean paymentDraftExist = paymentDraftRepository.existsByGoodsReceivedNote(g);
                g.setHasPendingPaymentDraft(paymentDraftExist);
                return g;
              })
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return list;
  }

  public List<GoodsReceivedNote> findNonApprovedGRN(RequestReview review) {
    return goodsReceivedNoteRepository.findByApprovedByHodFalse();
  }

  @SneakyThrows
  public File generatePdfOfGRN(int invoiceId) {
    GoodsReceivedNote grn =
        goodsReceivedNoteRepository
            .findByInvoiceId(invoiceId)
            .orElseThrow(() -> new GeneralException(GRN_NOT_FOUND, HttpStatus.NOT_FOUND));
    String supplierName = supplierRepository.findById(grn.getSupplier()).get().getName();
    String pattern = "EEEEE dd MMMMM yyyy";
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

  //  public String parseThymeleafTemplate(Context context) {
  //    return templateEngine.process(goodsReceivedNoteTemplate, context);
  //  }

  //  public File generatePdfFromHtml(String html, String pdfName)
  //      throws IOException, DocumentException {
  //    File file = File.createTempFile(pdfName, ".pdf");
  //
  //    OutputStream outputStream = new FileOutputStream(file);
  //    ITextRenderer renderer = new ITextRenderer();
  //    renderer.setDocumentFromString(html);
  //    renderer.layout();
  //    renderer.createPDF(outputStream);
  //    outputStream.close();
  //    if (Objects.isNull(file)) System.out.println("file is null");
  //    System.out.println("file to generate = " + file.getName());
  //    return file;
  //  }

  @SneakyThrows
  @Transactional(rollbackFor = Exception.class)
  public GoodsReceivedNote approveGRN(long grnId, int employeeId, EmployeeRole employeeRole) {
    return goodsReceivedNoteRepository
        .findById(grnId)
        .map(
            x -> {
              if (employeeRole == EmployeeRole.ROLE_HOD) {
                return hodGRNApproval(employeeId, x);
              }
              throw new IllegalStateException("Unexpected value: " + employeeRole);
            })
        .orElseThrow(() -> new GeneralException(GRN_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  private GoodsReceivedNote hodGRNApproval(int employeeId, GoodsReceivedNote x) {
    x.setApprovedByHod(true);
    x.setEmployeeHod(employeeId);
    x.setDateOfApprovalByHod(new Date());
    return goodsReceivedNoteRepository.save(x);
  }
}
