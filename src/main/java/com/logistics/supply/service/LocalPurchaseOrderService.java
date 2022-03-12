package com.logistics.supply.service;

import com.logistics.supply.dto.ItemDetailDTO;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.repository.LocalPurchaseOrderRepository;
import com.logistics.supply.repository.RoleRepository;
import com.logistics.supply.repository.SupplierRepository;
import com.lowagie.text.DocumentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocalPurchaseOrderService {

  private static final String PDF_RESOURCES = "/pdf-resources/";
  final LocalPurchaseOrderRepository localPurchaseOrderRepository;
  final RoleRepository roleRepository;
  final SupplierRepository supplierRepository;
  final EmployeeRepository employeeRepository;
  final RequestDocumentService requestDocumentService;
  @Value("${config.lpo.template}")
  String LPO_template;
  @Autowired private SpringTemplateEngine templateEngine;

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

  @Transactional(rollbackFor = Exception.class)
  public LocalPurchaseOrder saveLPO(LocalPurchaseOrder lpo) {
    try {
      return localPurchaseOrderRepository.save(lpo);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  public long count() {
    return localPurchaseOrderRepository.count() + 1;
  }

  public LocalPurchaseOrder findByRequestItemId(int requestItemId) {
    try {
      return localPurchaseOrderRepository.findLpoByRequestItem(requestItemId);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @Transactional(rollbackFor = Exception.class)
  public File generateLPOPdf(int lpoId) throws Exception {
    LocalPurchaseOrder lpo = findLpoById(lpoId);
    List<ItemDetailDTO> itemDetails =
        lpo.getRequestItems().stream()
            .map(
                x -> {
                  ItemDetailDTO i = new ItemDetailDTO();
                  i.setItemName(x.getName());
                  i.setQuantity(x.getQuantity());
                  i.setUnitPrice(x.getUnitPrice());
                  i.setTotalPrice(x.getTotalPrice());
                  return i;
                })
            .collect(Collectors.toList());

    List<String> title = new ArrayList<>();
    title.add("Item");
    title.add("Unit Price");
    title.add("Quantity");
    title.add("Total Cost");

    Supplier supplier = supplierRepository.findById(lpo.getSupplierId()).get();

    Role gmRole = roleRepository.findByName(EmployeeRole.ROLE_GENERAL_MANAGER.name());

    String generalManager = employeeRepository.getGeneralManager(gmRole.getId()).getFullName();

    String procurementOfficer = lpo.getCreatedBy().get().getFullName();
    Context context = new Context();

    String pattern = "EEEEE dd MMMMM yyyy";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, new Locale("en", "UK"));

    String trDate = simpleDateFormat.format(new Date());

    context.setVariable("supplier", supplier.getName());
    context.setVariable("lpoId", lpo.getLpoRef());
    context.setVariable("trxdate", trDate);
    context.setVariable("paymentMethod", "CHEQUE");
    context.setVariable("generalManager", generalManager);
    context.setVariable("procuredItems", itemDetails);
    context.setVariable("procurementOfficer", procurementOfficer);
    String lpoGenerateHtml = parseThymeleafTemplate(context);

    String pdfName =
        supplier.getName().replace(" ", "")
            + "_lpo_"
            + lpoId
            + (new Date()).toString().replace(" ", "");

    return generatePdfFromHtml(lpoGenerateHtml, pdfName);
  }

  public String parseThymeleafTemplate(Context context) {

    return templateEngine.process(LPO_template, context);
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
    return file;
  }

  public List<LocalPurchaseOrder> findAll() {
    List<LocalPurchaseOrder> lpos = new ArrayList<>();
    try {
      lpos.addAll(localPurchaseOrderRepository.findAll());
      return lpos;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return lpos;
  }

  public Page<LocalPurchaseOrder> findAll(int pageNo, int pageSize) {
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return localPurchaseOrderRepository.findAll(pageable);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public LocalPurchaseOrder findLpoById(int lpoId) {
    try {
      Optional<LocalPurchaseOrder> lpo = localPurchaseOrderRepository.findById(lpoId);
      if (lpo.isPresent()) return lpo.get();
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public LocalPurchaseOrder findLpoByRef(String lpoRef) {
    try {
      Optional<LocalPurchaseOrder> lpo = localPurchaseOrderRepository.findByLpoRef(lpoRef);
      if (lpo.isPresent()) return lpo.get();
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public List<LocalPurchaseOrder> findLpoBySupplier(int supplierId) {
    List<LocalPurchaseOrder> lpos = new ArrayList<>();
    try {
      lpos.addAll(localPurchaseOrderRepository.findBySupplierId(supplierId));
      return lpos;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return lpos;
  }

  public List<LocalPurchaseOrder> findLpoWithoutGRN() {
    try {
      List<LocalPurchaseOrder> lpos = new ArrayList<>();
      lpos.addAll(localPurchaseOrderRepository.findLPOUnattachedToGRN());
      return lpos;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return new ArrayList<>();
  }

  public List<LocalPurchaseOrder> findLpoWithoutGRN(Department department) {
    try {
      List<LocalPurchaseOrder> lpos = new ArrayList<>();
      lpos.addAll(
          localPurchaseOrderRepository.findLPOUnattachedToGRNByDepartment(department.getId()));
      return lpos;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return new ArrayList<>();
  }

  public List<LocalPurchaseOrder> findLpoLinkedToGRN() {
    try {
      List<LocalPurchaseOrder> lpos = new ArrayList<>();
      lpos.addAll(localPurchaseOrderRepository.findLPOLinkedToGRN());
      return lpos;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return new ArrayList<>();
  }

  @Transactional
  public void deleteLPO(int lpoId) {
    Optional<LocalPurchaseOrder> lpo = localPurchaseOrderRepository.findById(lpoId);
    if (lpo.isPresent()) localPurchaseOrderRepository.deleteById(lpoId);
  }
}
