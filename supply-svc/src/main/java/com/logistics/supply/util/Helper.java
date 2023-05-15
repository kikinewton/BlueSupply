package com.logistics.supply.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.EmployeeRole;
import java.util.ArrayList;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@Slf4j
@Component
public class Helper {
  public static ResponseEntity<?> notFound(String message) {
    ResponseDTO failed = new ResponseDTO(message, SUCCESS, new ArrayList<>());
    return ResponseEntity.ok(failed);
  }

  public static ResponseEntity<?> failedResponse(String message) {
    ResponseDTO failed = new ResponseDTO(message, ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }

  public static boolean hasRole(Employee employee, EmployeeRole role) {
    return employee.getRoles().stream().anyMatch(r -> role.name().equalsIgnoreCase(r.getName()));
  }




}
