package com.logistics.supply.controller;

import com.logistics.supply.dto.BulkFloatsDTO;
import com.logistics.supply.dto.ItemUpdateDTO;
import com.logistics.supply.dto.PagedResponseDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.enums.UpdateStatus;
import com.logistics.supply.event.listener.FundsReceivedFloatListener;
import com.logistics.supply.model.*;
import com.logistics.supply.service.*;
import com.logistics.supply.specification.FloatSpecification;
import com.logistics.supply.specification.SearchCriteria;
import com.logistics.supply.specification.SearchOperation;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.logistics.supply.util.Constants.SUCCESS;
import static com.logistics.supply.util.Helper.failedResponse;
import static com.logistics.supply.util.Helper.notFound;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FloatController {

  private final FloatService floatService;
  private final FloatOrderService floatOrderService;
  private final EmployeeService employeeService;
  private final RoleService roleService;
  private final RequestDocumentService requestDocumentService;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Operation(summary = "Tag float when the requester receives money from accounts", tags = "FLOATS")
  @PutMapping("/floatOrders/{floatOrderId}/receiveFunds")
  public ResponseEntity<?> setAllocateFundsToFloat(
      @PathVariable("floatOrderId") int floatOrderId, Authentication authentication) {
    try {
      FloatOrder allocatedOrder = floatOrderService.allocateFundsFloat(floatOrderId);
      if (Objects.isNull(allocatedOrder)) return notFound("FUNDS_ALLOCATION_FAILED");
      ResponseDTO response =
          new ResponseDTO("FUNDS_ALLOCATED_TO_FLOATS_SUCCESSFULLY", SUCCESS, allocatedOrder);
      Employee employee = employeeService.findEmployeeByEmail(authentication.getName());

      FundsReceivedFloatListener.FundsReceivedFloatEvent fundsReceivedFloatEvent =
          new FundsReceivedFloatListener.FundsReceivedFloatEvent(this, employee, allocatedOrder);
      applicationEventPublisher.publishEvent(fundsReceivedFloatEvent);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("ALLOCATE_FUNDS_TO_FLOAT_FAILED");
  }

  @ApiOperation("Get all floats by status")
  @GetMapping("/floats")
  public ResponseEntity<?> findAllFloat(
      @ApiParam(name = "approval", value = "The status of approval")
          Optional<RequestApproval> approval,
      @RequestParam(required = false) Optional<EndorsementStatus> endorsement,
      @RequestParam(required = false) Optional<Boolean> retired,
      @RequestParam(required = false) Optional<Boolean> awaitingFunds,
      @RequestParam(required = false) Optional<Boolean> awaitingGRN,
      @RequestParam(required = false) Optional<Boolean> receivedFundsAndNotRetired,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize) {
    try {

      if (approval.isPresent()) {
        return floatByApprovalStatus(approval.get(), pageNo, pageSize);
      }
      if (retired.isPresent()) {

        return floatsByRetiredStatus(true, pageNo, pageSize);
      }
      if (endorsement.isPresent()) {

        return floatByEndorsementStatus(endorsement.get(), pageNo, pageSize);
      }
      if (awaitingFunds.isPresent()) {

        return floatsAwaitingFunds(pageNo, pageSize);
      }
      if (awaitingGRN.isPresent()) {
        return floatsWithPendingGRN(pageNo, pageSize);
      }
      if (receivedFundsAndNotRetired.isPresent()) {
        return floatsReceivedFundNotRetired(pageNo, pageSize);
      } else {

        Page<Floats> floats = floatService.findAllFloats(pageNo, pageSize);
        if (floats != null) {
          return pagedResult(floats);
        }
        return notFound("NO_FLOAT_FOUND");
      }

    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return notFound("NO_FLOAT_FOUND");
  }

  private ResponseEntity<?> floatsReceivedFundNotRetired(int pageNo, int pageSize) {
    Page<Floats> floats = floatService.floatsReceivedFundsAndNotRetired(pageNo, pageSize);

    if (floats != null) {
      return pagedResult(floats);
    }
    return notFound("NO_FLOAT_FOUND");
  }

  private ResponseEntity<?> floatByRStatus(RequestStatus status, int pageNo, int pageSize) {
    Page<Floats> floats = floatService.findFloatsByRequestStatus(pageNo, pageSize, status);

    if (floats != null) {
      return pagedResult(floats);
    }
    return failedResponse("FETCH_FAILED");
  }

  public ResponseEntity<?> floatsWithPendingGRN(int pageNo, int pageSize) {
    Page<Floats> floats = floatService.floatsWithoutGRN(pageNo, pageSize);
    if (floats != null) return pagedResult(floats);
    return notFound("FLOAT_NOT_FOUND");
  }

  private ResponseEntity<?> floatsAwaitingFunds(int pageNo, int pageSize) {
    Page<Floats> floats = floatService.findFloatsAwaitingFunds(pageNo, pageSize);
    if (floats != null) return pagedResult(floats);
    return notFound("N0_FLOAT_FOUND");
  }

  private ResponseEntity<?> floatByEndorsementStatus(
      EndorsementStatus endorsement, int pageNo, int pageSize) {
    Page<Floats> floats = floatService.findFloatsByEndorseStatus(pageNo, pageSize, endorsement);
    if (floats != null) {
      return pagedResult(floats);
    }
    return failedResponse("FETCH_FAILED");
  }

  private ResponseEntity<?> floatByApprovalStatus(
      RequestApproval approval, int pageNo, int pageSize) {
    Page<Floats> floats = floatService.findByApprovalStatus(pageNo, pageSize, approval);

    if (floats != null) {
      return pagedResult(floats);
    }
    return notFound("NO_FLOAT_FOUND");
  }

  private ResponseEntity<?> floatsByRetiredStatus(Boolean retired, int pageNo, int pageSize) {
    Page<Floats> floats = floatService.findFloatsByRetiredStatus(pageNo, pageSize, retired);

    if (floats != null) {
      return pagedResult(floats);
    }
    return notFound("NO_FLOAT_FOUND");
  }

  @Operation(summary = "Get floats requested by employee")
  @GetMapping("/floatsForEmployee")
  public ResponseEntity<?> findByEmployee(Authentication authentication, Pageable pageable) {
    try {
      int employeeId = employeeService.findEmployeeByEmail(authentication.getName()).getId();

      Page<Floats> floats = floatService.findByEmployee(employeeId, pageable);
      if (floats != null) {
        return pagedResult(floats);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return notFound("NO_FLOAT_FOUND");
  }

  private ResponseEntity<?> pagedResult(Page<Floats> floats) {
    PagedResponseDTO.MetaData metaData =
        new PagedResponseDTO.MetaData(
            floats.getNumberOfElements(),
            floats.getPageable().getPageSize(),
            floats.getNumber(),
            floats.getTotalPages());
    PagedResponseDTO response =
        new PagedResponseDTO("FETCH_SUCCESSFUL", SUCCESS, metaData, floats.getContent());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/floatsForDepartment")
  @PreAuthorize("hasRole('ROLE_HOD')")
  public ResponseEntity<?> findByDepartment(Authentication authentication, Pageable pageable) {

    Department department =
        employeeService.findEmployeeByEmail(authentication.getName()).getDepartment();
    Page<Floats> floats = floatService.findPendingByDepartment(department, pageable);

    if (floats != null) {
      return pagedResult(floats);
    }
    return notFound("NO_FLOAT_FOUND");
  }

  @Operation(summary = "Get floats by department of login user")
  @GetMapping("/floats/department")
  public ResponseEntity<?> findByDept(
      Authentication authentication,
      Pageable pageable,
      @RequestParam(required = false) RequestStatus status) {
    Department department =
        employeeService.findEmployeeByEmail(authentication.getName()).getDepartment();
    FloatSpecification floatSpecification = new FloatSpecification();
    floatSpecification.add(new SearchCriteria("status", status, SearchOperation.EQUAL));
    floatSpecification.add(new SearchCriteria("department", department, SearchOperation.EQUAL));
    Page<Floats> floats = floatService.findByStatusAndDepartment(floatSpecification, pageable);
    if (floats != null) {
      return pagedResult(floats);
    }
    return notFound("FLOAT_NOT_FOUND");
  }

  @Operation(summary = "Get floats by floatRef")
  @GetMapping("/float/{floatRef}")
  public ResponseEntity<?> findFloatByRef(@PathVariable("floatRef") String floatRef) {
    try {
      Floats f = floatService.findByRef(floatRef);
      ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, f);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("FETCH_FAILED");
  }

  @PutMapping("/floatOrders/{floatOrderId}")
  @PreAuthorize("hasRole('ROLE_HOD') or hasRole('ROLE_GENERAL_MANAGER')")
  public ResponseEntity<?> changeState(
      @PathVariable("floatOrderId") int floatOrderId,
      @RequestParam UpdateStatus statusChange,
      Authentication authentication) {

    switch (statusChange) {
      case APPROVE:
        return approveFloats(floatOrderId, authentication);
      case ENDORSE:
        return endorseFloats(floatOrderId, authentication);
      case CANCEL:
        return cancelFloats(floatOrderId, authentication);
      default:
        return failedResponse("FAILED_REQUEST");
    }
  }

  private ResponseEntity<?> endorseFloats(int floatOrderId, Authentication authentication) {

    if (!checkAuthorityExist(authentication, EmployeeRole.ROLE_HOD))
      return failedResponse("FORBIDDEN_ACCESS");
    FloatOrder endorsedOrder = floatOrderService.endorse(floatOrderId, EndorsementStatus.ENDORSED);
    if (Objects.nonNull(endorsedOrder)) {
      ResponseDTO response = new ResponseDTO("ENDORSE_FLOATS_SUCCESSFUL", SUCCESS, endorsedOrder);
      return ResponseEntity.ok(response);
    }
    return failedResponse("FAILED_TO_ENDORSE");
  }

  private ResponseEntity<?> approveFloats(int floatOrderId, Authentication authentication) {
    if (!checkAuthorityExist(authentication, EmployeeRole.ROLE_GENERAL_MANAGER))
      return failedResponse("FORBIDDEN_ACCESS");
    FloatOrder approvedOrder = floatOrderService.approve(floatOrderId, RequestApproval.APPROVED);

    if (Objects.nonNull(approvedOrder)) {
      ResponseDTO response = new ResponseDTO("APPROVE_FLOAT_SUCCESSFUL", SUCCESS, approvedOrder);
      return ResponseEntity.ok(response);
    }

    return failedResponse("FAILED_TO_ENDORSE");
  }

  private ResponseEntity<?> cancelFloats(int floatOrderId, Authentication authentication) {

    if (checkAuthorityExist(authentication, EmployeeRole.ROLE_HOD)) {
      FloatOrder endorseCancel = floatOrderService.cancel(floatOrderId, EmployeeRole.ROLE_HOD);
      if (Objects.nonNull(endorseCancel)) {
        ResponseDTO response = new ResponseDTO("FLOAT_ENDORSEMENT_CANCELLED", SUCCESS, endorseCancel);
        return ResponseEntity.ok(response);
      }
    }

    if (checkAuthorityExist(authentication, EmployeeRole.ROLE_GENERAL_MANAGER)) {
      FloatOrder approveCancel = floatOrderService.cancel(floatOrderId, EmployeeRole.ROLE_HOD);
      if (Objects.nonNull(approveCancel)) {
        ResponseDTO response = new ResponseDTO("FLOAT_APPROVAL_CANCELLED", SUCCESS, approveCancel);
        return ResponseEntity.ok(response);
      }
    }
    return failedResponse("CANCEL_REQUEST_FAILED");
  }

  @Operation(summary = "Update the float request after comment")
  @PutMapping("floatOrders/{floatOrderId}")
  public ResponseEntity<?> updateFloat(
      @Valid @RequestBody ItemUpdateDTO updateDTO, @PathVariable("floatOrderId") int floatOrderId) {
    try {
      FloatOrder update = floatOrderService.updateFloat(floatOrderId, updateDTO);
      if (Objects.isNull(update)) return failedResponse("UPDATE_FAILED");
      ResponseDTO response = new ResponseDTO("UPDATE_FLOAT", SUCCESS, update);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("UPDATE_FAILED");
  }

  @Operation(summary = "Retire float process, upload supporting document of float")
  @PutMapping("floatOrders/{floatOrderId}/supportingDocument")
  public ResponseEntity<?> retireFloat(
      Authentication authentication,
      @PathVariable("floatOrderId") int floatOrderId,
      @RequestBody Set<RequestDocument> documents) {
    try {
      Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
      FloatOrder f = floatOrderService.findById(floatOrderId);
      if (f == null) return failedResponse("FLOAT_DOES_NOT_EXIST");
      boolean loginUserCreatedFloat = f.getCreatedBy().equals(employee);
      if (!loginUserCreatedFloat) return failedResponse("USER_NOT_ALLOWED_TO_RETIRE_FLOAT");
      if (documents.isEmpty()) return failedResponse("DOCUMENT_DOES_NOT_EXIST");
      FloatOrder updated = floatOrderService.uploadSupportingDoc(floatOrderId, documents);
      if (updated == null) return failedResponse("FAILED_TO_ASSIGN_DOCUMENT_TO_FLOAT");
      ResponseDTO response =
          new ResponseDTO("SUPPORTING_DOCUMENT_ASSIGNED_TO_FLOAT", SUCCESS, updated);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("FLOAT_RETIREMENT_FAILED");
  }

//  @PutMapping("floats/{floatId}/retirementApproval")
//  public ResponseEntity<?> approveSupportingDoc(
//      Authentication authentication, @PathVariable("floatId") int floatId) {
//    try {
//      EmployeeRole employeeRole = roleService.getEmployeeRole(authentication);
//      Floats floats = floatService.retirementApproval(floatId, employeeRole);
//      if (floats == null) return failedResponse("FLOAT_RETIREMENT_APPROVAL_FAILED");
//      ResponseDTO response =
//          new ResponseDTO("SUPPORTING_DOCUMENT_ASSIGNED_TO_FLOAT", SUCCESS, floats);
//      return ResponseEntity.ok(response);
//
//    } catch (Exception e) {
//      log.error(e.toString());
//    }
//    return failedResponse("RETIREMENT_APPROVAL_FAILED");
//  }
//

  @GetMapping("floatOrders")
  public ResponseEntity<?> getAllFloatOrders(
      Authentication authentication,
      @RequestParam(required = false) Optional<Boolean> stores,
      @RequestParam(required = false) Optional<Boolean> myRequest,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize) {
    try {
      if (stores.isPresent()
          && checkAuthorityExist(authentication, EmployeeRole.ROLE_STORE_OFFICER)) {
        Page<FloatOrder> floatOrders = floatOrderService.getAllFloatOrders(pageNo, pageSize, false);
        PagedResponseDTO.MetaData metaData =
            new PagedResponseDTO.MetaData(
                floatOrders.getNumberOfElements(),
                floatOrders.getPageable().getPageSize(),
                floatOrders.getNumber(),
                floatOrders.getTotalPages());
        PagedResponseDTO response =
            new PagedResponseDTO("FETCH_SUCCESSFUL", SUCCESS, metaData, floatOrders.getContent());
        //        ResponseDTO response =
        //            new ResponseDTO("NON_RETIRED_FLOAT_ORDER_FETCH_SUCCESSFULLY", SUCCESS,
        // floatOrders);
        return ResponseEntity.ok(response);
      }
      if (myRequest.isPresent()) {
        Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
        Page<FloatOrder> orders =
            floatOrderService.getAllEmployeeFloatOrder(pageNo, pageSize, employee);
        PagedResponseDTO.MetaData metaData =
            new PagedResponseDTO.MetaData(
                orders.getNumberOfElements(),
                orders.getPageable().getPageSize(),
                orders.getNumber(),
                orders.getTotalPages());
        PagedResponseDTO response =
            new PagedResponseDTO("FETCH_SUCCESSFUL", SUCCESS, metaData, orders.getContent());

        return ResponseEntity.ok(response);
      }
    } catch (Exception e) {
      log.error(e.toString());
      e.printStackTrace();
    }
    return notFound("FLOAT_ORDERS_NOT_FOUND");
  }

  @PutMapping("/floatOrders/{floatOrderRef}/addItems")
  public ResponseEntity<?> addFloatItems(
      Authentication authentication,
      @PathVariable("floatOrderRef") String floatOrderRef,
      @Valid BulkFloatsDTO floatsDTO) {
    try {
      Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
      FloatOrder updatedOrder =
          Optional.ofNullable(floatOrderService.findByRef(floatOrderRef))
              .filter(i -> i.getCreatedBy().get().equals(employee))
              .map(o -> floatOrderService.addFloatsToOrder(floatOrderRef, floatsDTO.getFloats()))
              .orElse(null);
      if (Objects.isNull(updatedOrder))
        return failedResponse("FLOAT_ITEMS_MUST_BE_ADDED_BY_FLOAT_CREATOR");
      ResponseDTO response =
          new ResponseDTO("FLOAT_ITEMS_ADDED_TO_FLOAT_ORDER", SUCCESS, updatedOrder);
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("ADD_FLOAT_ITEMS_FAILED");
  }

  private Boolean checkAuthorityExist(Authentication authentication, EmployeeRole role) {
    return authentication.getAuthorities().stream()
        .map(a -> a.getAuthority().equalsIgnoreCase(role.name()))
        .findAny()
        .isPresent();
  }
}
