package com.logistics.supply.service;

import com.logistics.supply.dto.GoodsReceivedNoteDTO;
import com.logistics.supply.model.GoodsReceivedNote;
import com.logistics.supply.model.Invoice;
import com.logistics.supply.model.LocalPurchaseOrder;
import com.logistics.supply.repository.GoodsReceivedNoteRepository;
import com.logistics.supply.repository.InvoiceRepository;
import com.logistics.supply.repository.LocalPurchaseOrderRepository;
import com.logistics.supply.repository.SupplierRepository;
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
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class GoodsReceivedNoteService {

  @Autowired GoodsReceivedNoteRepository goodsReceivedNoteRepository;
  @Autowired SupplierRepository supplierRepository;
  @Autowired LocalPurchaseOrderRepository localPurchaseOrderRepository;
  @Autowired InvoiceRepository invoiceRepository;

  @Value("${config.goodsReceivedNote.template}")
  String goodsReceivedNoteTemplate;

  @Autowired private SpringTemplateEngine templateEngine;

  public List<GoodsReceivedNote> findAllGRN(int pageNo, int pageSize) {
    List<GoodsReceivedNote> goodsReceivedNotes = new ArrayList<>();
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdDate").descending());
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

  public GoodsReceivedNote findGRNById(int grnId) {
    try {
      return goodsReceivedNoteRepository.findById(grnId).get();
    } catch (Exception e) {
      log.error(e.toString());
      e.printStackTrace();
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
      System.out.println("UPDATE GRN");
      return goodsReceivedNoteRepository.save(grn);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public List<GoodsReceivedNote> findGRNWithoutCompletePayment() {
    List<GoodsReceivedNote> list = new ArrayList<>();
    try {
      list.addAll(goodsReceivedNoteRepository.grnWithoutCompletePayment());
      return list;
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return list;
  }

  public File generatePdfOfGRN(int invoiceId) throws DocumentException, IOException {
    GoodsReceivedNote grn = goodsReceivedNoteRepository.findByInvoiceId(invoiceId);
    if (Objects.isNull(grn)) return null;
    String supplierName = supplierRepository.findById(grn.getSupplier()).get().getName();
    String pattern = "EEEEE dd MMMMM yyyy";
    DateTimeFormatter dTF = DateTimeFormatter.ofPattern("dd MMM uuuu");
    String deliveryDate = grn.getCreatedDate().get().format(dTF);
    System.out.println(grn.getLocalPurchaseOrder().getRequestItems());
    Context context = new Context();
    context.setVariable("invoiceNo", invoiceId);
    context.setVariable("supplier", supplierName);
    context.setVariable("grnId", grn.getId());
    context.setVariable("deliveryDate", deliveryDate);
    context.setVariable("receivedBy", grn.getCreatedBy().get().getFullName());
    context.setVariable("receivedItems", grn.getReceivedItems());
    String html = parseThymeleafTemplate(context);
    String pdfName =
        deliveryDate.replace(" ", "").concat("GRN_").concat(grn.getInvoice().getInvoiceNumber());
    return generatePdfFromHtml(html, pdfName);
  }

  public String parseThymeleafTemplate(Context context) {

    return templateEngine.process(goodsReceivedNoteTemplate, context);
  }

  public File generatePdfFromHtml(String html, String pdfName)
      throws IOException, DocumentException {
    File file = File.createTempFile(pdfName, ".pdf");

    OutputStream outputStream = new FileOutputStream(file);
    System.out.println("step 2");
    ITextRenderer renderer = new ITextRenderer();
    renderer.setDocumentFromString(html);
    renderer.layout();
    renderer.createPDF(outputStream);
    outputStream.close();
    if (Objects.isNull(file)) System.out.println("file is null");
    System.out.println("file in generate = " + file.getName());
    return file;
  }
}
