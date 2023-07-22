package com.logistics.supply.service;

import com.logistics.supply.dto.ItemDetailDTO;
import com.logistics.supply.dto.LpoMinorDto;
import com.logistics.supply.exception.LpoNotFoundException;
import com.logistics.supply.exception.NotFoundException;
import com.logistics.supply.exception.SupplierNotFoundException;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.repository.LocalPurchaseOrderRepository;
import com.logistics.supply.repository.RoleRepository;
import com.logistics.supply.repository.SupplierRepository;
import com.logistics.supply.util.FileGenerationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

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
  @CacheEvict(
      value = {
        "lpoDraftAwaitingApproval",
        "lpoBySupplier",
        "lpoById",
        "lpoByRequestItemId",
        "lpoAwaitingApproval"
      },
      allEntries = true)
  public LocalPurchaseOrder saveLPO(LocalPurchaseOrder lpo) {
    return localPurchaseOrderRepository.save(lpo);
  }

  public long count() {
    return localPurchaseOrderRepository.count() + 1;
  }

  public LocalPurchaseOrder findByRequestItemId(int requestItemId) {
    return localPurchaseOrderRepository
        .findLpoByRequestItem(requestItemId)
        .orElseThrow(() -> new NotFoundException("Lpo with request item id %s not found".formatted(requestItemId)));
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

    BigDecimal totalCost =
        itemDetails.stream().map(i -> i.getTotalPrice()).reduce(BigDecimal.ZERO, BigDecimal::add);

    Supplier supplier = supplierRepository.findById(lpo.getSupplierId()).get();

    Role gmRole =
        roleRepository
            .findByName(EmployeeRole.ROLE_GENERAL_MANAGER.name())
            .orElseThrow(() -> new NotFoundException("Role General Manager not found"));

    Optional<Employee> manager = employeeRepository.getGeneralManager(gmRole.getId());
    if (!manager.isPresent()) {
      throw new NotFoundException("General manager not found");
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
    context.setVariable("totalCost", totalCost);
    String lpoGenerateHtml = fileGenerationUtil.parseThymeleafTemplate(LPO_template, context);

    String pdfName = supplier.getName().replace(" ", "") + "_lpo_" + lpoId + (new Date()).getTime();

    return fileGenerationUtil.generatePdfFromHtml(lpoGenerateHtml, pdfName).join();
  }

  @Cacheable(value = "allLpo")
  public List<LocalPurchaseOrder> findAll() {
    return localPurchaseOrderRepository.findAll();
  }

  //  @Cacheable(value = "allLpoPage", key = "#{#pageNo, #pageSize}", unless = "#result == null")
  public Page<LocalPurchaseOrder> findAll(Pageable pageable) {
    return localPurchaseOrderRepository.findAll(pageable);
  }

  public LocalPurchaseOrder findLpoById(int lpoId)  {
    return localPurchaseOrderRepository
        .findById(lpoId)
        .orElseThrow(() -> new LpoNotFoundException(lpoId));
  }

  public LocalPurchaseOrder findLpoByRef(String lpoRef)  {
    return localPurchaseOrderRepository
        .findByLpoRef(lpoRef)
        .orElseThrow(() -> new LpoNotFoundException(lpoRef));
  }

  public Page<LocalPurchaseOrder> findLpoBySupplierName(String supplierName, Pageable pageable) {
    Optional<Supplier> supplier = supplierRepository.findByNameEqualsIgnoreCase(supplierName);
    if(!supplier.isPresent()) throw new SupplierNotFoundException(supplierName);
    return localPurchaseOrderRepository.findBySupplierIdEqualsOrderByCreatedDateDesc(supplier.get().getId(),pageable);
  }

  public List<LocalPurchaseOrder> findLpoBySupplier(int supplierId) {
    return localPurchaseOrderRepository.findBySupplierId(supplierId);
  }

  public int countLpoWithoutGRN() {
    return localPurchaseOrderRepository.countLPOUnattachedToGRN();
  }

  @Cacheable(value = "lpoWithoutGRN")
  public Page<LpoMinorDto> findLpoDtoWithoutGRN(Pageable pageable) {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String username = ((UserDetails) principal).getUsername();
    Employee employee = employeeRepository.findByEmailAndEnabledIsTrue(username).get();

    // if the employee is in procurement, fetch all LPO unattached to GRN
    if (EmployeeRole.ROLE_PROCUREMENT_MANAGER
        .name()
        .equalsIgnoreCase(employee.getRoles().get(0).getName())) {
      Page<LocalPurchaseOrder> lpoUnattachedToGRNForProcurement =
          localPurchaseOrderRepository.findLPOUnattachedToGRNForProcurement(pageable);

      return lpoUnattachedToGRNForProcurement.map(LpoMinorDto::toDto2);
    }
    Department employeeDept = employee.getDepartment();
    log.info(
        "Get lpo to be reviewed by store officer: {} in department: {}",
        username,
        employeeDept.getName());
    Page<LocalPurchaseOrder> lpoUnattachedToGRN =
        localPurchaseOrderRepository.findLPOUnattachedToGRN(employeeDept.getId(), pageable);
    return lpoUnattachedToGRN.map(LpoMinorDto::toDto2);
  }

  @Cacheable(value = "lpoWithoutGRNByDepartment", key = "#department", unless = "#result == null")
  public List<LpoMinorDto> findLpoWithoutGRNByDepartment(Department department) {
    return localPurchaseOrderRepository
        .findLPOUnattachedToGRNByDepartment(department.getId())
        .stream()
        .map(l -> LpoMinorDto.toDto(l))
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
