package com.logistics.supply.controller;

import com.logistics.supply.dto.BulkFloatsDTO;
import com.logistics.supply.dto.PagedResponseDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.enums.UpdateStatus;
import com.logistics.supply.event.listener.FundsReceivedFloatListener;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.Floats;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.FloatService;
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
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.SUCCESS;
import static com.logistics.supply.util.Helper.failedResponse;
import static com.logistics.supply.util.Helper.notFound;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FloatController {

  private final FloatService floatService;
  private final EmployeeService employeeService;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Operation(summary = "Tag float when the requester receives money from accounts", tags = "FLOATS")
  @PutMapping("/floats/receiveFunds")
  public ResponseEntity<?> setAllocateFundsToFloat(
      @Valid @RequestBody BulkFloatsDTO floats, Authentication authentication) {
    try {
      Set<Floats> updatedFloats =
          floats.getFloats().stream()
              .map(
                  f -> {
                    if (f.getStatus().equals(RequestStatus.APPROVED))
                      f.setStatus(RequestStatus.PROCESSED);
                    f.setFundsReceived(true);
                    return floatService.saveFloat(f);
                  })
              .collect(Collectors.toSet());
      if (updatedFloats.isEmpty()) return notFound("FUNDS_ALLOCATION_FAILED");
      ResponseDTO response = new ResponseDTO("UPDATE_FLOATS_SUCCESSFUL", SUCCESS, updatedFloats);
      Employee employee = employeeService.findEmployeeByEmail(authentication.getName());

      FundsReceivedFloatListener.FundsReceivedFloatEvent fundsReceivedFloatEvent =
          new FundsReceivedFloatListener.FundsReceivedFloatEvent(this, employee, updatedFloats);
      applicationEventPublisher.publishEvent(fundsReceivedFloatEvent);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("UPDATE_FLOAT_FAILED");
  }

  @ApiOperation("Get all floats by status")
  @GetMapping("/floats")
  public ResponseEntity<?> findAllFloat(
      @ApiParam(name = "approval", value = "The status of approval")
          Optional<RequestApproval> approval,
      @RequestParam(required = false) Optional<EndorsementStatus> endorsement,
      @RequestParam(required = false) Optional<RequestStatus> status,
      @RequestParam(required = false) Optional<Boolean> retired,
      @RequestParam(required = false) Optional<Boolean> awaitingFunds,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "100") int pageSize) {
    try {

      if (approval.isPresent()) {

        return floatByApprovalStatus(approval.get(), pageNo, pageSize);
      }
      if (retired.isPresent()) {

        return floatsByRetiredStatus(retired.get(), pageNo, pageSize);
      }
      if (endorsement.isPresent()) {

        return floatByEndorsementStatus(endorsement.get(), pageNo, pageSize);
      }
      if (awaitingFunds.isPresent()) {

        return floatsAwaitingFunds(pageNo, pageSize);
      }
      if (status.isPresent()) {

        return floatByRStatus(status.get(), pageNo, pageSize);

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

  private ResponseEntity<?> floatByRStatus(RequestStatus status, int pageNo, int pageSize) {
    Page<Floats> floats = floatService.findFloatsByRequestStatus(pageNo, pageSize, status);

    if (floats != null) {
      return pagedResult(floats);
    }
    return failedResponse("FETCH_FAILED");
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
    return failedResponse("FETCH_FAILED");
  }

  private ResponseEntity<?> floatsByRetiredStatus(Boolean retired, int pageNo, int pageSize) {
    Page<Floats> floats = floatService.findFloatsByRetiredStatus(pageNo, pageSize, retired);

    if (floats != null) {
      return pagedResult(floats);
    }
    return failedResponse("FETCH_FAILED");
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
    return failedResponse("FETCH_FAILED");
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
    return failedResponse("FETCH_FAILED");
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

  @PutMapping("/bulkFloats/{statusChange}")
  @PreAuthorize("hasRole('ROLE_HOD') or hasRole('ROLE_GENERAL_MANAGER')")
  public ResponseEntity<?> changeState(
      @Valid @RequestBody Set<Floats> bulkFloat,
      @PathVariable("statusChange") UpdateStatus statusChange,
      Authentication authentication) {

    switch (statusChange) {
      case APPROVE:
        return approveFloats(bulkFloat, authentication);
      case ENDORSE:
        return endorseFloats(bulkFloat, authentication);
      case CANCEL:
        return cancelFloats(bulkFloat, authentication);
      default:
        return failedResponse("FAILED_REQUEST");
    }
  }

  private ResponseEntity<?> endorseFloats(Set<Floats> bulkItems, Authentication authentication) {

    if (!checkAuthorityExist(authentication, EmployeeRole.ROLE_HOD))
      return failedResponse("FORBIDDEN_ACCESS");
    Set<Floats> floats =
        bulkItems.stream()
            .map(x -> floatService.endorse(x.getId(), EndorsementStatus.ENDORSED))
            .collect(Collectors.toSet());
    if (!floats.isEmpty()) {
      ResponseDTO response = new ResponseDTO("ENDORSE_FLOATS_SUCCESSFUL", SUCCESS, floats);
      return ResponseEntity.ok(response);
    }
    return failedResponse("FAILED_TO_ENDORSE");
  }

  private ResponseEntity<?> approveFloats(Set<Floats> bulkFloats, Authentication authentication) {
    if (!checkAuthorityExist(authentication, EmployeeRole.ROLE_GENERAL_MANAGER))
      return failedResponse("FORBIDDEN_ACCESS");
    Set<Floats> floats =
        bulkFloats.stream()
            .map(x -> floatService.approve(x.getId(), RequestApproval.APPROVED))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    if (!floats.isEmpty()) {
      ResponseDTO response = new ResponseDTO("APPROVE_FLOAT_SUCCESSFUL", SUCCESS, floats);
      return ResponseEntity.ok(response);
    }

    return failedResponse("FAILED_TO_ENDORSE");
  }

  private ResponseEntity<?> cancelFloats(Set<Floats> bulkFloats, Authentication authentication) {

    if (checkAuthorityExist(authentication, EmployeeRole.ROLE_HOD)) {
      Set<Floats> floats =
          bulkFloats.stream()
              .map(x -> floatService.endorse(x.getId(), EndorsementStatus.REJECTED))
              .filter(Objects::nonNull)
              .collect(Collectors.toSet());
      if (!floats.isEmpty()) {
        ResponseDTO response = new ResponseDTO("FLOAT_ENDORSEMENT_CANCELLED", SUCCESS, floats);
        return ResponseEntity.ok(response);
      }
    }

    if (!checkAuthorityExist(authentication, EmployeeRole.ROLE_GENERAL_MANAGER)) {
      Set<Floats> floats =
          bulkFloats.stream()
              .map(x -> floatService.approve(x.getId(), RequestApproval.REJECTED))
              .filter(Objects::nonNull)
              .collect(Collectors.toSet());
      if (!floats.isEmpty()) {
        ResponseDTO response = new ResponseDTO("FLOAT_APPROVAL_CANCELLED", SUCCESS, floats);
        return ResponseEntity.ok(response);
      }
    }

    return failedResponse("CANCEL_REQUEST_FAILED");
  }

  public ResponseEntity<?> retireFloat(Authentication authentication) {
    try {
      Employee employee = employeeService.findEmployeeByEmail(authentication.getName());

    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("FLOAT_RETIREMENT_FAILED");
  }

  private Boolean checkAuthorityExist(Authentication authentication, EmployeeRole role) {
    return authentication.getAuthorities().stream()
        .map(x -> x.getAuthority().equalsIgnoreCase(role.name()))
        .filter(x -> x == true)
        .findAny()
        .get();
  }
}
