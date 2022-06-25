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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
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
import static com.logistics.supply.util.Helper.failedResponse;
import static com.logistics.supply.util.Helper.notFound;

@RestController
@Slf4j
@RequestMapping(value = "/api")
@CrossOrigin(
    origins = {
      "https://etornamtechnologies.github.io/skyblue-request-frontend-react",
      "http://localhost:4000"
    },
    allowedHeaders = "*")
@RequiredArgsConstructor
public class ProcurementController {
   private final EmailSender emailSender;
  private final EmployeeService employeeService;
  private final RequestItemService requestItemService;
  private final ProcurementService procurementService;
  private final SupplierService supplierService;



  @CacheEvict(cacheNames = "{#requestItemsHistoryByDepartment}", allEntries = true)
  @Operation(summary = "Add unit-price to endorsed request items ", tags = "PROCUREMENT")
  @PutMapping(value = "/procurement/requestItem/procurementDetails")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseEntity<?> addProcurementInfo(
      Authentication authentication, @RequestBody @Valid ProcurementDTO procurementDTO) {
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    Optional<RequestItem> item =
        requestItemService.findById(procurementDTO.getRequestItem().getId());
    if (!item.isPresent()) return failedResponse("REQUEST ITEM NOT FOUND");
    try {
      if (item.get().getEndorsement().equals(EndorsementStatus.ENDORSED)
          && item.get().getStatus().equals(RequestStatus.PENDING)
          && Objects.isNull(item.get().getSuppliedBy())) {
        RequestItem result =
            procurementService.assignProcurementDetails(item.get(), procurementDTO);
        requestItemService.saveRequest(item.get(), employee, RequestStatus.PENDING);
        if (Objects.isNull(result)) return failedResponse("UPDATE REQUEST ITEM FAILED");
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
            new ResponseDTO("PROCUREMENT DETAILED ADDED SUCCESSFULLY", SUCCESS, result);
        return ResponseEntity.ok(response);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("ADD PROCUREMENT DETAIL FAILED");
  }

  @Operation(summary = "Assign selected suppliers to endorsed request items", tags = "PROCUREMENT")
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
      ResponseDTO response = new ResponseDTO("UPDATE SUCCESSFUL", SUCCESS, mappedRequests);
      return ResponseEntity.ok(response);
    }
    return failedResponse("UPDATE FAILED");
  }

  @Operation(summary = "Get endorsed requests with multiple supplier", tags = "PROCUREMENT")
  @GetMapping(value = "/procurement/endorsedItemsWithMultipleSuppliers")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> findEndorsedItemsWithMultipleSuppliers() {
    List<RequestItem> items = new ArrayList<>();
    items.addAll(requestItemService.getEndorsedItemsWithAssignedSuppliers());

    ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, items);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get all endorsed request items for a supplier", tags = "PROCUREMENT")
  @GetMapping(value = "/procurement/endorsedItemsWithSupplierId/suppliers/{supplierId}")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> findRequestItemsBySupplierId(
      @PathVariable("supplierId") int supplierId) {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemService.findRequestItemsForSupplier(supplierId));
      ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, items);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return notFound("FETCH FAILED");
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
      log.error(e.toString());
    }
  }



}
