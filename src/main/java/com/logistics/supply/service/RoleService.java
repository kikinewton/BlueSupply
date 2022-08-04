package com.logistics.supply.service;

import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.Role;
import com.logistics.supply.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.logistics.supply.util.Constants.ROLE_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {
  private final RoleRepository roleRepository;

  @Cacheable(value = "allRole", unless = "#result.isEmpty() == true")
  public List<Role> getRoles() {
      return roleRepository.findAll();
  }

  @Cacheable(value = "roleByName", key = "#name")
  public Role findByName(String name) throws GeneralException {
    return roleRepository
        .findByName(name)
        .orElseThrow(() -> new GeneralException(ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  @Cacheable(value = "roleById", key = "#id")
  public Role findById(int id) throws GeneralException {
    return roleRepository
        .findById(id)
        .orElseThrow(() -> new GeneralException(ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  @Cacheable(value = "employeeRole", key = "#authentication.getName()")
  public EmployeeRole getEmployeeRole(Authentication authentication) throws GeneralException {
    String auth =
        authentication.getAuthorities().stream()
            .map(r -> r.getAuthority())
            .filter(i -> i.contains("ROLE_"))
            .findAny()
            .orElseThrow(() -> new GeneralException(ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));
    return EmployeeRole.valueOf(auth);
  }

  public void deleteRole(int id) {
    roleRepository.deleteById(id);
  }
}
