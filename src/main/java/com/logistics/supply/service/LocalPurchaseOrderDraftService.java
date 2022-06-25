package com.logistics.supply.service;

import com.logistics.supply.dto.ItemDetailDTO;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.LocalPurchaseOrderDraft;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.repository.LocalPurchaseOrderDraftRepository;
import com.logistics.supply.repository.RoleRepository;
import com.logistics.supply.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

  @Transactional(rollbackFor = Exception.class)
  public LocalPurchaseOrderDraft saveLPO(LocalPurchaseOrderDraft lpo) {
    return localPurchaseOrderDraftRepository.save(lpo);
  }

  public long count() {
    return localPurchaseOrderDraftRepository.count() + 1;
  }

  public LocalPurchaseOrderDraft findByRequestItemId(int requestItemId) {
    return localPurchaseOrderDraftRepository.findLpoByRequestItem(requestItemId);
  }

//  public String parseThymeleafTemplate(Context context) {
//
//    return templateEngine.process(LPO_template, context);
//  }

//  public File generatePdfFromHtml(String html, String pdfName)
//      throws IOException, DocumentException {
//    File file = File.createTempFile(pdfName, ".pdf");
//    OutputStream outputStream = new FileOutputStream(file);
//    System.out.println("step 2");
//    ITextRenderer renderer = new ITextRenderer();
//    renderer.setDocumentFromString(html);
//    renderer.layout();
//    renderer.createPDF(outputStream);
//    outputStream.close();
//    if (Objects.isNull(file)) System.out.println("file is null");
//    System.out.println("file in generate = " + file.getName());
//    return file;
//  }

  public List<LocalPurchaseOrderDraft> findAll() {
    return localPurchaseOrderDraftRepository.findAll();
  }

  public LocalPurchaseOrderDraft findLpoById(int lpoId) throws GeneralException {
    return localPurchaseOrderDraftRepository
        .findById(lpoId)
        .orElseThrow(() -> new GeneralException(LPO_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  public List<LocalPurchaseOrderDraft> findLpoBySupplier(int supplierId) {
    return localPurchaseOrderDraftRepository.findBySupplierId(supplierId);
  }

  public List<LocalPurchaseOrderDraft> findDraftAwaitingApproval() {
    return localPurchaseOrderDraftRepository.findDraftAwaitingApproval();
  }

  public void deleteLPO(int lpoId) {
    localPurchaseOrderDraftRepository.deleteById(lpoId);
  }
}
