package com.logistics.supply.loader;

import com.logistics.supply.model.Employee;
import com.logistics.supply.model.Privilege;
import com.logistics.supply.model.Role;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.repository.PrivilegeRepository;
import com.logistics.supply.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {
  boolean alreadySetup = false;

  @Autowired EmployeeRepository employeeRepository;

  @Autowired RoleRepository roleRepository;

  @Autowired PrivilegeRepository privilegeRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    if(employeeRepository.count() > 1) return;
//    if (alreadySetup) return;
    Privilege readPrivilege = createPrivilegeIfNotFound("READ_PRIVILEGE");
    Privilege writePrivilege = createPrivilegeIfNotFound("WRITE_PRIVILEGE");

    List<Privilege> readAndWritePrivileges = Arrays.asList(readPrivilege, writePrivilege);
    createRoleIfNotFound("ROLE_ADMIN", readAndWritePrivileges);
    createRoleIfNotFound("ROLE_REGULAR", readAndWritePrivileges);
    createRoleIfNotFound("ROLE_HOD", readAndWritePrivileges);
    createRoleIfNotFound("ROLE_GENERAL_MANAGER", readAndWritePrivileges);
    createRoleIfNotFound("ROLE_PROCUREMENT_OFFICER", readAndWritePrivileges);
    createRoleIfNotFound("ROLE_STORE_OFFICER", readAndWritePrivileges);
    createRoleIfNotFound("ROLE_ACCOUNT_OFFICER", readAndWritePrivileges);
    createRoleIfNotFound("ROLE_CHIEF_ACCOUNT_OFFICER", readAndWritePrivileges);
    createRoleIfNotFound("ROLE_PROCUREMENT_MANAGER", readAndWritePrivileges);
    createRoleIfNotFound("ROLE_FINANCIAL_MANAGER", readAndWritePrivileges);
    createRoleIfNotFound("ROLE_AUDITOR", readAndWritePrivileges);



    Role adminRole = roleRepository.findByName("ROLE_ADMIN");
    Employee user = new Employee();
    user.setFirstName("Test");
    user.setLastName("Test");
    user.setPassword(passwordEncoder.encode("password1.com"));
    user.setEmail("admin@test.com");
    user.setPhoneNo("000000000000");
    user.setRoles(Arrays.asList(adminRole));
    user.setEnabled(true);
    employeeRepository.save(user);

    alreadySetup = true;
  }

  @Transactional
  Privilege createPrivilegeIfNotFound(String name) {

    Privilege privilege = privilegeRepository.findByName(name);
    if (privilege == null) {
      privilege = new Privilege(name);
      privilegeRepository.save(privilege);
    }
    return privilege;
  }

  @Transactional
  Role createRoleIfNotFound(String name, Collection<Privilege> privileges) {

    Role role = roleRepository.findByName(name);
    if (role == null) {
      role = new Role(name);
      role.setPrivileges(privileges);
      roleRepository.save(role);
    }
    return role;
  }
}
