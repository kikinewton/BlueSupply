package com.logistics.supply.service;

import com.logistics.supply.exception.NotFoundException;
import com.logistics.supply.exception.RoleNotFoundException;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.Role;
import com.logistics.supply.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {
  private final RoleRepository roleRepository;

  @Cacheable(value = "allRole", unless = "#result.isEmpty() == true")
  public List<Role> getRoles() {

    log.info("Fetch all roles");
    return roleRepository.findAll();
  }

  @Cacheable(value = "roleByName", key = "#name")
  public Role findByName(String name) {

    log.info("Fetch role: {}", name);
    return roleRepository.findByName(name).orElseThrow(() -> new RoleNotFoundException(name));
  }

  @Cacheable(value = "roleById", key = "#id")
  public Role findById(int id) {

    log.info("Fetch role with id: {}", id);
    return roleRepository.findById(id).orElseThrow(() -> new RoleNotFoundException(id));
  }

  @Cacheable(value = "employeeRole", key = "#authentication.getName()")
  public EmployeeRole getEmployeeRole(Authentication authentication) {

    log.info("Fetch role for employee with email {}", authentication.getName());
    String auth =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(i -> i.contains("ROLE_"))
            .findAny()
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Role not found for employee %s".formatted(authentication.getName())));
    return EmployeeRole.valueOf(auth);
  }
}
