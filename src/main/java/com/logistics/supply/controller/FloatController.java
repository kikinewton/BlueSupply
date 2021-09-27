package com.logistics.supply.controller;

import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.Floats;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.FloatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@Slf4j
@RestController
@RequestMapping("/api")
public class FloatController {

  @Autowired FloatService floatService;
  @Autowired EmployeeService employeeService;

  @GetMapping("/floats")
  public ResponseEntity<?> findAllFloat() {

    return failedResponse("FETCH_FAILED");
  }

  @GetMapping("/floats/employee")
  public ResponseEntity<?> findByEmployee(
      Authentication authentication,
      @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
      @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
    try {
      int employeeId = employeeService.findEmployeeByEmail(authentication.getName()).getId();
      List<Floats> floats = floatService.findByEmployee(employeeId, pageNo, pageSize);
      ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, floats);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("FETCH_FAILED");
  }

  public ResponseEntity<?> createFloat() {
    return failedResponse("FETCH_FAILED");
  }

  private ResponseEntity<ResponseDTO> failedResponse(String message) {
    ResponseDTO failed = new ResponseDTO(message, ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
