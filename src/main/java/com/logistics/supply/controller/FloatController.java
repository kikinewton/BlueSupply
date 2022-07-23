package com.logistics.supply.controller;

import com.logistics.supply.dto.BulkFloatsDTO;
import com.logistics.supply.dto.ItemUpdateDTO;
import com.logistics.supply.dto.PagedResponseDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.enums.UpdateStatus;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.event.listener.FloatRetirementListener;
import com.logistics.supply.event.listener.FundsReceivedFloatListener;
import com.logistics.supply.model.*;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.FloatOrderService;
import com.logistics.supply.service.FloatService;
import com.logistics.supply.service.RequestDocumentService;
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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.FETCH_SUCCESSFUL;
import static com.logistics.supply.util.Constants.LOGIN_EXPIRED;
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
  private final RequestDocumentService requestDocumentService;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Operation(summary = "Tag float when the requester receives money from accounts", tags = "FLOATS")
  @PutMapping("/floatOrders/{floatOrderId}/receiveFunds")
  public ResponseEntity<?> setAllocateFundsToFloat(
      @PathVariable("floatOrderId") int floatOrderId, Authentication authentication) {
    try {
      FloatOrder allocatedOrder = floatOrderService.allocateFundsFloat(floatOrderId);
      if (Objects.isNull(allocatedOrder)) return notFound("FUNDS ALLOCATION FAILED");
      Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
      CompletableFuture.runAsync(
          () -> {
            FundsReceivedFloatListener.FundsReceivedFloatEvent fundsReceivedFloatEvent =
                new FundsReceivedFloatListener.FundsReceivedFloatEvent(
                    this, employee, allocatedOrder);
            applicationEventPublisher.publishEvent(fundsReceivedFloatEvent);
          });
      return ResponseDTO.wrapSuccessResult(
          allocatedOrder, "FUNDS ALLOCATED TO FLOATS SUCCESSFULLY");

    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("ALLOCATE FUNDS TO FLOAT FAILED");
  }

  @GetMapping("/floats")
  public ResponseEntity<?> findAllFloat(
      Optional<RequestApproval> approval,
      @RequestParam(required = false) Optional<EndorsementStatus> endorsement,
      @RequestParam(required = false) Optional<Boolean> retired,
      @RequestParam(required = false) Optional<Boolean> gmRetire,
      @RequestParam(required = false) Optional<Boolean> auditorRetire,
      @RequestParam(required = false) Optional<Boolean> awaitingFunds,
      @RequestParam(required = false) Optional<Boolean> awaitingGRN,
      @RequestParam(required = false) Optional<Boolean> receivedFundsAndNotRetired,
      @RequestParam(required = false) Optional<Boolean> awaitingDocument,
      @RequestParam(required = false) Optional<Boolean> closeRetirement,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize,
      Authentication authentication) {
    try {
      if (authentication == null) return failedResponse(LOGIN_EXPIRED);
      if (approval.isPresent()) {
        return floatByApprovalStatus(approval.get(), pageNo, pageSize);
      }
      if (retired.isPresent()) {

        return floatsByRetiredStatus(true, pageNo, pageSize);
      }
      if (auditorRetire.isPresent()) return floatsRetiredStatus(authentication, pageNo, pageSize);
      if (gmRetire.isPresent()) return floatsRetiredStatus(authentication, pageNo, pageSize);
      if (awaitingDocument.isPresent()) {
        int employeeId = employeeService.findEmployeeByEmail(authentication.getName()).getId();
        return floatAwaitingDocument(pageNo, pageSize, employeeId);
      }

      if (endorsement.isPresent()) {

        return floatByEndorsementStatus(endorsement.get(), pageNo, pageSize);
      }
      if (awaitingFunds.isPresent()) {

        return floatsAwaitingFunds(pageNo, pageSize);
      }
      if (closeRetirement.isPresent()) return floatsToCloseRetirement(pageNo, pageSize);
      if (awaitingGRN.isPresent()) {
        return notFound("Not implemented");
      }
      if (receivedFundsAndNotRetired.isPresent()) {
        return floatsReceivedFundNotRetired(pageNo, pageSize);
      } else {

        Page<FloatOrder> floats = floatOrderService.findAllFloatOrder(pageNo, pageSize);
        if (floats != null) {
          return PagedResponseDTO.wrapSuccessResult(floats, FETCH_SUCCESSFUL);
        }
        return notFound("NO FLOAT FOUND");
      }

    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return notFound("NO FLOAT FOUND");
  }

  private ResponseEntity<?> floatsReceivedFundNotRetired(int pageNo, int pageSize) {
    Page<FloatOrder> floats = floatOrderService.floatsReceivedFundsAndNotRetired(pageNo, pageSize);
    if (floats != null) {
      return PagedResponseDTO.wrapSuccessResult(floats, FETCH_SUCCESSFUL);
    }
    return notFound("NO FLOAT FOUND");
  }

  private ResponseEntity<?> floatsToCloseRetirement(int pageNo, int pageSize)
      throws GeneralException {
    Page<FloatOrder> floatOrders = floatOrderService.findFloatOrderToClose(pageNo, pageSize);
    if (floatOrders != null) {
      return PagedResponseDTO.wrapSuccessResult(floatOrders, FETCH_SUCCESSFUL);
    }
    return notFound("NO FLOAT FOUND");
  }

  private ResponseEntity<?> floatByRStatus(RequestStatus status, int pageNo, int pageSize)
      throws GeneralException {
    Page<FloatOrder> floats = floatOrderService.findFloatsByRequestStatus(pageNo, pageSize, status);
    if (floats != null) {
      return PagedResponseDTO.wrapSuccessResult(floats, FETCH_SUCCESSFUL);
    }
    return failedResponse("FETCH FAILED");
  }

  private ResponseEntity<?> floatsAwaitingFunds(int pageNo, int pageSize) {
    Page<FloatOrder> floats = floatOrderService.findFloatsAwaitingFunds(pageNo, pageSize);
    if (floats != null) return PagedResponseDTO.wrapSuccessResult(floats, FETCH_SUCCESSFUL);
    return notFound("N0 FLOAT FOUND");
  }

  private ResponseEntity<?> floatByEndorsementStatus(
      EndorsementStatus endorsement, int pageNo, int pageSize) throws GeneralException {
    Page<FloatOrder> floats =
        floatOrderService.findFloatsByEndorseStatus(pageNo, pageSize, endorsement);
    if (floats != null) {
      return PagedResponseDTO.wrapSuccessResult(floats, FETCH_SUCCESSFUL);
    }
    return failedResponse("FETCH FAILED");
  }

  private ResponseEntity<?> floatAwaitingDocument(int pageNo, int pageSize, int employeeId) {
    Page<FloatOrder> floats =
        floatOrderService.findFloatsAwaitingDocument(pageNo, pageSize, employeeId);
    if (floats != null) {
      return PagedResponseDTO.wrapSuccessResult(floats, FETCH_SUCCESSFUL);
    }
    return notFound("NO FLOAT_FOUND");
  }

  private ResponseEntity<?> floatsRetiredStatus(
      Authentication authentication, int pageNo, int pageSize) throws GeneralException {
    if (checkAuthorityExist(authentication, EmployeeRole.ROLE_AUDITOR)) {
      Page<FloatOrder> floats = floatOrderService.floatOrderForAuditorRetire(pageNo, pageSize);
      if (floats != null) {
        return PagedResponseDTO.wrapSuccessResult(floats, FETCH_SUCCESSFUL);
      }
    } else if (checkAuthorityExist(authentication, EmployeeRole.ROLE_GENERAL_MANAGER)) {
      Page<FloatOrder> floats = floatOrderService.floatOrdersForGmRetire(pageNo, pageSize);
      if (floats != null) {
        return PagedResponseDTO.wrapSuccessResult(floats, FETCH_SUCCESSFUL);
      }
    }
    return notFound("NO FLOAT FOUND");
  }

  private ResponseEntity<?> floatByApprovalStatus(
      RequestApproval approval, int pageNo, int pageSize) {
    Page<FloatOrder> floats = floatOrderService.findByApprovalStatus(pageNo, pageSize, approval);
    if (floats != null) {
      return PagedResponseDTO.wrapSuccessResult(floats, FETCH_SUCCESSFUL);
    }
    return notFound("NO FLOAT FOUND");
  }

  private ResponseEntity<?> floatsByRetiredStatus(Boolean retired, int pageNo, int pageSize) {
    Page<FloatOrder> floats =
        floatOrderService.findFloatsByRetiredStatus(pageNo, pageSize, retired);

    if (floats != null) {
      return PagedResponseDTO.wrapSuccessResult(floats, FETCH_SUCCESSFUL);
    }
    return notFound("NO FLOAT FOUND");
  }

  @GetMapping("floatOrders/{floatOrderId}")
  public ResponseEntity<?> findByFloatOrderId(@PathVariable("floatOrderId") int floatOrderId) {
    try {
      FloatOrder order = floatOrderService.findById(floatOrderId);
      return ResponseDTO.wrapSuccessResult(order, FETCH_SUCCESSFUL);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return notFound("NO FLOAT FOUND");
  }

  @Operation(summary = "Get floats requested by employee")
  @GetMapping("/floatsForEmployee")
  public ResponseEntity<?> findByEmployee(Authentication authentication, Pageable pageable) {
    try {
      int employeeId = employeeService.findEmployeeByEmail(authentication.getName()).getId();

      Page<FloatOrder> floats = floatOrderService.findByEmployee(employeeId, pageable);
      if (floats != null) {
        return PagedResponseDTO.wrapSuccessResult(floats, FETCH_SUCCESSFUL);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return notFound("NO FLOAT FOUND");
  }

  @GetMapping("/floatsForDepartment")
  @PreAuthorize("hasRole('ROLE_HOD')")
  public ResponseEntity<?> findByDepartment(Authentication authentication, Pageable pageable) {

    Department department =
        employeeService.findEmployeeByEmail(authentication.getName()).getDepartment();
    Page<FloatOrder> floats = floatOrderService.findPendingByDepartment(department, pageable);
    return PagedResponseDTO.wrapSuccessResult(floats, FETCH_SUCCESSFUL);
  }

  @Operation(summary = "Get floats by floatRef")
  @GetMapping("/float/{floatRef}")
  public ResponseEntity<?> findFloatByRef(@PathVariable("floatRef") String floatRef) {
    Floats floats = floatService.findByRef(floatRef);
    return ResponseDTO.wrapSuccessResult(floats, FETCH_SUCCESSFUL);
  }

  @PutMapping("/floatOrders/{floatOrderId}")
  @PreAuthorize(
      "hasRole('ROLE_HOD') or hasRole('ROLE_GENERAL_MANAGER') or  hasRole('ROLE_AUDITOR')")
  public ResponseEntity<?> changeState(
      @PathVariable("floatOrderId") int floatOrderId,
      @RequestParam UpdateStatus statusChange,
      Authentication authentication)
      throws Exception {

    switch (statusChange) {
      case APPROVE:
        return approveFloats(floatOrderId, authentication);
      case ENDORSE:
        return endorseFloats(floatOrderId, authentication);
      case CANCEL:
        return cancelFloats(floatOrderId, authentication);
      case RETIRE:
        return approveFloatRetirement(floatOrderId, authentication);
      default:
        return failedResponse("FAILED REQUEST");
    }
  }

  private ResponseEntity<?> approveFloatRetirement(int floatOrderId, Authentication authentication)
      throws Exception {
    if (checkAuthorityExist(authentication, EmployeeRole.ROLE_AUDITOR)) {
      FloatOrder order =
          floatOrderService.approveRetirement(floatOrderId, EmployeeRole.ROLE_AUDITOR);
      if (Objects.nonNull(order)) {
        CompletableFuture.runAsync(
            () -> {
              FloatRetirementListener.FloatRetirementEvent event =
                  new FloatRetirementListener.FloatRetirementEvent(this, order);
              applicationEventPublisher.publishEvent(event);
            });
        return ResponseDTO.wrapSuccessResult(order, "AUDITOR APPROVE FLOATS RETIREMENT SUCCESSFUL");
      }
      return failedResponse("FAILED TO APPROVE RETIREMENT");
    } else if (checkAuthorityExist(authentication, EmployeeRole.ROLE_GENERAL_MANAGER)) {
      FloatOrder order =
          floatOrderService.approveRetirement(floatOrderId, EmployeeRole.ROLE_GENERAL_MANAGER);
      if (Objects.nonNull(order)) {
        CompletableFuture.runAsync(() -> applicationEventPublisher.publishEvent(order));
        return ResponseDTO.wrapSuccessResult(order, "GM APPROVE FLOATS RETIREMENT SUCCESSFUL");
      }
      return failedResponse("FAILED TO APPROVE RETIREMENT");
    }
    return failedResponse("FORBIDDEN ACCESS");
  }

  private ResponseEntity<?> endorseFloats(int floatOrderId, Authentication authentication)
      throws GeneralException {

    if (!checkAuthorityExist(authentication, EmployeeRole.ROLE_HOD))
      return failedResponse("FORBIDDEN ACCESS");
    FloatOrder endorsedOrder = floatOrderService.endorse(floatOrderId, EndorsementStatus.ENDORSED);

    return ResponseDTO.wrapSuccessResult(endorsedOrder, "ENDORSE FLOATS SUCCESSFUL");
  }

  private ResponseEntity<?> approveFloats(int floatOrderId, Authentication authentication) {
    if (!checkAuthorityExist(authentication, EmployeeRole.ROLE_GENERAL_MANAGER))
      return failedResponse("FORBIDDEN ACCESS");
    FloatOrder approvedOrder = floatOrderService.approve(floatOrderId, RequestApproval.APPROVED);

    if (Objects.nonNull(approvedOrder)) {
      return ResponseDTO.wrapSuccessResult(approvedOrder, "APPROVE FLOAT SUCCESSFUL");
    }

    return failedResponse("FAILED TO_ENDORSE");
  }

  private ResponseEntity<?> cancelFloats(int floatOrderId, Authentication authentication) {

    if (checkAuthorityExist(authentication, EmployeeRole.ROLE_HOD)) {
      FloatOrder endorseCancel = floatOrderService.cancel(floatOrderId, EmployeeRole.ROLE_HOD);
      if (Objects.nonNull(endorseCancel)) {
        return ResponseDTO.wrapSuccessResult(endorseCancel, "FLOAT ENDORSEMENT CANCELLED");
      }
    }

    if (checkAuthorityExist(authentication, EmployeeRole.ROLE_GENERAL_MANAGER)) {
      FloatOrder approveCancel = floatOrderService.cancel(floatOrderId, EmployeeRole.ROLE_HOD);
      return ResponseDTO.wrapSuccessResult(approveCancel, "FLOAT APPROVAL CANCELLED");
    }
    return failedResponse("CANCEL REQUEST FAILED");
  }

  @Operation(summary = "Update the float request after comment")
  @PutMapping("floatOrders/{floatOrderId}/comment")
  public ResponseEntity<?> updateFloat(
      @Valid @RequestBody ItemUpdateDTO updateDTO, @PathVariable("floatOrderId") int floatOrderId) {
    try {
      FloatOrder update = floatOrderService.updateFloat(floatOrderId, updateDTO);
      if (Objects.isNull(update)) return failedResponse("UPDATE FAILED");
      return ResponseDTO.wrapSuccessResult(update, "UPDATE FLOAT");
    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("UPDATE FAILED");
  }

  @PutMapping("floatOrders/{floatOrderId}/close")
  @PreAuthorize("hasRole('ROLE_ACCOUNT_OFFICER')")
  public ResponseEntity<?> closeFloat(@PathVariable("floatOrderId") int floatOrderId) {
    try {
      FloatOrder order = floatOrderService.closeRetirement(floatOrderId);
      if (Objects.isNull(order)) return failedResponse("CLOSE FLOAT RETIREMENT_FAILED");
      return ResponseDTO.wrapSuccessResult(order, "CLOSE RETIREMENT SUCCESSFUL");
    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("CLOSE RETIREMENT FAILED");
  }

  @Operation(summary = "Retire float process, upload supporting document of float")
  @PutMapping("floatOrders/{floatOrderId}/supportingDocument")
  public ResponseEntity<?> retireFloat(
      Authentication authentication,
      @PathVariable("floatOrderId") int floatOrderId,
      @RequestBody Set<RequestDocument> documents) {
    try {
      FloatOrder f = floatOrderService.findById(floatOrderId);
      if (f == null) return failedResponse("FLOAT_DOES_NOT_EXIST");
      boolean loginUserCreatedFloat = f.getCreatedBy().getEmail().equals(authentication.getName());
      if (!loginUserCreatedFloat) return failedResponse("USER NOT ALLOWED TO RETIRE FLOAT");
      if (documents.isEmpty()) return failedResponse("DOCUMENT DOES NOT EXIST");
      Set<RequestDocument> requestDocuments =
          documents.stream()
              .map(l -> requestDocumentService.findById(l.getId()))
              .collect(Collectors.toSet());
      FloatOrder order = floatOrderService.uploadSupportingDoc(floatOrderId, requestDocuments);
      if (order == null) return failedResponse("FAILED TO ASSIGN DOCUMENT TO FLOAT");
      CompletableFuture.runAsync(
          () -> {
            FloatRetirementListener.FloatRetirementEvent event =
                new FloatRetirementListener.FloatRetirementEvent(this, order);
            applicationEventPublisher.publishEvent(event);
          });

      return ResponseDTO.wrapSuccessResult(order, "SUPPORTING DOCUMENT ASSIGNED TO FLOAT");
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("FLOAT RETIREMENT FAILED");
  }

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
        return PagedResponseDTO.wrapSuccessResult(floatOrders, FETCH_SUCCESSFUL);
      }
      if (myRequest.isPresent()) {
        Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
        Page<FloatOrder> orders =
            floatOrderService.getAllEmployeeFloatOrder(pageNo, pageSize, employee);
        return PagedResponseDTO.wrapSuccessResult(orders, FETCH_SUCCESSFUL);
      }
      if (checkAuthorityExist(authentication, EmployeeRole.ROLE_ADMIN)) {
        Page<FloatOrder> allFloatOrdersAdmin =
            floatOrderService.findAllFloatOrder(pageNo, pageSize);
        return PagedResponseDTO.wrapSuccessResult(allFloatOrdersAdmin, FETCH_SUCCESSFUL);
      }
    } catch (Exception e) {
      log.error(e.toString());
    }
    return notFound("FLOAT ORDERS NOT FOUND");
  }

  @PutMapping("/floatOrders/{floatOrderId}/addItems")
  public ResponseEntity<?> addFloatItems(
      Authentication authentication,
      @PathVariable("floatOrderId") int floatOrderId,
      @Valid @RequestBody BulkFloatsDTO floatsDTO) {
    try {
      Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
      FloatOrder floatOrder = floatOrderService.findById(floatOrderId);
      FloatOrder updatedOrder =
          Optional.ofNullable(floatOrder)
              .filter(i -> i.getCreatedBy().getId().equals(employee.getId()))
              .map(o -> floatOrderService.addFloatsToOrder(floatOrderId, floatsDTO.getFloats()))
              .orElse(null);
      if (Objects.isNull(updatedOrder)) return failedResponse("ADD FLOAT BREAKDOWN FAILED");
      return ResponseDTO.wrapSuccessResult(updatedOrder, "FLOAT ITEMS ADDED TO FLOAT ORDER");
    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("ADD FLOAT ITEMS FAILED");
  }

  private Boolean checkAuthorityExist(Authentication authentication, EmployeeRole role) {
    return authentication.getAuthorities().stream()
        .map(a -> a.getAuthority().equalsIgnoreCase(role.name()))
        .filter(r -> r.booleanValue())
        .findAny()
        .isPresent();
  }
}
