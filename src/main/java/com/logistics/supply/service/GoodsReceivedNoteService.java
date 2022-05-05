package com.logistics.supply.service;

import com.logistics.supply.dto.GoodsReceivedNoteDTO;
import com.logistics.supply.dto.PaymentDTO;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.*;
import com.lowagie.text.DocumentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GoodsReceivedNoteService {

  @Autowired GoodsReceivedNoteRepository goodsReceivedNoteRepository;
  @Autowired SupplierRepository supplierRepository;
  @Autowired LocalPurchaseOrderRepository localPurchaseOrderRepository;
  @Autowired PaymentRepository paymentRepository;
  @Autowired PaymentDraftRepository paymentDraftRepository;
  @Autowired InvoiceRepository invoiceRepository;

  @Value("${config.goodsReceivedNote.template}")
  String goodsReceivedNoteTemplate;

  @Autowired private SpringTemplateEngine templateEngine;

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
    List<GoodsReceivedNote> goodsReceivedNotes = new ArrayList<>();
    try {
      goodsReceivedNotes.addAll(goodsReceivedNoteRepository.findBySupplier(supplierId));
      return goodsReceivedNotes;
    } catch (Exception e) {
      log.error(e.toString());
      e.printStackTrace();
    }
    return goodsReceivedNotes;
  }

  public long count() {
    return goodsReceivedNoteRepository.count() + 1;
  }

  public GoodsReceivedNote findGRNById(long grnId) {
    try {
      return goodsReceivedNoteRepository.findById(grnId).get();
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public GoodsReceivedNote findByInvoice(int invoiceId) {
    try {
      return goodsReceivedNoteRepository.findByInvoiceId(invoiceId);
    } catch (Exception e) {
      log.error(e.toString());
      e.printStackTrace();
    }
    return null;
  }

  @Transactional(rollbackFor = Exception.class)
  public GoodsReceivedNote saveGRN(GoodsReceivedNote goodsReceivedNote) {
    try {
      return goodsReceivedNoteRepository.save(goodsReceivedNote);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  @Transactional(rollbackFor = Exception.class)
  public GoodsReceivedNote updateGRN(int grnId, GoodsReceivedNoteDTO grnDto) {
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
    return null;
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
    try {
      return goodsReceivedNoteRepository
          .findByPaymentDateIsNullAndApprovedByGmTrueAndApprovedByHodTrue();
    } catch (Exception e) {
      log.error(e.toString());
    }
    return new ArrayList<>();
  }

  public List<GoodsReceivedNote> findGRNWithoutCompletePayment() {
    List<GoodsReceivedNote> list = new ArrayList<>();
    try {
      System.out.println(1);
      list.addAll(goodsReceivedNoteRepository.grnWithoutCompletePayment());
      return list.stream()
          .map(
              g -> {
                System.out.println(2);
                List<Payment> payment = paymentRepository.findByGoodsReceivedNote(g.getId());
                payment.forEach(System.out::println);
                boolean paymentDraftExist = paymentDraftRepository.existsByGoodsReceivedNote(g);
                g.setHasPendingPaymentDraft(paymentDraftExist);
                System.out.println(3);
                if (!payment.isEmpty()) {
                  System.out.println("before history");
                  List<PaymentDTO> history =
                      payment.stream()
                          .map(
                              p -> {
                                PaymentDTO pay = new PaymentDTO();
                                BeanUtils.copyProperties(p, pay);
                                return pay;
                              })
                          .collect(Collectors.toList());
                  System.out.println("print history");
                  history.forEach(System.out::println);
                  g.setPaymentHistory(history);
                  System.out.println("set history");
                }
                return g;
              })
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    System.out.println(10);
    return list;
  }

  public List<GoodsReceivedNote> findNonApprovedGRN(RequestReview review) {
    try {
      switch (review) {
        case HOD_REVIEW:
          return goodsReceivedNoteRepository.findByApprovedByHodFalse();
        case GM_REVIEW:
          return goodsReceivedNoteRepository.findByApprovedByGmFalseAndApprovedByHodTrue();
      }
    } catch (Exception e) {
      log.error(e.toString());
    }
    return new ArrayList<>();
  }

  public File generatePdfOfGRN(int invoiceId) throws DocumentException, IOException {
    GoodsReceivedNote grn = goodsReceivedNoteRepository.findByInvoiceId(invoiceId);
    if (Objects.isNull(grn)) return null;
    String supplierName = supplierRepository.findById(grn.getSupplier()).get().getName();
    String pattern = "EEEEE dd MMMMM yyyy";
    DateTimeFormatter dTF = DateTimeFormatter.ofPattern("dd MMM uuuu");
    String deliveryDate = grn.getCreatedDate().format(dTF);
    System.out.println(grn.getLocalPurchaseOrder().getRequestItems());
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
    String html = parseThymeleafTemplate(context);
    String pdfName = deliveryDate.replace(" ", "").concat("GRN_").concat(grnId);
    return generatePdfFromHtml(html, pdfName);
  }

  public String parseThymeleafTemplate(Context context) {

    return templateEngine.process(goodsReceivedNoteTemplate, context);
  }

  public File generatePdfFromHtml(String html, String pdfName)
      throws IOException, DocumentException {
    File file = File.createTempFile(pdfName, ".pdf");

    OutputStream outputStream = new FileOutputStream(file);
    ITextRenderer renderer = new ITextRenderer();
    renderer.setDocumentFromString(html);
    renderer.layout();
    renderer.createPDF(outputStream);
    outputStream.close();
    if (Objects.isNull(file)) System.out.println("file is null");
    System.out.println("file to generate = " + file.getName());
    return file;
  }

  @Transactional(rollbackFor = Exception.class)
  public GoodsReceivedNote approveGRN(long grnId, int employeeId, EmployeeRole employeeRole) {
    return goodsReceivedNoteRepository
        .findById(grnId)
        .map(
            x -> {
              switch (employeeRole) {
                case ROLE_GENERAL_MANAGER:
                  return gmGRNApproval(employeeId, x);
                case ROLE_HOD:
                  return hodGRNApproval(employeeId, x);
                default:
                  throw new IllegalStateException("Unexpected value: " + employeeRole);
              }
            })
        .orElse(null);
  }

  private GoodsReceivedNote gmGRNApproval(int employeeId, GoodsReceivedNote x) {
    x.setApprovedByGm(true);
    x.setEmployeeGm(employeeId);
    x.setDateOfApprovalByGm(new Date());
    return goodsReceivedNoteRepository.save(x);
  }

  private GoodsReceivedNote hodGRNApproval(int employeeId, GoodsReceivedNote x) {
    x.setApprovedByHod(true);
    x.setEmployeeHod(employeeId);
    x.setDateOfApprovalByHod(new Date());
    return goodsReceivedNoteRepository.save(x);
  }
}
