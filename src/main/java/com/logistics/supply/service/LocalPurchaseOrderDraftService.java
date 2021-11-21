package com.logistics.supply.service;

import com.logistics.supply.dto.ItemDetailDTO;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.*;
import com.lowagie.text.DocumentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocalPurchaseOrderDraftService {

  private static final String PDF_RESOURCES = "/pdf-resources/";
  final LocalPurchaseOrderDraftRepository localPurchaseOrderDraftRepository;
  final RoleRepository roleRepository;
  final SupplierRepository supplierRepository;
  final EmployeeRepository employeeRepository;
  final RequestDocumentService requestDocumentService;

  @Autowired private SpringTemplateEngine templateEngine;

  @Value("${config.lpo.template}")
  private String LPO_template;

  private static String buildLpoHtmlTable(List<String> title, List<ItemDetailDTO> suppliers) {
    StringBuilder header = new StringBuilder();
    for (String t : title) header.append(String.format(tableHeader, t));

    header = new StringBuilder(String.format(tableRow, header));
    String sb =
        suppliers.stream()
            .map(
                s ->
                    String.format(tableData, s.getItemName())
                        + String.format(tableData, s.getUnitPrice())
                        + String.format(tableData, s.getQuantity())
                        + String.format(tableData, s.getTotalPrice()))
            .map(t -> String.format(tableRow, t))
            .collect(Collectors.joining("", "", ""));
    return header.toString().concat(sb);
  }

  public void createLPO() {}

  @Transactional(rollbackFor = Exception.class)
  public LocalPurchaseOrderDraft saveLPO(LocalPurchaseOrderDraft lpo) {
    try {
      return localPurchaseOrderDraftRepository.save(lpo);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  public long count() {
    return localPurchaseOrderDraftRepository.count() + 1;
  }

  public LocalPurchaseOrderDraft findByRequestItemId(int requestItemId) {
    try {
      return localPurchaseOrderDraftRepository.findLpoByRequestItem(requestItemId);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

//
  public String parseThymeleafTemplate(Context context) {

    return templateEngine.process(LPO_template, context);
  }

  public File generatePdfFromHtml(String html, String pdfName)
      throws IOException, DocumentException {
    File file = File.createTempFile(pdfName, ".pdf");
    //    File file = new File(pdfName + ".pdf");
    //    String outputFolder =
    //        System.getProperty("user.home") + File.separator + pdfName.replace(" ", "") + ".pdf";
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

  public List<LocalPurchaseOrderDraft> findAll() {
    List<LocalPurchaseOrderDraft> lpos = new ArrayList<>();
    try {
      lpos.addAll(localPurchaseOrderDraftRepository.findAll());
      return lpos;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return lpos;
  }

  public LocalPurchaseOrderDraft findLpoById(int lpoId) {
    try {
      Optional<LocalPurchaseOrderDraft> lpo = localPurchaseOrderDraftRepository.findById(lpoId);
      if (lpo.isPresent()) return lpo.get();
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public LocalPurchaseOrderDraft findLpoByRef(String lpoRef) {
    try {
      Optional<LocalPurchaseOrderDraft> lpo = localPurchaseOrderDraftRepository.findByLpoRef(lpoRef);
      if (lpo.isPresent()) return lpo.get();
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public List<LocalPurchaseOrderDraft> findLpoBySupplier(int supplierId) {
    List<LocalPurchaseOrderDraft> lpos = new ArrayList<>();
    try {
      lpos.addAll(localPurchaseOrderDraftRepository.findBySupplierId(supplierId));
      return lpos;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return lpos;
  }

  public List<LocalPurchaseOrderDraft> findLpoWithoutGRN() {
    try {
      List<LocalPurchaseOrderDraft> lpos = new ArrayList<>();
      lpos.addAll(localPurchaseOrderDraftRepository.findLPOUnattachedToGRN());
      return lpos;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return new ArrayList<>();
  }

  public List<LocalPurchaseOrderDraft> findLpoLinkedToGRN() {
    try {
      List<LocalPurchaseOrderDraft> lpos = new ArrayList<>();
      lpos.addAll(localPurchaseOrderDraftRepository.findLPOLinkedToGRN());
      return lpos;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return new ArrayList<>();
  }

  @Transactional
  public void deleteLPO(int lpoId) {
    Optional<LocalPurchaseOrderDraft> lpo = localPurchaseOrderDraftRepository.findById(lpoId);
    if (lpo.isPresent()) localPurchaseOrderDraftRepository.deleteById(lpoId);
  }
}
