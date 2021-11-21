package com.logistics.supply.controller;

import com.logistics.supply.dto.*;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.ProcurementType;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.enums.UpdateStatus;
import com.logistics.supply.event.*;
import com.logistics.supply.model.*;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.FloatService;
import com.logistics.supply.service.PettyCashService;
import com.logistics.supply.service.RequestItemService;
import com.logistics.supply.util.IdentifierUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@RestController
@Slf4j
@RequestMapping("/api")
public class MultiplierItemsController {

  @Autowired EmployeeService employeeService;
  @Autowired RequestItemService requestItemService;
  @Autowired FloatService floatService;
  @Autowired PettyCashService pettyCashService;
  @Autowired ApplicationEventPublisher applicationEventPublisher;

  @PostMapping("/multipleRequestItems")
  public ResponseEntity<?> addBulkRequest(
      @RequestBody @Valid MultipleItemDTO multipleItemDTO, Authentication authentication)
      throws Exception {
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    List<RequestItem> createdItems =
        multipleItemDTO.getMultipleRequestItem().stream()
            .map(i -> requestItemService.createRequestItem(i, employee))
            .collect(Collectors.toList());
    if (createdItems.isEmpty()) return failedResponse("FAILED");

    ResponseDTO response = new ResponseDTO("CREATED_REQUEST_ITEMS", SUCCESS, createdItems);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/bulkFloatOrPettyCash/{procurementType}")
  public ResponseEntity<?> addBulkFloatOrPettyCash(
      @Valid @RequestBody FloatOrPettyCashDTO bulkItems,
      @PathVariable("procurementType") ProcurementType procurementType,
      Authentication authentication) {
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    if (procurementType.equals(ProcurementType.FLOAT)) {
      Set<Floats> savedResult =
          bulkItems.getItems().stream()
              .map(i -> createFloat(employee, i))
              .collect(Collectors.toSet());
      if (!savedResult.isEmpty()) {
        FloatEvent floatEvent = new FloatEvent(this, savedResult);
        applicationEventPublisher.publishEvent(floatEvent);
        ResponseDTO response = new ResponseDTO("CREATED_FLOAT_ITEMS", SUCCESS, savedResult);
        return ResponseEntity.ok(response);
      }
      return failedResponse("FAILED_TO_CREATE_FLOATS");
    }
    if (procurementType.equals(ProcurementType.PETTY_CASH)) {
      Set<PettyCash> savedResult =
          bulkItems.getItems().stream()
              .map(i -> createPettyCash(employee, i))
              .filter(Objects::nonNull)
              .collect(Collectors.toSet());
      if (!savedResult.isEmpty()) {
        PettyCashEvent pettyCashEvent = new PettyCashEvent(this, savedResult);
        applicationEventPublisher.publishEvent(pettyCashEvent);
        ResponseDTO response = new ResponseDTO("CREATED_PETTY_CASH_ITEMS", SUCCESS, savedResult);
        return ResponseEntity.ok(response);
      }
      return failedResponse("FAILED_TO_CREATE_PETTY_CASH");
    }
    return failedResponse("FAILED");
  }

  @PutMapping(value = "requestItems/updateStatus/{statusChange}")
  @PreAuthorize("hasRole('ROLE_HOD') or hasRole('ROLE_GENERAL_MANAGER')")
  public ResponseEntity<?> updateMultipleRequestItem(
      @Valid @RequestBody BulkRequestItemDTO bulkRequestItem,
      @PathVariable("statusChange") UpdateStatus statusChange,
      Authentication authentication)
      throws Exception {
    switch (statusChange) {
      case ENDORSE:
        return endorseRequest(authentication, bulkRequestItem.getRequestItems());
      case APPROVE:
        return approveRequestGM(authentication, bulkRequestItem.getRequestItems());
      case CANCEL:
        return cancelRequest(authentication, bulkRequestItem.getRequestItems());
      case HOD_REVIEW:
        return reviewRequestAfterProcurement(authentication, bulkRequestItem.getRequestItems());
      default:
        return failedResponse("UPDATE_STATUS_FAILED");
    }
  }

  private Boolean checkAuthorityExist(Authentication authentication, EmployeeRole role) {
    return authentication.getAuthorities().stream()
        .map(x -> x.getAuthority().equalsIgnoreCase(role.name()))
        .filter(x -> x == true)
        .findAny()
        .get();
  }

  private ResponseEntity<?> cancelRequest(Authentication authentication, List<RequestItem> items) {
    int employeeId = employeeService.findEmployeeByEmail(authentication.getName()).getId();
    List<CancelledRequestItem> cancels =
        items.stream()
            .map(i -> requestItemService.cancelRequest(i.getId(), employeeId))
            .filter(c -> Objects.nonNull(c))
            .collect(Collectors.toList());
    if (cancels.size() > 0) {
      CancelRequestItemEvent cancelRequestItemEvent = new CancelRequestItemEvent(this, cancels);
      applicationEventPublisher.publishEvent(cancelRequestItemEvent);
      ResponseDTO response = new ResponseDTO("CANCELLED_REQUEST", SUCCESS, cancels);
      return ResponseEntity.ok(response);
    }
    return failedResponse("CANCEL_REQUEST_FAILED");
  }

  private ResponseEntity<?> approveRequestGM(
      Authentication authentication, List<RequestItem> items) {
    if (!checkAuthorityExist(authentication, EmployeeRole.ROLE_GENERAL_MANAGER))
      return failedResponse("FORBIDDEN_ACCESS");
    List<Boolean> approvedItems =
        items.stream()
            .map(item -> requestItemService.approveRequest(item.getId()))
            .map(y -> y.equals(Boolean.TRUE))
            .collect(Collectors.toList());
    if (approvedItems.size() > 0) {
      List<RequestItem> approved =
          items.stream()
              .filter(r -> requestItemService.findApprovedItemById(r.getId()).isPresent())
              .map(a -> requestItemService.findById(a.getId()).get())
              .collect(Collectors.toList());
      ApproveRequestItemEvent requestItemEvent = new ApproveRequestItemEvent(this, approved);
      applicationEventPublisher.publishEvent(requestItemEvent);
      ResponseDTO response = new ResponseDTO("APPROVAL_SUCCESSFUL", SUCCESS, approved);
      return ResponseEntity.ok(response);
    }
    return failedResponse("APPROVAL_FAILED");
  }

  private Floats createFloat(Employee employee, ItemDTO i) {
    Floats fl = new Floats();
    fl.setDepartment(employee.getDepartment());
    fl.setEstimatedUnitPrice(i.getUnitPrice());
    fl.setItemDescription(i.getName());
    fl.setQuantity(i.getQuantity());
    fl.setPurpose(i.getPurpose());
    fl.setCreatedBy(employee);
    String ref =
        IdentifierUtil.idHandler(
            "FLT", employee.getDepartment().getName(), String.valueOf(floatService.count()));
    fl.setFloatRef(ref);
    return floatService.saveFloat(fl);
  }

  private PettyCash createPettyCash(Employee employee, ItemDTO i) {
    PettyCash pettyCash = new PettyCash();
    pettyCash.setDepartment(employee.getDepartment());
    pettyCash.setName(i.getName());
    pettyCash.setPurpose(i.getPurpose());
    pettyCash.setSupportingDocument(i.getDocuments());
    pettyCash.setAmount(i.getUnitPrice());
    pettyCash.setQuantity(i.getQuantity());
    pettyCash.setCreatedBy(employee);
    String ref =
        IdentifierUtil.idHandler(
            "PTC", employee.getDepartment().getName(), String.valueOf(pettyCashService.count()));
    pettyCash.setPettyCashRef(ref);
    return pettyCashService.save(pettyCash);
  }

  private ResponseEntity<?> reviewRequestAfterProcurement(
      Authentication authentication, List<RequestItem> items) {
    if (!checkAuthorityExist(authentication, EmployeeRole.ROLE_HOD))
      return failedResponse("FORBIDDEN_ACCESS");
    Set<RequestItem> reviewList =
        items.stream()
            .filter(
                i ->
                    Objects.isNull(i.getRequestReview())
                        && i.getEndorsement().equals(EndorsementStatus.PENDING))
            .map(r -> requestItemService.updateRequestReview(r.getId(), RequestReview.HOD_REVIEW))
            .collect(Collectors.toSet());
    if (!reviewList.isEmpty()) {
      ResponseDTO response = new ResponseDTO("HOD_REVIEW_SUCCESSFUL", SUCCESS, reviewList);
      return ResponseEntity.ok(response);
    }
    return failedResponse("HOD_REVIEW_FAILED");
  }

  private ResponseEntity<?> endorseRequest(Authentication authentication, List<RequestItem> items)
      throws Exception {
    if (!checkAuthorityExist(authentication, EmployeeRole.ROLE_HOD))
      return failedResponse("FORBIDDEN_ACCESS");

    List<RequestItem> endorse =
        items.stream()
            .filter(
                x ->
                    (Objects.isNull(x.getSuppliedBy())
                        && x.getEndorsement().equals(EndorsementStatus.PENDING)
                        && Objects.isNull(x.getEndorsementDate())))
            .map(y -> requestItemService.endorseRequest(y.getId()))
            .collect(Collectors.toList());

    if (endorse.size() > 0) {

      BulkRequestItemEvent requestItemEvent = new BulkRequestItemEvent(this, endorse);
      applicationEventPublisher.publishEvent(requestItemEvent);
      ResponseDTO response = new ResponseDTO("REQUEST_ENDORSED", SUCCESS, endorse);
      return ResponseEntity.ok(response);
    }
    return failedResponse("FAILED_TO_ENDORSE");
  }

  public ResponseEntity<ResponseDTO> failedResponse(String message) {
    ResponseDTO failed = new ResponseDTO(message, ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
