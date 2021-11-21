package com.logistics.supply.controller;

import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.PettyCash;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.PettyCashService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@Slf4j
@RestController
@RequestMapping("/api")
public class PettyCashController {
  @Autowired PettyCashService pettyCashService;
  @Autowired EmployeeService employeeService;

  @PostMapping("/pettyCash")
  public ResponseEntity<?> createPettyCash(@Valid @RequestBody PettyCash pettyCash) {
    try {
      PettyCash cash = pettyCashService.save(pettyCash);
      if (Objects.nonNull(cash)) {
        ResponseDTO successResponse = new ResponseDTO("PETTY_CASH_CREATED", SUCCESS, cash);
        return ResponseEntity.ok(successResponse);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    ResponseDTO failed = new ResponseDTO("REQUEST_FAILED", ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }

  @GetMapping("/pettyCash")
  public ResponseEntity<?> findAllPettyCash(
      @RequestParam(required = false, defaultValue = "false") Boolean approved,
      @RequestParam(required = false, defaultValue = "false") Boolean endorsed,
      @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
      @RequestParam(value = "pageSize", defaultValue = "100") int pageSize) {
    if (approved) {
      try {
        List<PettyCash> approvedList = pettyCashService.findApprovedPettyCash(pageNo, pageSize);
        ResponseDTO successResponse =
            new ResponseDTO("FETCH_APPROVED_PETTY_CASH", SUCCESS, approvedList);
        return ResponseEntity.ok(successResponse);
      } catch (Exception e) {
        log.error(e.toString());
      }

    } else if (endorsed) {
      try {
        List<PettyCash> endorsedList = pettyCashService.findEndorsedPettyCash();
        ResponseDTO successResponse =
            new ResponseDTO("FETCH_ENDORSED_PETTY_CASH", SUCCESS, endorsedList);
        return ResponseEntity.ok(successResponse);
      } catch (Exception e) {
        log.error(e.toString());
      }
    } else {
      try {
        List<PettyCash> cashList = pettyCashService.findAllPettyCash(pageNo, pageSize);
        ResponseDTO successResponse = new ResponseDTO("FETCH_PETTY_CASH", SUCCESS, cashList);
        return ResponseEntity.ok(successResponse);
      } catch (Exception e) {
        log.error(e.toString());
      }
    }
    ResponseDTO failed = new ResponseDTO("FETCH_FAILED", ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }

  @GetMapping("/pettyCashByDepartment")
  @PreAuthorize("hasRole('ROLE_HOD')")
  public ResponseEntity<?> findAllPettyCashByDepartment(
      Authentication authentication,
      @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
      @RequestParam(value = "pageSize", defaultValue = "100") int pageSize) {
    try {
      Department department =
          employeeService.findEmployeeByEmail(authentication.getName()).getDepartment();
      List<PettyCash> cashList = pettyCashService.findByDepartment(department);
      ResponseDTO successResponse = new ResponseDTO("FETCH_PETTY_CASH", SUCCESS, cashList);
      return ResponseEntity.ok(successResponse);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    ResponseDTO failed = new ResponseDTO("FETCH_FAILED", ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }

  @GetMapping("/pettyCashForEmployee")
  public ResponseEntity<?> findPettyCashForEmployee(
      Authentication authentication,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "100") int pageSize) {
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    try {
      List<PettyCash> pettyCashList =
          pettyCashService.findByEmployee(employee.getId(), pageNo, pageSize);
      ResponseDTO success = new ResponseDTO("FETCH_EMPLOYEE_PETTY_CASH", SUCCESS, pettyCashList);
      return ResponseEntity.ok(success);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    ResponseDTO failed = new ResponseDTO("FETCH_FAILED", ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
