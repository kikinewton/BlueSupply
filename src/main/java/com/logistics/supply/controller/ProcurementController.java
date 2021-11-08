package com.logistics.supply.controller;

import com.logistics.supply.dto.MappingSuppliersAndRequestItemsDTO;
import com.logistics.supply.dto.ProcurementDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.service.*;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.*;
import java.util.stream.Collectors;

import static com.logistics.supply.util.CommonHelper.buildEmail;
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
public class ProcurementController {

  @Autowired private final EmailSender emailSender;
  @Autowired EmployeeService employeeService;
  @Autowired RequestItemService requestItemService;
  @Autowired ProcurementService procurementService;
  @Autowired LocalPurchaseOrderService localPurchaseOrderService;
  @Autowired SupplierService supplierService;
  @Autowired GeneratedQuoteService generatedQuoteService;

  @Autowired
  public ProcurementController(EmailSender emailSender) {
    this.emailSender = emailSender;
  }

  @Operation(summary = "Add unit-price to endorsed request items ", tags = "Procurement")
  @PutMapping(value = "/procurement/requestItem/procurementDetails")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseEntity<?> addProcurementInfo(
      Authentication authentication, @RequestBody @Valid ProcurementDTO procurementDTO) {
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    Optional<RequestItem> item =
        requestItemService.findById(procurementDTO.getRequestItem().getId());
    if (!item.isPresent()) return failedResponse("REQUEST_ITEM_NOT_FOUND");
    try {
      System.out.println("Trying to endorse after checking conditions");
      if (item.get().getEndorsement().equals(EndorsementStatus.ENDORSED)
          && item.get().getStatus().equals(RequestStatus.PENDING)
          && Objects.isNull(item.get().getSuppliedBy())) {
        System.out.println("Request can now be accessed for procurement details");
        RequestItem result =
            procurementService.assignProcurementDetails(item.get(), procurementDTO);
        requestItemService.saveRequest(item.get(), employee, RequestStatus.PENDING);
        if (Objects.isNull(result)) return failedResponse("UPDATE_REQUEST_ITEM_FAILED");
        //        Role role = role
        Employee generalManager = employeeService.getGeneralManager();
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
        ResponseDTO response =
            new ResponseDTO("PROCUREMENT_DETAILED_ADDED_SUCCESSFULLY", SUCCESS, result);
        return ResponseEntity.ok(response);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("ADD_PROCUREMENT_DETAIL_FAILED");
  }

  @PutMapping(value = "/procurement/assignSuppliers/requestItems")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseEntity<?> addSuppliersToRequestItem(
      @RequestBody MappingSuppliersAndRequestItemsDTO mappingDTO) {

    Set<RequestItem> items =
        mappingDTO.getRequestItems().stream()
            .filter(i -> requestItemService.existById(i.getId()))
            .map(r -> requestItemService.findById(r.getId()).get())
            .collect(Collectors.toSet());

    Set<Supplier> suppliers =
        mappingDTO.getSuppliers().stream()
            .map(s -> supplierService.findById(s.getId()))
            .collect(Collectors.toSet());

    Set<RequestItem> mappedRequests = procurementService.assignRequestToSupplier(suppliers, items);

    if (mappedRequests.size() > 0) {
      ResponseDTO response = new ResponseDTO("UPDATE_SUCCESSFUL", SUCCESS, mappedRequests);
      return ResponseEntity.ok(response);
    }
    return failedResponse("UPDATE_FAILED");
  }

  @GetMapping(value = "/procurement/endorsedItemsWithMultipleSuppliers")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseEntity<?> findEndorsedItemsWithMultipleSuppliers() {
    List<RequestItem> items = new ArrayList<>();
    items.addAll(requestItemService.getEndorsedItemsWithAssignedSuppliers());

    ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, items);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/procurement/endorsedItemsWithSupplierId/suppliers/{supplierId}")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> findRequestItemsBySupplierId(
      @PathVariable("supplierId") int supplierId) {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemService.findRequestItemsForSupplier(supplierId));
      ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, items);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("FETCH_FAILED");
  }

  @Operation(
      summary = "Generate a PDF with the list of request assigned to a supplier",
      tags = "PROCUREMENT")
  @GetMapping(value = "procurement/generateRequestListForSupplier/suppliers/{supplierId}")
  public void generateRequestListForSupplier(
      @PathVariable("supplierId") int supplierId,
      HttpServletResponse response) {
    try {

      File file = requestItemService.generateRequestListForSupplier(supplierId);
      if (Objects.isNull(file)) log.error("Error while generating Request list file");

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
      log.error(e.getMessage());
    }
  }

  private ResponseEntity<ResponseDTO> failedResponse(String message) {
    ResponseDTO failed = new ResponseDTO(message, ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
