package com.logistics.supply.service;

import com.itextpdf.html2pdf.HtmlConverter;
import com.logistics.supply.dto.ItemDetailDTO;
import com.logistics.supply.model.LocalPurchaseOrder;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.util.EmailComposer;
import com.logistics.supply.util.LpoTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.*;

@Service
@Slf4j
public class LocalPurchaseOrderService extends AbstractDataService {

  public LocalPurchaseOrder saveLPO(LocalPurchaseOrder lpo) {
    try {
      return localPurchaseOrderRepository.save(lpo);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  @Transactional(rollbackFor = Exception.class)
  public String generateLPOPdf(int lpoId) {
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

    String htmlTable = buildLpoHtmlTable(title, itemDetails);
    String lpoCompose = LpoTemplate.LpoCompose(lpoId, supplier.getName(), "", htmlTable);
    try {
      String outputFolder =
          System.getProperty("user.home")
              + File.separator
              + supplier.getName().replace(" ", "")
              + "_lpo_"
              + lpoId
              + ".pdf";
      OutputStream outputStream = new FileOutputStream(outputFolder);
      HtmlConverter.convertToPdf(lpoCompose, outputStream);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

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

  public LocalPurchaseOrder findLpoById(int lpoId) {
    try {
      return localPurchaseOrderRepository.findById(lpoId).get();
    } catch (Exception e) {
      e.printStackTrace();
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
}
