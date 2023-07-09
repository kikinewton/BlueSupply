package com.logistics.supply.controller;

import com.logistics.supply.dto.BulkPettyCashDTO;
import com.logistics.supply.dto.ItemUpdateDto;
import com.logistics.supply.dto.PagedResponseDto;
import com.logistics.supply.dto.ResponseDto;
import com.logistics.supply.enums.UpdateStatus;
import com.logistics.supply.model.*;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.PettyCashService;
import com.logistics.supply.util.Constants;
import com.logistics.supply.util.Helper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PettyCashController {

  private final PettyCashService pettyCashService;
  private final EmployeeService employeeService;

  @GetMapping("/pettyCashOrders")
  public ResponseEntity<PagedResponseDto<Page<PettyCashOrder>>> findAllPettyCashOrder(
      @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
      @RequestParam(value = "pageSize", defaultValue = "200") int pageSize) {

    Page<PettyCashOrder> allPettyCashOrder =
        pettyCashService.findAllPettyCashOrder(pageNo, pageSize);
    return PagedResponseDto.wrapSuccessResult(allPettyCashOrder, Constants.FETCH_SUCCESSFUL);
  }

  @Operation(
      summary = "Tag petty cash when the requester receives money from accounts",
      tags = "PETTY CASH")
  @PutMapping("/bulkPettyCash/receiveFunds")
  public ResponseEntity<ResponseDto<List<PettyCash>>> setAllocateFundsToPettyCash(
          @Valid @RequestBody BulkPettyCashDTO bulkPettyCash, Authentication authentication) {

      List<PettyCash> updatedPettyCash = pettyCashService.allocateFunds(bulkPettyCash, authentication.getName());
      return ResponseDto.wrapSuccessResult(updatedPettyCash, "FUNDS ALLOCATED FOR PETTY CASH SUCCESSFULLY" );
    }

  @GetMapping("/pettyCash")
  public ResponseEntity<?> findAllPettyCash(
      @RequestParam(required = false, defaultValue = "false") Boolean approved,
      @RequestParam(required = false, defaultValue = "false") Boolean endorsed,
      @RequestParam(required = false, defaultValue = "false") Boolean unpaid,
      @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
      @RequestParam(value = "pageSize", defaultValue = "200") int pageSize) {
    if (approved) {
      List<PettyCash> approvedList = pettyCashService.findPettyCashPendingPayment();
      return ResponseDto.wrapSuccessResult(
          approvedList, "FETCH APPROVED PETTY CASH PENDING PAYMENT");
    } else if (endorsed) {
      List<PettyCash> endorsedList = pettyCashService.findEndorsedPettyCash();
      return ResponseDto.wrapSuccessResult(endorsedList, "FETCH ENDORSED PETTY CASH");
    } else if (unpaid) {
      List<PettyCash> cashListPendingPayment = pettyCashService.findPettyCashPendingPayment();
      return ResponseDto.wrapSuccessResult(
          cashListPendingPayment, "FETCH PETTY CASH PENDING PAYMENT");
    } else {
      Page<PettyCash> allPettyCashPage = pettyCashService.findAllPettyCashPage(pageNo, pageSize);
      return PagedResponseDto.wrapSuccessResult(allPettyCashPage, Constants.FETCH_SUCCESSFUL);
    }
  }

  @PutMapping("/pettyCash/{pettyCashId}")
  public ResponseEntity<ResponseDto<PettyCash>> updatePettyCash(
      @PathVariable("pettyCashId") int pettyCashId, @Valid @RequestBody ItemUpdateDto itemUpdate) {

      PettyCash pettyCash = pettyCashService.updatePettyCash(pettyCashId, itemUpdate);
      return ResponseDto.wrapSuccessResult(pettyCash, "UPDATE PETTY CASH SUCCESSFUL");
  }

  @GetMapping("/pettyCashByDepartment")
  @PreAuthorize("hasRole('ROLE_HOD')")
  public ResponseEntity<PagedResponseDto<Page<PettyCash>>> findAllPettyCashByDepartment(
      Authentication authentication,
  @RequestParam(defaultValue = "0") int pageNo,
  @RequestParam(defaultValue = "300") int pageSize) {

    Page<PettyCash> cashList = pettyCashService.findByDepartment(pageNo, pageSize, authentication.getName());
    return PagedResponseDto.wrapSuccessResult(cashList, Constants.FETCH_SUCCESSFUL);
  }

  @GetMapping("/pettyCashForEmployee")
  public ResponseEntity<PagedResponseDto<Page<PettyCash>>> findPettyCashForEmployee(
      Authentication authentication,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "300") int pageSize) {

    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    Page<PettyCash> pettyCashList =
        pettyCashService.findByEmployee(employee.getId(), pageNo, pageSize);
    return PagedResponseDto.wrapSuccessResult(pettyCashList, Constants.FETCH_SUCCESSFUL);
  }
  @PutMapping("/bulkPettyCash/endorse")
  @PreAuthorize("hasRole('ROLE_HOD')")
  public ResponseEntity<ResponseDto<Set<PettyCash>>> endorsePettyCash(
           @RequestBody Set<PettyCash> bulkPettyCash) {

    Set<PettyCash> pettyCashList = pettyCashService.bulkEndorse(bulkPettyCash);
    return ResponseDto.wrapSuccessResult(pettyCashList, "PETTY CASH ENDORSED");
  }

  @PutMapping("/bulkPettyCash/approve")
  @PreAuthorize("hasRole('ROLE_GENERAL_MANAGER')")
  public ResponseEntity<ResponseDto<Set<PettyCash>>> approvePettyCash(
          @RequestBody Set<PettyCash> bulkPettyCash) {

    Set<PettyCash> pettyCashList = pettyCashService.bulkApproval(bulkPettyCash);
    return ResponseDto.wrapSuccessResult(pettyCashList, "FLOAT APPROVED");
  }

  @PutMapping("/bulkPettyCash/{statusChange}")
  @PreAuthorize("hasRole('ROLE_HOD') or hasRole('ROLE_GENERAL_MANAGER')")
  public ResponseEntity<?> changeState(
      @Valid @RequestBody Set<PettyCash> bulkPettyCash,
      @PathVariable("statusChange") UpdateStatus statusChange,
      Authentication authentication) {

    switch (statusChange) {
      case APPROVE:
        return approvePettyCash(bulkPettyCash, authentication);
      case ENDORSE:
        return endorsePettyCash(bulkPettyCash, authentication);
      case CANCEL:
        return cancelPettyCash(bulkPettyCash, authentication);
      default:
        return Helper.failedResponse("FAILED REQUEST");
    }
  }

  private ResponseEntity<?> cancelPettyCash(
      Set<PettyCash> bulkPettyCash, Authentication authentication) {
    if (Objects.isNull(authentication)) return Helper.failedResponse("Auth token is required");

    if (checkAuthorityExist(authentication, EmployeeRole.ROLE_HOD)) {
      Set<PettyCash> pettyCashes =
          bulkPettyCash.stream()
              .map(x -> pettyCashService.cancelByHod(x.getId()))
              .filter(Objects::nonNull)
              .collect(Collectors.toSet());
      if (!pettyCashes.isEmpty()) {
        ResponseDto response =
            new ResponseDto("PETTY CASH ENDORSEMENT CANCELLED", Constants.SUCCESS, pettyCashes);
        return ResponseEntity.ok(response);
      }
    }

    if (checkAuthorityExist(authentication, EmployeeRole.ROLE_GENERAL_MANAGER)) {
      Set<PettyCash> pettyCashSet =
          bulkPettyCash.stream()
              .map(x -> pettyCashService.cancelByGeneralManager(x.getId()))
              .filter(Objects::nonNull)
              .collect(Collectors.toSet());
      if (!pettyCashSet.isEmpty()) {
        ResponseDto response =
            new ResponseDto("PETTY CASH APPROVAL CANCELLED", Constants.SUCCESS, pettyCashSet);
        return ResponseEntity.ok(response);
      }
    }

    return Helper.failedResponse("CANCEL REQUEST FAILED");
  }

  private ResponseEntity<?> endorsePettyCash(
      Set<PettyCash> bulkPettyCash, Authentication authentication) {

    if (Objects.isNull(authentication)) return Helper.failedResponse("Auth token is required");
    if (!checkAuthorityExist(authentication, EmployeeRole.ROLE_HOD))
      return Helper.failedResponse("FORBIDDEN ACCESS");
    Set<PettyCash> pettyCash =
        bulkPettyCash.stream()
            .map(x -> pettyCashService.endorse(x.getId()))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    if (!pettyCash.isEmpty()) {
      ResponseDto response = new ResponseDto("ENDORSE PETTY CASH SUCCESSFUL", Constants.SUCCESS, pettyCash);
      return ResponseEntity.ok(response);
    }

    return Helper.failedResponse("FAILED TO ENDORSE");
  }

  private ResponseEntity<?> approvePettyCash(
      Set<PettyCash> bulkPettyCash, Authentication authentication) {
    if (Objects.isNull(authentication)) return Helper.failedResponse("Auth token is required");
    if (!checkAuthorityExist(authentication, EmployeeRole.ROLE_GENERAL_MANAGER))
      return Helper.failedResponse("FORBIDDEN ACCESS");
    Set<PettyCash> pettyCash =
        bulkPettyCash.stream()
            .map(x -> pettyCashService.approve(x.getId()))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    if (!pettyCash.isEmpty()) {
      return ResponseDto.wrapSuccessResult(pettyCash, "APPROVE FLOAT SUCCESSFUL");
    }
    return Helper.failedResponse("FAILED TO APPROVE");
  }

  private Boolean checkAuthorityExist(Authentication authentication, EmployeeRole role) {
    return authentication.getAuthorities().stream()
        .map(a -> a.getAuthority().equalsIgnoreCase(role.name()))
        .findAny()
        .isPresent();
  }
}
