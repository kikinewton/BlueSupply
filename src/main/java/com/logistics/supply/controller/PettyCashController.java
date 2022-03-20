package com.logistics.supply.controller;

import com.logistics.supply.dto.BulkPettyCashDTO;
import com.logistics.supply.dto.ItemUpdateDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.enums.UpdateStatus;
import com.logistics.supply.event.listener.FundsReceivedPettyCashListener;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.PettyCash;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.PettyCashService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;
import static com.logistics.supply.util.Helper.failedResponse;
import static com.logistics.supply.util.Helper.notFound;

@Slf4j
@RestController
@RequestMapping("/api")
public class PettyCashController {
  @Autowired PettyCashService pettyCashService;
  @Autowired EmployeeService employeeService;
  @Autowired ApplicationEventPublisher applicationEventPublisher;

  @PostMapping("/pettyCash")
  public ResponseEntity<?> createPettyCash(@Valid @RequestBody PettyCash pettyCash) {
    try {
      PettyCash cash = pettyCashService.save(pettyCash);
      if (Objects.nonNull(cash)) {
        ResponseDTO successResponse = new ResponseDTO("PETTY CASH CREATED", SUCCESS, cash);
        return ResponseEntity.ok(successResponse);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    ResponseDTO failed = new ResponseDTO("REQUEST FAILED", ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }

  @Operation(
      summary = "Tag petty cash when the requester receives money from accounts",
      tags = "PETTY CASH")
  @PutMapping("/pettyCash/receiveFunds")
  public ResponseEntity<?> setAllocateFundsToPettyCash(
      @Valid @RequestBody BulkPettyCashDTO pettyCash, Authentication authentication) {
    try {
      List<Integer> ids = new ArrayList<>();
      pettyCash.getPettyCash().stream()
          .forEach(
              p -> {
                if (p.getApproval().equals(RequestApproval.APPROVED)) ids.add(p.getId());
              });

      List<PettyCash> pettyCashList =
          pettyCashService.findAllById(ids).stream()
              .map(
                  p -> {
                    p.setStatus(RequestStatus.PROCESSED);
                    p.setPaid(true);
                    return p;
                  })
              .collect(Collectors.toList());

      List<PettyCash> updatedPettyCash = pettyCashService.saveAll(pettyCashList);

      if (updatedPettyCash.isEmpty()) return notFound("FUNDS ALLOCATION FAILED");
      ResponseDTO response =
          new ResponseDTO("FUNDS ALLOCATED FOR PETTY CASH SUCCESSFULLY", SUCCESS, updatedPettyCash);

      CompletableFuture.runAsync(
          () -> {
            Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
            FundsReceivedPettyCashListener.FundsReceivedPettyCashEvent fundsReceivedPettyCashEvent =
                new FundsReceivedPettyCashListener.FundsReceivedPettyCashEvent(
                    this, employee, updatedPettyCash);
            applicationEventPublisher.publishEvent(fundsReceivedPettyCashEvent);
          });

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("ALLOCATE FUNDS FOR PETTY CASH FAILED");
  }

  @GetMapping("/pettyCash")
  public ResponseEntity<?> findAllPettyCash(
      @RequestParam(required = false, defaultValue = "false") Boolean approved,
      @RequestParam(required = false, defaultValue = "false") Boolean endorsed,
      @RequestParam(required = false, defaultValue = "false") Boolean unpaid,
      @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
      @RequestParam(value = "pageSize", defaultValue = "200") int pageSize) {
    if (approved) {
      try {
        List<PettyCash> approvedList = pettyCashService.findPettyCashPendingPayment();
        ResponseDTO successResponse =
            new ResponseDTO("FETCH APPROVED PETTY CASH PENDING PAYMENT", SUCCESS, approvedList);
        return ResponseEntity.ok(successResponse);
      } catch (Exception e) {
        log.error(e.toString());
      }

    } else if (endorsed) {
      try {
        List<PettyCash> endorsedList = pettyCashService.findEndorsedPettyCash();
        ResponseDTO successResponse =
            new ResponseDTO("FETCH ENDORSED PETTY CASH", SUCCESS, endorsedList);
        return ResponseEntity.ok(successResponse);
      } catch (Exception e) {
        log.error(e.toString());
      }
    } else if (unpaid) {
      List<PettyCash> cashListPendingPayment = pettyCashService.findPettyCashPendingPayment();
      ResponseDTO successResponse =
          new ResponseDTO("FETCH PETTY CASH PENDING PAYMENT", SUCCESS, cashListPendingPayment);
      return ResponseEntity.ok(successResponse);
    } else {
      try {
        List<PettyCash> cashList = pettyCashService.findAllPettyCash(pageNo, pageSize);
        ResponseDTO successResponse = new ResponseDTO("FETCH PETTY CASH", SUCCESS, cashList);
        return ResponseEntity.ok(successResponse);
      } catch (Exception e) {
        log.error(e.toString());
      }
    }

    ResponseDTO failed = new ResponseDTO("FETCH FAILED", ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }

  @PutMapping("/pettyCash/{pettyCashId}")
  public ResponseEntity<?> updatePettyCash(
      @PathVariable("pettyCashId") int pettyCashId, @Valid @RequestBody ItemUpdateDTO itemUpdate) {
    try {
      PettyCash pettyCash = pettyCashService.updatePettyCash(pettyCashId, itemUpdate);
      if (Objects.isNull(pettyCash)) failedResponse("PETTY CASH UPDATE FAILED");
      ResponseDTO response = new ResponseDTO("UPDATE PETTY CASH SUCCESSFUL", SUCCESS, pettyCash);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("UPDATE PETTY CASH FAILED");
  }

  @GetMapping("/pettyCashByDepartment")
  @PreAuthorize("hasRole('ROLE_HOD')")
  public ResponseEntity<?> findAllPettyCashByDepartment(
      Authentication authentication,
      @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
      @RequestParam(value = "pageSize", defaultValue = "100") int pageSize) {
    if (Objects.isNull(authentication)) return failedResponse("Auth token is required");
    try {
      if (Objects.isNull(authentication)) return failedResponse("Auth token is required");
      Department department =
          employeeService.findEmployeeByEmail(authentication.getName()).getDepartment();
      List<PettyCash> cashList = pettyCashService.findByDepartment(department);
      ResponseDTO successResponse = new ResponseDTO("FETCH PETTY CASH", SUCCESS, cashList);
      return ResponseEntity.ok(successResponse);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    ResponseDTO failed = new ResponseDTO("FETCH FAILED", ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }

  @GetMapping("/pettyCashForEmployee")
  public ResponseEntity<?> findPettyCashForEmployee(
      Authentication authentication,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "100") int pageSize) {
    if (Objects.isNull(authentication)) return failedResponse("Auth token is required");
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    try {
      List<PettyCash> pettyCashList =
          pettyCashService.findByEmployee(employee.getId(), pageNo, pageSize);
      ResponseDTO success = new ResponseDTO("FETCH EMPLOYEE PETTY CASH", SUCCESS, pettyCashList);
      return ResponseEntity.ok(success);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    ResponseDTO failed = new ResponseDTO("FETCH FAILED", ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }

  @PutMapping("/bulkPettyCash/{statusChange}")
  @PreAuthorize("hasRole('ROLE_HOD') or hasRole('ROLE_GENERAL_MANAGER')")
  public ResponseEntity<?> changeState(
      @Valid @RequestBody Set<PettyCash> bulkPettyCash,
      @PathVariable("statusChange") UpdateStatus statusChange,
      Authentication authentication) {
    if (Objects.isNull(authentication)) return failedResponse("Auth token is required");
    switch (statusChange) {
      case APPROVE:
        return approvePettyCash(bulkPettyCash, authentication);
      case ENDORSE:
        return endorsePettyCash(bulkPettyCash, authentication);
      case CANCEL:
        return cancelPettyCash(bulkPettyCash, authentication);
      default:
        return failedResponse("FAILED REQUEST");
    }
  }

  private ResponseEntity<?> cancelPettyCash(
      Set<PettyCash> bulkPettyCash, Authentication authentication) {
    if (Objects.isNull(authentication)) return failedResponse("Auth token is required");
    if (checkAuthorityExist(authentication, EmployeeRole.ROLE_HOD)) {
      Set<PettyCash> pettyCashes =
          bulkPettyCash.stream()
              .map(x -> pettyCashService.endorse(x.getId(), EndorsementStatus.REJECTED))
              .filter(Objects::nonNull)
              .collect(Collectors.toSet());
      if (!pettyCashes.isEmpty()) {
        ResponseDTO response =
            new ResponseDTO("PETTY CASH ENDORSEMENT CANCELLED", SUCCESS, pettyCashes);
        return ResponseEntity.ok(response);
      }
    }

    if (checkAuthorityExist(authentication, EmployeeRole.ROLE_GENERAL_MANAGER)) {
      Set<PettyCash> pettyCashSet =
          bulkPettyCash.stream()
              .map(x -> pettyCashService.approve(x.getId(), RequestApproval.REJECTED))
              .filter(Objects::nonNull)
              .collect(Collectors.toSet());
      if (!pettyCashSet.isEmpty()) {
        ResponseDTO response =
            new ResponseDTO("PETTY CASH APPROVAL CANCELLED", SUCCESS, pettyCashSet);
        return ResponseEntity.ok(response);
      }
    }

    return failedResponse("CANCEL REQUEST FAILED");
  }

  private ResponseEntity<?> endorsePettyCash(
      Set<PettyCash> bulkPettyCash, Authentication authentication) {
    if (Objects.isNull(authentication)) return failedResponse("Auth token is required");
    if (!checkAuthorityExist(authentication, EmployeeRole.ROLE_HOD))
      return failedResponse("FORBIDDEN ACCESS");
    Set<PettyCash> pettyCash =
        bulkPettyCash.stream()
            .map(x -> pettyCashService.endorse(x.getId(), EndorsementStatus.ENDORSED))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    if (!pettyCash.isEmpty()) {
      ResponseDTO response = new ResponseDTO("ENDORSE PETTY CASH SUCCESSFUL", SUCCESS, pettyCash);
      return ResponseEntity.ok(response);
    }

    return failedResponse("FAILED TO ENDORSE");
  }

  private ResponseEntity<?> approvePettyCash(
      Set<PettyCash> bulkPettyCash, Authentication authentication) {
    if (Objects.isNull(authentication)) return failedResponse("Auth token is required");
    if (!checkAuthorityExist(authentication, EmployeeRole.ROLE_GENERAL_MANAGER))
      return failedResponse("FORBIDDEN ACCESS");
    Set<PettyCash> pettyCash =
        bulkPettyCash.stream()
            .map(x -> pettyCashService.approve(x.getId(), RequestApproval.APPROVED))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    if (!pettyCash.isEmpty()) {
      ResponseDTO response = new ResponseDTO("APPROVE FLOAT SUCCESSFUL", SUCCESS, pettyCash);
      return ResponseEntity.ok(response);
    }
    return failedResponse("FAILED TO APPROVE");
  }

  private Boolean checkAuthorityExist(Authentication authentication, EmployeeRole role) {
    return authentication.getAuthorities().stream()
        .map(a -> a.getAuthority().equalsIgnoreCase(role.name()))
        .findAny()
        .isPresent();
  }
}
