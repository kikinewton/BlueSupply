package com.logistics.supply.service;

import com.logistics.supply.dto.ItemDetailDTO;
import com.logistics.supply.dto.LpoMinorDTO;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.repository.LocalPurchaseOrderRepository;
import com.logistics.supply.repository.RoleRepository;
import com.logistics.supply.repository.SupplierRepository;
import com.logistics.supply.util.FileGenerationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocalPurchaseOrderService {
  private final LocalPurchaseOrderRepository localPurchaseOrderRepository;
  private final RoleRepository roleRepository;
  private final SupplierRepository supplierRepository;
  private final EmployeeRepository employeeRepository;
  private final FileGenerationUtil fileGenerationUtil;

  @Value("${config.lpo.template}")
  private String LPO_template;

  @Transactional(rollbackFor = Exception.class)
  public LocalPurchaseOrder saveLPO(LocalPurchaseOrder lpo) {
    return localPurchaseOrderRepository.save(lpo);
  }

  public long count() {
    return localPurchaseOrderRepository.count() + 1;
  }

  public LocalPurchaseOrder findByRequestItemId(int requestItemId) throws GeneralException {
    return localPurchaseOrderRepository
        .findLpoByRequestItem(requestItemId)
        .orElseThrow(() -> new GeneralException(LPO_NOT_FOUND, HttpStatus.NOT_FOUND));
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
                  i.setCurrency(x.getCurrency());
                  return i;
                })
            .collect(Collectors.toList());

    Supplier supplier = supplierRepository.findById(lpo.getSupplierId()).get();

    Role gmRole =
        roleRepository
            .findByName(EmployeeRole.ROLE_GENERAL_MANAGER.name())
            .orElseThrow(() -> new GeneralException(ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));

    Optional<Employee> manager = employeeRepository.getGeneralManager(gmRole.getId());
    if (!manager.isPresent()) {
      throw new GeneralException(EMPLOYEE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
    String generalManager = manager.get().getFullName();

    String procurementOfficer = lpo.getCreatedBy().get().getFullName();
    Context context = new Context();

    String pattern = "EEEEE dd MMMMM yyyy";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, new Locale("en", "UK"));

    String trDate = simpleDateFormat.format(lpo.getCreatedAt());

    context.setVariable("supplier", supplier.getName());
    context.setVariable("lpoId", lpo.getLpoRef());
    context.setVariable("trxdate", trDate);
    context.setVariable("paymentMethod", "CHEQUE");
    context.setVariable("generalManager", generalManager);
    context.setVariable("procuredItems", itemDetails);
    context.setVariable("procurementOfficer", procurementOfficer);
    String lpoGenerateHtml = fileGenerationUtil.parseThymeleafTemplate(LPO_template, context);

    String pdfName = supplier.getName().replace(" ", "") + "_lpo_" + lpoId + (new Date()).getTime();

    return fileGenerationUtil.generatePdfFromHtml(lpoGenerateHtml, pdfName).join();
  }

  @Cacheable(value = "allLpo")
  public List<LocalPurchaseOrder> findAll() {
    return localPurchaseOrderRepository.findAll();
  }

//  @Cacheable(value = "allLpoPage", key = "#{#pageNo, #pageSize}", unless = "#result == null")
  public Page<LocalPurchaseOrder> findAll(int pageNo, int pageSize) {
    Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
    return localPurchaseOrderRepository.findAll(pageable);
  }

  public LocalPurchaseOrder findLpoById(int lpoId) throws GeneralException {
    return localPurchaseOrderRepository
        .findById(lpoId)
        .orElseThrow(() -> new GeneralException(LPO_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  public LocalPurchaseOrder findLpoByRef(String lpoRef) throws GeneralException {
    return localPurchaseOrderRepository
        .findByLpoRef(lpoRef)
        .orElseThrow(() -> new GeneralException(LPO_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  public List<LocalPurchaseOrder> findLpoBySupplier(int supplierId) {
    return localPurchaseOrderRepository.findBySupplierId(supplierId);
  }

  @Cacheable(value = "lpoWithoutGRN")
  public List<LocalPurchaseOrder> findLpoWithoutGRN() {
    return localPurchaseOrderRepository.findLPOUnattachedToGRN();
  }

  @Cacheable(value = "lpoWithoutGRNByDepartment", key = "#department", unless = "#result == null")
  public List<LpoMinorDTO> findLpoWithoutGRNByDepartment(Department department) {
    return localPurchaseOrderRepository
        .findLPOUnattachedToGRNByDepartment(department.getId())
        .stream()
        .map(l -> LpoMinorDTO.toDto(l))
        .collect(Collectors.toList());
  }

  public List<LocalPurchaseOrder> findLpoLinkedToGRN() {
    return localPurchaseOrderRepository.findLPOLinkedToGRN();
  }

  @Transactional
  public void deleteLPO(int lpoId) {
    localPurchaseOrderRepository.deleteById(lpoId);
  }
}
