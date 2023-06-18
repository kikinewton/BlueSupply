package com.logistics.supply.controller;

import com.logistics.supply.dto.ResponseDto;
import com.logistics.supply.model.Role;
import com.logistics.supply.service.RoleService;
import com.logistics.supply.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RoleController {

  final RoleService roleService;

  @GetMapping("/roles")
  public ResponseEntity<?> listAllRoles() {
    List<Role> roles = roleService.getRoles();
    ResponseDto successResponse = new ResponseDto("FETCH_SUCCESSFUL", Constants.SUCCESS, roles);
    return ResponseEntity.ok(successResponse);
  }

  @GetMapping("/roles/{roleId}")
  public ResponseEntity<?> getRoleById(@PathVariable("roleId") int roleId) {
    try {
      Role role = roleService.findById(roleId);
      if (Objects.nonNull(role)) {
        ResponseDto successResponse = new ResponseDto("FETCH_SUCCESSFUL", Constants.SUCCESS, role);
        return ResponseEntity.ok(successResponse);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    ResponseDto failedResponse = new ResponseDto(Constants.ERROR, null, "FAILED");
    return ResponseEntity.badRequest().body(failedResponse);
  }
}
