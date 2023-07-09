package com.logistics.supply.controller;

import com.logistics.supply.dto.ResponseDto;
import com.logistics.supply.model.Role;
import com.logistics.supply.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.logistics.supply.util.Constants.FETCH_SUCCESSFUL;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RoleController {

  private final RoleService roleService;

  @GetMapping("/roles")
  public ResponseEntity<ResponseDto<List<Role>>> listAllRoles() {

    List<Role> roles = roleService.getRoles();
    return ResponseDto.wrapSuccessResult(roles, FETCH_SUCCESSFUL);
  }

  @GetMapping("/roles/{roleId}")
  public ResponseEntity<ResponseDto<Role>> getRoleById(
          @PathVariable("roleId") int roleId) {

      Role role = roleService.findById(roleId);
      return ResponseDto.wrapSuccessResult( role, FETCH_SUCCESSFUL);
  }
}
