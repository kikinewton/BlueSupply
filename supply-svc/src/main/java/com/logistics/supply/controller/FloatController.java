package com.logistics.supply.controller;

import com.logistics.supply.dto.BulkFloatsDTO;
import com.logistics.supply.dto.ItemUpdateDto;
import com.logistics.supply.dto.PagedResponseDto;
import com.logistics.supply.dto.ResponseDto;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.enums.UpdateStatus;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.exception.UnauthorizedUpdateException;
import com.logistics.supply.model.*;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.FloatOrderService;
import com.logistics.supply.service.FloatService;
import com.logistics.supply.util.Helper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.logistics.supply.util.Constants.FETCH_SUCCESSFUL;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FloatController {

  private final FloatService floatService;
  private final FloatOrderService floatOrderService;
  private final EmployeeService employeeService;

  @Operation(summary = "Tag float when the requester receives money from accounts", tags = "FLOATS")
  @PutMapping("/floatOrders/{floatOrderId}/receiveFunds")
  public ResponseEntity<ResponseDto<FloatOrder>> setAllocateFundsToFloat(
      @PathVariable("floatOrderId") int floatOrderId, Authentication authentication) {

    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    FloatOrder allocatedOrder = floatOrderService.setFundsAllocatedOnFloatOrder(floatOrderId, employee);
    return ResponseDto.wrapSuccessResult(allocatedOrder, "FUNDS ALLOCATED TO FLOATS SUCCESSFULLY");
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
      @RequestParam(required = false) Optional<Boolean> pendingStoreManagerApproval,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize,
      Authentication authentication) {

    try {
      if (approval.isPresent()) return floatByApprovalStatus(approval.get(), pageNo, pageSize);
      if (auditorRetire.isPresent()) return floatsRetiredStatus(authentication, pageNo, pageSize);
      if (gmRetire.isPresent()) return floatsRetiredStatus(authentication, pageNo, pageSize);
      Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
      Department department = employee.getDepartment();
      if (awaitingDocument.isPresent()) {
        int employeeId = employee.getId();
        return floatAwaitingDocument(pageNo, pageSize, employeeId);
      }

      if (endorsement.isPresent())
        return floatByEndorsementStatus(endorsement.get(), pageNo, pageSize);
      if (awaitingFunds.isPresent()) return floatsAwaitingFunds(pageNo, pageSize);
      if (closeRetirement.isPresent()) return floatsToCloseRetirement(pageNo, pageSize);
      if (awaitingGRN.isPresent()
          && (checkAuthorityExist(authentication, EmployeeRole.ROLE_STORE_OFFICER)
              || checkAuthorityExist(authentication, EmployeeRole.ROLE_STORE_MANAGER))) {
        return floatsByPendingGRN(pageNo, pageSize, department.getId());
      }
      if (pendingStoreManagerApproval.isPresent()
          && pendingStoreManagerApproval.get()
          && checkAuthorityExist(authentication, EmployeeRole.ROLE_STORE_MANAGER)) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());

        Page<FloatOrder> pendingGrnApprovalFromStoreManager =
            floatOrderService.findPendingGrnApprovalFromStoreManager(pageable, department.getId());
        return PagedResponseDto.wrapSuccessResult(
            pendingGrnApprovalFromStoreManager, FETCH_SUCCESSFUL);
      }
      if (receivedFundsAndNotRetired.isPresent())
        return floatsReceivedFundNotRetired(pageNo, pageSize);
      else {

        Page<FloatOrder> floats = floatOrderService.findAllFloatOrder(pageNo, pageSize);
        if (floats != null) {
          return PagedResponseDto.wrapSuccessResult(floats, FETCH_SUCCESSFUL);
        }
        return Helper.notFound("NO FLOAT FOUND");
      }

    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return Helper.notFound("NO FLOAT FOUND");
  }

  private ResponseEntity<?> floatsReceivedFundNotRetired(int pageNo, int pageSize) {

    Page<FloatOrder> floats = floatOrderService.getFloatOrderWithReceivedFundsAndNotRetired(pageNo, pageSize);
    return PagedResponseDto.wrapSuccessResult(floats, FETCH_SUCCESSFUL);
  }

  private ResponseEntity<?> floatsToCloseRetirement(int pageNo, int pageSize) {
    Page<FloatOrder> floatOrders = floatOrderService.findFloatOrderToClose(pageNo, pageSize);
    return PagedResponseDto.wrapSuccessResult(floatOrders, FETCH_SUCCESSFUL);
  }

  private ResponseEntity<?> floatByRStatus(RequestStatus status, int pageNo, int pageSize) {
    Page<FloatOrder> floats = floatOrderService.findFloatOrderByRequestStatus(pageNo, pageSize, status);
    return PagedResponseDto.wrapSuccessResult(floats, FETCH_SUCCESSFUL);
  }

  private ResponseEntity<?> floatsAwaitingFunds(int pageNo, int pageSize) {
    Page<FloatOrder> floats = floatOrderService.findFloatsAwaitingFunds(pageNo, pageSize);
    return PagedResponseDto.wrapSuccessResult(floats, FETCH_SUCCESSFUL);
  }

  private ResponseEntity<?> floatByEndorsementStatus(
      EndorsementStatus endorsement, int pageNo, int pageSize) {
    Page<FloatOrder> floats =
        floatOrderService.findFloatOrderByEndorseStatus(pageNo, pageSize, endorsement);
    return PagedResponseDto.wrapSuccessResult(floats, FETCH_SUCCESSFUL);
  }

  private ResponseEntity<?> floatAwaitingDocument(int pageNo, int pageSize, int employeeId)
      throws GeneralException {
    Page<FloatOrder> floats =
        floatOrderService.findFloatsAwaitingDocument(pageNo, pageSize, employeeId);
    return PagedResponseDto.wrapSuccessResult(floats, FETCH_SUCCESSFUL);
  }

  private ResponseEntity<?> floatsRetiredStatus(
      Authentication authentication, int pageNo, int pageSize) {
    if (checkAuthorityExist(authentication, EmployeeRole.ROLE_AUDITOR)) {
      Page<FloatOrder> floats = floatOrderService.floatOrderForAuditorRetirementApproval(pageNo, pageSize);
      return PagedResponseDto.wrapSuccessResult(floats, FETCH_SUCCESSFUL);

    } else if (checkAuthorityExist(authentication, EmployeeRole.ROLE_GENERAL_MANAGER)) {
      Page<FloatOrder> floats = floatOrderService.floatOrdersForGmRetirementApproval(pageNo, pageSize);
      return PagedResponseDto.wrapSuccessResult(floats, FETCH_SUCCESSFUL);
    }
    return Helper.notFound("NO FLOAT FOUND");
  }

  private ResponseEntity<?> floatByApprovalStatus(
      RequestApproval approval, int pageNo, int pageSize) {
    Page<FloatOrder> floats = floatOrderService.findByApprovalStatus(pageNo, pageSize, approval);
    return PagedResponseDto.wrapSuccessResult(floats, FETCH_SUCCESSFUL);
  }

  private ResponseEntity<?> floatsByPendingGRN(int pageNo, int pageSize, int departmentId) {
    Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
    Page<FloatOrder> floats = floatOrderService.findFloatsAwaitingGRN(pageable, departmentId);
    return PagedResponseDto.wrapSuccessResult(floats, FETCH_SUCCESSFUL);
  }

  @GetMapping("floatOrders/{floatOrderId}")
  public ResponseEntity<ResponseDto<FloatOrder>> findByFloatOrderId(@PathVariable("floatOrderId") int floatOrderId) {

    FloatOrder order = floatOrderService.findById(floatOrderId);
    return ResponseDto.wrapSuccessResult(order, FETCH_SUCCESSFUL);
  }

  @Operation(summary = "Get floats requested by employee")
  @GetMapping("/floatsForEmployee")
  public ResponseEntity<PagedResponseDto<Page<FloatOrder>>> findByEmployee(
          Authentication authentication,
          Pageable pageable) {

    int employeeId = employeeService.findEmployeeByEmail(authentication.getName()).getId();
    Page<FloatOrder> floats = floatOrderService.findByEmployee(employeeId, pageable);
    return PagedResponseDto.wrapSuccessResult(floats, FETCH_SUCCESSFUL);
  }

  @GetMapping("/floatsForDepartment")
  @PreAuthorize("hasRole('ROLE_HOD')")
  public ResponseEntity<PagedResponseDto<Page<FloatOrder>>> findByDepartment(
          Authentication authentication,
          Pageable pageable) {

    Department department = employeeService.findEmployeeByEmail(authentication.getName()).getDepartment();
    Page<FloatOrder> floats = floatOrderService.findPendingByDepartment(department, pageable);
    return PagedResponseDto.wrapSuccessResult(floats, FETCH_SUCCESSFUL);
  }

  @Operation(summary = "Get floats by floatRef")
  @GetMapping("/float/{floatRef}")
  public ResponseEntity<ResponseDto<Floats>> findFloatByRef(@PathVariable("floatRef") String floatRef) {

    Floats floats = floatService.findByRef(floatRef);
    return ResponseDto.wrapSuccessResult(floats, FETCH_SUCCESSFUL);
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
        return Helper.failedResponse("FAILED REQUEST");
    }
  }

  private ResponseEntity<?> approveFloatRetirement(int floatOrderId, Authentication authentication)
      throws Exception {
    if (checkAuthorityExist(authentication, EmployeeRole.ROLE_AUDITOR)) {
      FloatOrder order =
          floatOrderService.approveRetirementByAuditor(floatOrderId);
      if (Objects.nonNull(order)) {

        return ResponseDto.wrapSuccessResult(order, "AUDITOR APPROVE FLOATS RETIREMENT SUCCESSFUL");
      }
      return Helper.failedResponse("FAILED TO APPROVE RETIREMENT");
    } else if (checkAuthorityExist(authentication, EmployeeRole.ROLE_GENERAL_MANAGER)) {
      FloatOrder order = floatOrderService.approveRetirementByGeneralManager(floatOrderId);
      if (Objects.nonNull(order)) {

        return ResponseDto.wrapSuccessResult(order, "GM APPROVE FLOATS RETIREMENT SUCCESSFUL");
      }
      return Helper.failedResponse("FAILED TO APPROVE RETIREMENT");
    }
    return Helper.failedResponse("FORBIDDEN ACCESS");
  }

  @PreAuthorize("hasRole('ROLE_HOD')")
  private ResponseEntity<?> endorseFloats(int floatOrderId, Authentication authentication) {

    int employeeId = employeeService.findEmployeeByEmail(authentication.getName()).getId();
    FloatOrder endorsedOrder =
        floatOrderService.endorse(floatOrderId, EndorsementStatus.ENDORSED, employeeId);
    return ResponseDto.wrapSuccessResult(endorsedOrder, "ENDORSE FLOATS SUCCESSFUL");
  }

  @PreAuthorize("hasRole('ROLE_GENERAL_MANAGER')")
  private ResponseEntity<?> approveFloats(int floatOrderId, Authentication authentication) {

    Integer employeeId = employeeService.findEmployeeByEmail(authentication.getName()).getId();
    FloatOrder approvedOrder =
        floatOrderService.approve(floatOrderId, RequestApproval.APPROVED, employeeId);
    return ResponseDto.wrapSuccessResult(approvedOrder, "APPROVE FLOAT SUCCESSFUL");
  }

  private ResponseEntity<?> cancelFloats(int floatOrderId, Authentication authentication) {

    if (checkAuthorityExist(authentication, EmployeeRole.ROLE_HOD)) {
      FloatOrder endorseCancel = floatOrderService.cancel(floatOrderId, EmployeeRole.ROLE_HOD);
      if (Objects.nonNull(endorseCancel)) {
        return ResponseDto.wrapSuccessResult(endorseCancel, "FLOAT ENDORSEMENT CANCELLED");
      }
    }

    if (checkAuthorityExist(authentication, EmployeeRole.ROLE_GENERAL_MANAGER)) {
      FloatOrder approveCancel = floatOrderService.cancel(floatOrderId, EmployeeRole.ROLE_HOD);
      return ResponseDto.wrapSuccessResult(approveCancel, "FLOAT APPROVAL CANCELLED");
    }
    return Helper.failedResponse("CANCEL REQUEST FAILED");
  }

  @Operation(summary = "Update the float request after comment")
  @PutMapping("floatOrders/{floatOrderId}/comment")
  public ResponseEntity<ResponseDto<FloatOrder>> updateFloat(
          @Valid @RequestBody ItemUpdateDto updateDTO, @PathVariable("floatOrderId") int floatOrderId) {

      FloatOrder update = floatOrderService.updateFloat(floatOrderId, updateDTO);
      return ResponseDto.wrapSuccessResult(update, "UPDATED FLOAT");
  }

  @PutMapping("floatOrders/{floatOrderId}/close")
  @PreAuthorize("hasRole('ROLE_ACCOUNT_OFFICER')")
  public ResponseEntity<ResponseDto<FloatOrder>> closeFloat(@PathVariable("floatOrderId") int floatOrderId) {

      FloatOrder order = floatOrderService.closeRetirement(floatOrderId);
      return ResponseDto.wrapSuccessResult(order, "CLOSE RETIREMENT SUCCESSFUL");
  }

  @Operation(summary = "Retire float process, upload supporting document of float")
  @PutMapping("floatOrders/{floatOrderId}/supportingDocument")
  public ResponseEntity<ResponseDto<FloatOrder>> retireFloat(
      Authentication authentication,
      @PathVariable("floatOrderId") int floatOrderId,
      @RequestBody @Size(min = 1) Set<RequestDocument> documents) {

      FloatOrder floatOrder = floatOrderService.retireFloat(
              floatOrderId,
              authentication.getName(),
              documents);

      return ResponseDto.wrapSuccessResult(floatOrder, "SUPPORTING DOCUMENT ASSIGNED TO FLOAT");

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
        Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
        List<FloatOrder> floatOrdersRequiringGRN =
            floatOrderService.findFloatOrdersRequiringGRN(employee.getDepartment());
        return ResponseDto.wrapSuccessResult(floatOrdersRequiringGRN, FETCH_SUCCESSFUL);
      }
      if (myRequest.isPresent()) {
        Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
        Page<FloatOrder> orders =
            floatOrderService.getEmployeeFloatOrders(pageNo, pageSize, employee);
        return PagedResponseDto.wrapSuccessResult(orders, FETCH_SUCCESSFUL);
      }
      if (checkAuthorityExist(authentication, EmployeeRole.ROLE_ADMIN)) {
        Page<FloatOrder> allFloatOrdersAdmin =
            floatOrderService.findAllFloatOrder(pageNo, pageSize);
        return PagedResponseDto.wrapSuccessResult(allFloatOrdersAdmin, FETCH_SUCCESSFUL);
      }
    } catch (Exception e) {
      log.error(e.toString());
    }
    return Helper.notFound("FLOAT ORDERS NOT FOUND");
  }

  @PutMapping("/floatOrders/{floatOrderId}/addItems")
  public ResponseEntity<ResponseDto<FloatOrder>> addFloatItems(
      Authentication authentication,
      @PathVariable("floatOrderId") int floatOrderId,
      @Valid @RequestBody BulkFloatsDTO floatsDTO) {

      Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
      FloatOrder floatOrder = floatOrderService.findById(floatOrderId);

      Employee createdBy = floatOrder.getCreatedBy();
      if (!createdBy.equals(employee)) {
        throw new UnauthorizedUpdateException("You are not authorized to update this record.");
      }

      FloatOrder updatedOrder = floatOrderService.addBulkFloatsToFloatOrder(floatOrderId, floatsDTO.getFloats());
      return ResponseDto.wrapSuccessResult(updatedOrder, "FLOAT ITEMS ADDED TO FLOAT ORDER");

  }

  private Boolean checkAuthorityExist(Authentication authentication, EmployeeRole role) {
    return authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equalsIgnoreCase(role.name()));
  }
}
