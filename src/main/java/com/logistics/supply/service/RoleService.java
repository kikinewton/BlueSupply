package com.logistics.supply.service;

import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.Role;
import com.logistics.supply.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.logistics.supply.util.Constants.ROLE_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

  final RoleRepository roleRepository;

  public List<Role> getRoles() {
    try {
      return roleRepository.findAll();
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return new ArrayList<>();
  }

  public Role findByName(String name) throws GeneralException {
      return roleRepository
          .findByName(name)
          .orElseThrow(() -> new GeneralException(ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  public Role findById(int id) {
    try {
      Optional<Role> role = roleRepository.findById(id);
      if (role.isPresent()) return role.get();
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public EmployeeRole getEmployeeRole(Authentication authentication) {
    String auth =
        authentication.getAuthorities().stream()
            .map(r -> r.getAuthority())
            .filter(i -> i.contains("ROLE_"))
            .findAny()
            .orElse(null);
    return EmployeeRole.valueOf(auth);
  }

  public void deleteRole(int id) {
    try {
      roleRepository.deleteById(id);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }
}
