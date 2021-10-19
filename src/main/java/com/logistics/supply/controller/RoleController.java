package com.logistics.supply.controller;

import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.Role;
import com.logistics.supply.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RoleController {

  final RoleService roleService;

  @GetMapping("/roles")
  public ResponseEntity<?> findAllRoles() {
    try {
      List<Role> roles = roleService.getRoles();
      if (!roles.isEmpty()) {
        ResponseDTO successResponse = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, roles);
        return ResponseEntity.ok(successResponse);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    ResponseDTO failedResponse = new ResponseDTO(ERROR, null, "FAILED");
    return ResponseEntity.badRequest().body(failedResponse);
  }

  @GetMapping("/roles/{roleId}")
  public ResponseEntity<?> findRoleById(@PathVariable("roleId") int roleId) {
    try {
      Role role = roleService.findById(roleId);
      if (Objects.nonNull(role)) {
        ResponseDTO successResponse = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, role);
        return ResponseEntity.ok(successResponse);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    ResponseDTO failedResponse = new ResponseDTO(ERROR, null, "FAILED");
    return ResponseEntity.badRequest().body(failedResponse);
  }
}
