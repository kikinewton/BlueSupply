package com.logistics.supply.controller;

import com.logistics.supply.dto.*;
import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.*;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.RequestItemRepository;
import com.logistics.supply.repository.SupplierRepository;
import com.logistics.supply.service.AbstractRestService;
import com.logistics.supply.service.GeneratedQuoteService;
import com.logistics.supply.service.LocalPurchaseOrderService;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static com.logistics.supply.enums.RequestStatus.PROCESSED;
import static com.logistics.supply.util.CommonHelper.buildEmail;
import static com.logistics.supply.util.CommonHelper.getNullPropertyNames;
import static com.logistics.supply.util.Constants.*;

@RestController
@Slf4j
@RequestMapping(value = "/api")
@CrossOrigin(
    origins = {
      "https://etornamtechnologies.github.io/skyblue-request-frontend-react",
      "http://localhost:4000"
    },
    allowedHeaders = "*")
public class ProcurementController extends AbstractRestService {

  @Autowired private SupplierRepository supplierRepository;

  @Autowired private final EmailSender emailSender;

  @Autowired private RequestItemRepository requestItemRepository;

  @Autowired private GeneratedQuoteService generatedQuoteService;

  @Autowired
  public ProcurementController(EmailSender emailSender) {
    this.emailSender = emailSender;
  }

  @PutMapping(value = "/procurement/{employeeId}/requestItem/{requestItemId}")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseDTO<RequestItem> addProcurementInfo(
      @PathVariable("employeeId") int employeeId,
      @PathVariable("requestItemId") int requestItemId,
      @RequestBody ProcurementDTO procurementDTO) {
    String[] nullValues = getNullPropertyNames(procurementDTO);
    System.out.println("count of null properties: " + Arrays.stream(nullValues).count());

    Set<String> l = new HashSet<>(Arrays.asList(nullValues));
    if (l.size() > 0) {
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
    }

    Employee employee = employeeService.findEmployeeById(employeeId);
    System.out.println(
        EmployeeLevel.PROCUREMENT_OFFICER.name() + " " + employee.getRole().toString());
    if (Objects.isNull(employee)
        | !employee.getRole().equals(EmployeeLevel.PROCUREMENT_OFFICER.name()))
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);

    Optional<RequestItem> item = requestItemService.findById(requestItemId);
    if (!item.isPresent()) return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), null, ERROR);

    if (item.get().getTotalPrice().doubleValue() > 0 && item.get().getQuantity() < 1) {
      return new ResponseDTO<>(HttpStatus.NOT_ACCEPTABLE.name(), null, ERROR);
    }
    try {
      System.out.println("Trying to endorse after checking conditions");
      if (item.get().getEndorsement().equals(EndorsementStatus.ENDORSED)
          && item.get().getStatus().equals(RequestStatus.PENDING)
          && Objects.isNull(item.get().getSuppliedBy())) {
        System.out.println("Request can now be accessed for procurement details");
        RequestItem result =
            procurementService.assignProcurementDetails(item.get(), procurementDTO);
        requestItemService.saveRequest(item.get(), employee, RequestStatus.PENDING);
        if (Objects.isNull(result))
          return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), null, ERROR);
        Employee generalManager =
            employeeService.getGeneralManager(EmployeeRole.ROLE_GENERAL_MANAGER.ordinal());
        if (Objects.nonNull(generalManager)) {
          String emailContent =
              buildEmail(
                  generalManager.getLastName(),
                  REQUEST_PENDING_APPROVAL_LINK,
                  REQUEST_PENDING_APPROVAL_TITLE,
                  REQUEST_APPROVAL_MAIL);
          String generalManagerEmail = generalManager.getEmail();
          emailSender.sendMail(
              generalManagerEmail, EmailType.GENERAL_MANAGER_APPROVAL_MAIL, emailContent);
        }
        return new ResponseDTO<>(HttpStatus.OK.name(), result, SUCCESS);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

  @PutMapping(value = "/procurement/assignSuppliers/requestItems")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseDTO<Set<RequestItem>> addSuppliersToRequestItem(
      @RequestBody MappingSuppliersAndRequestItemsDTO mappingDTO) {

    Set<RequestItem> items =
        mappingDTO.getRequestItems().stream()
            .filter(i -> requestItemRepository.existsById(i.getId()))
            .map(r -> requestItemRepository.findById(r.getId()).get())
            .collect(Collectors.toSet());

    Set<Supplier> suppliers =
        mappingDTO.getSuppliers().stream()
            .map(s -> supplierRepository.findById(s.getId()).get())
            .collect(Collectors.toSet());

    Set<RequestItem> mappedRequests = procurementService.assignRequestToSupplier(suppliers, items);

    if (mappedRequests.size() > 0) {
      return new ResponseDTO<>(HttpStatus.OK.name(), mappedRequests, SUCCESS);
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

  @PutMapping(value = "/procurement/setSuppliedBy/requestItems")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseDTO<LocalPurchaseOrder> assignSupplierForRequestItems(
      @RequestBody SetSupplierDTO suppliedBy) {

    String[] nullValues = getNullPropertyNames(suppliedBy);
    System.out.println("count of null properties: " + Arrays.stream(nullValues).count());

    Set<String> l = new HashSet<>(Arrays.asList(nullValues));
    if (l.size() > 0) {
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
    }
    LocalPurchaseOrder lpo = procurementService.assignDetailsForMultipleItems(suppliedBy);
    if (Objects.nonNull(lpo)) {
      return new ResponseDTO<>(HttpStatus.OK.name(), lpo, SUCCESS);
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

  @GetMapping(value = "/procurement/localPurchaseOrders")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseDTO<List<LocalPurchaseOrder>> findAllLPOS() {
    List<LocalPurchaseOrder> lpos = localPurchaseOrderService.findAll();
    if (lpos.size() > 0) return new ResponseDTO<>(HttpStatus.OK.name(), lpos, SUCCESS);
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

  @GetMapping(value = "/procurement/localPurchaseOrders/supplier/{supplierId}")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseDTO<List<LocalPurchaseOrder>> findLPOBySupplier(
      @PathVariable("supplierId") int supplierId) {
    Optional<Supplier> supplier = supplierService.findBySupplierId(supplierId);
    if (!supplier.isPresent())
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, "SUPPLIER_NOT_FOUND");
    List<LocalPurchaseOrder> lpos = localPurchaseOrderService.findLpoBySupplier(supplierId);
    if (lpos.size() > 0) return new ResponseDTO<>(HttpStatus.OK.name(), lpos, SUCCESS);
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, "NO_LPO_EXIST_FOR_SUPPLIER");
  }

  @GetMapping(value = "/procurement/localPurchaseOrders/{lpoId}")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseDTO<LocalPurchaseOrder> findLPOById(@PathVariable("lpoId") int lpoId) {
    LocalPurchaseOrder lpo = localPurchaseOrderService.findLpoById(lpoId);
    if (Objects.nonNull(lpo)) return new ResponseDTO<>(HttpStatus.OK.name(), lpo, SUCCESS);
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

  @GetMapping(value = "/procurement/endorsedItemsWithMultipleSuppliers")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseDTO<List<RequestItem>> findEndorsedItemsWithMultipleSuppliers() {
    List<RequestItem> items = new ArrayList<>();
    items.addAll(requestItemService.getEndorsedItemsWithAssignedSuppliers());

    if (items.size() > 0) return new ResponseDTO<>(HttpStatus.OK.name(), items, SUCCESS);
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

  @GetMapping(value = "/procurement/endorsedItemsWithSupplierId/{supplierId}")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseDTO<List<RequestItem>> findRequestItemsBySupplierId(
      @PathVariable("supplierId") int supplierId) {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.getRequestItemsBySupplierId(supplierId));
      return new ResponseDTO<>(HttpStatus.OK.name(), items, SUCCESS);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

//  @GetMapping(value = "/document/lpo/old/{lpoId}")
//  public HttpEntity<byte[]> getLpoDocument(
//      @PathVariable("lpoId") int lpoId, HttpServletResponse response) throws Exception {
//    LocalPurchaseOrder lpo = this.localPurchaseOrderService.findLpoById(lpoId);
//    if (Objects.isNull(lpo)) System.out.println("lpo does not exist");
//
//    try {
//      var file = this.localPurchaseOrderService.generateLPOPdf(lpoId);
//
//      if (Objects.isNull(file)) System.out.println("something wrong somewhere");
//      HttpHeaders headers = new HttpHeaders();
//
//      System.out.println("file = " + file);
//      headers.add("Content-Disposition", "attachment; filename=" + file.getName());
//      return new HttpEntity<>(Files.readAllBytes(file.toPath()), headers);
//
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//    return null;
//  }

  @GetMapping(value = "/document/lpo/{lpoId}")
  public void getLpoDocumentInBrowser(
      @PathVariable("lpoId") int lpoId, HttpServletResponse response) throws Exception {
    System.out.println("lpoId print ");
    LocalPurchaseOrder lpo = this.localPurchaseOrderService.findLpoById(lpoId);
    if (Objects.isNull(lpo)) System.out.println("lpo does not exist");

    try {
      var file = this.localPurchaseOrderService.generateLPOPdf(lpoId);

      if (Objects.isNull(file)) System.out.println("something wrong somewhere");

      String mimeType = URLConnection.guessContentTypeFromName(file.getName());
      if (mimeType == null) {
        mimeType = "application/octet-stream";
      }
      response.setContentType(mimeType);
      response.setHeader(
          "Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));

      response.setContentLength((int) file.length());

      InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

      FileCopyUtils.copy(inputStream, response.getOutputStream());

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @GetMapping(value = "/requestItems/quotationsWithoutDocs")
  public ResponseDTO<List<RequestItem>> findRequestItemsWithoutDocsInQuotation() {
    List<RequestItem> items = new ArrayList<>();
    List<RequestItem> i = requestItemService.findRequestItemsWithoutDocInQuotation();
    if (i.size() > 0) {
      items.addAll(i);
      return new ResponseDTO<>(HttpStatus.OK.name(), items, SUCCESS);
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

  @PutMapping(value = "/requestItems/updateRequestItems")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseDTO<LocalPurchaseOrder> updateRequestItems(
      @RequestBody RequestItemListDTO requestItems) {
    try {
      Set<RequestItem> result =
          requestItems.getItems().stream()
              .filter(
                  r ->
                      (Objects.nonNull(r.getUnitPrice())
                          && Objects.nonNull(r.getRequestCategory())
                          && Objects.nonNull(r.getSuppliedBy())))
              .map(
                  i -> {
                    RequestItem item = requestItemService.findById(i.getId()).get();
                    item.setSuppliedBy(i.getSuppliedBy());
                    item.setUnitPrice(i.getUnitPrice());
                    item.setRequestCategory(i.getRequestCategory());
                    item.setStatus(RequestStatus.PROCESSED);
                    double totalPrice =
                        Double.parseDouble(String.valueOf(i.getUnitPrice())) * i.getQuantity();
                    item.setTotalPrice(BigDecimal.valueOf(totalPrice));
                    return requestItemRepository.save(item);
                  })
              .collect(Collectors.toSet());
      if (result.size() > 0) {
        LocalPurchaseOrder lpo = new LocalPurchaseOrder();
        lpo.setDeliveryDate(requestItems.getDeliveryDate());
        lpo.setComment("");
        lpo.setRequestItems(result);
        lpo.setSupplierId(result.stream().findFirst().get().getSuppliedBy());
        LocalPurchaseOrder newLpo = localPurchaseOrderService.saveLPO(lpo);
        if (Objects.nonNull(newLpo))
          return new ResponseDTO<>(HttpStatus.OK.name(), newLpo, SUCCESS);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

  @PostMapping(value = "/quotations/generateQuoteForSupplier")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public void generateQuoteForSupplier(@RequestBody GeneratedQuoteDTO request, HttpServletResponse response) {
    try {
      File file = generatedQuoteService.createQuoteForUnregisteredSupplier(request);
      if (Objects.isNull(file)) System.out.println("something wrong somewhere");

      String mimeType = URLConnection.guessContentTypeFromName(file.getName());
      if (mimeType == null) {
        mimeType = "application/octet-stream";
      }
      response.setContentType(mimeType);
      response.setHeader(
              "Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));

      response.setContentLength((int) file.length());

      InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

      FileCopyUtils.copy(inputStream, response.getOutputStream());
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  @PutMapping(value = "setProcurementType/{procurementType}")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseDTO setProcurementType(@RequestBody MultipleRequestItemDTO requestItemsDTO, @PathVariable("procurementType") ProcurementType procurementType) {
    List<RequestItem> items = requestItemsDTO.getRequestList().stream().map(i -> {
      RequestItem r = requestItemService.findById(i.getId()).get();
      r.setProcurementType(procurementType);
      r.setStatus(PROCESSED);
      return requestItemRepository.save(r);
    }).collect(Collectors.toList());
    if (items.size() > 0) return new ResponseDTO("SUCCESS", HttpStatus.OK.name());
    return new ResponseDTO("ERROR", HttpStatus.NOT_FOUND.name());
  }

  @GetMapping(value = "/requestItems/generateRequestListForSupplier/{supplierId}")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public void generateRequestListForSupplier(@PathVariable("supplierId") int supplierId, HttpServletResponse response) {
    try {
      File file = requestItemService.generateRequestListForSupplier(supplierId);
      if (Objects.isNull(file)) System.out.println("something wrong somewhere");

      String mimeType = URLConnection.guessContentTypeFromName(file.getName());
      if (mimeType == null) {
        mimeType = "application/octet-stream";
      }
      response.setContentType(mimeType);
      response.setHeader(
              "Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));

      response.setContentLength((int) file.length());

      InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

      FileCopyUtils.copy(inputStream, response.getOutputStream());
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

}
