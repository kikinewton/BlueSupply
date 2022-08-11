package com.logistics.supply.service;

import com.logistics.supply.dto.EmployeeDTO;
import com.logistics.supply.dto.RegistrationRequest;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.event.RoleChangeEvent;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.Role;
import com.logistics.supply.repository.DepartmentRepository;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.EMPLOYEE_NOT_FOUND;
import static com.logistics.supply.util.Constants.ROLE_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class EmployeeService {
  private final RoleRepository roleRepository;
  private final EmployeeRepository employeeRepository;
  private final DepartmentRepository departmentRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Cacheable(value = "allEmployees", unless = "#result.isEmpty == true")
  public List<Employee> getAll() {
    return employeeRepository.findAll();
  }

  @CacheEvict(
      value = {"allEmployees", "departmentById", "employeeById2", "employeeByEmail"},
      allEntries = true)
  public Employee save(Employee employee) {
    return employeeRepository.save(employee);
  }

  @Cacheable(value = "employeeById", key = "#employeeId")
  public Employee getById(int employeeId) throws GeneralException {
    return employeeRepository
        .findById(employeeId)
        .orElseThrow(() -> new GeneralException(EMPLOYEE_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  @CacheEvict(
      value = {"allEmployees", "departmentById", "employeeById2", "employeeByEmail"},
      allEntries = true)
  public void deleteById(int employeeId) {
    employeeRepository.deleteById(employeeId);
  }

  @CacheEvict(
      value = {"allEmployees", "departmentById", "employeeById2", "employeeByEmail"},
      allEntries = true)
  public Employee disableEmployee(int employeeId) throws GeneralException {
    Employee employee =
        employeeRepository
            .findById(employeeId)
            .orElseThrow(() -> new GeneralException(EMPLOYEE_NOT_FOUND, HttpStatus.NOT_FOUND));
    employee.setEnabled(false);
    return employeeRepository.save(employee);
  }

  @CacheEvict(
      value = {"allEmployees", "departmentById", "employeeById2", "employeeByEmail"},
      allEntries = true)
  public Employee enableEmployee(int employeeId) throws GeneralException {
    Employee employee =
        employeeRepository
            .findById(employeeId)
            .orElseThrow(() -> new GeneralException(EMPLOYEE_NOT_FOUND, HttpStatus.NOT_FOUND));
    employee.setEnabled(true);
    return employeeRepository.save(employee);
  }

  @CacheEvict(
      value = {"allEmployees", "departmentById", "employeeById2", "employeeByEmail"},
      allEntries = true)
  public Employee create(Employee employee) {
    return employeeRepository.save(employee);
  }

  @Transactional
  @CacheEvict(
      value = {"allEmployees", "departmentById", "employeeById2", "employeeByEmail"},
      allEntries = true)
  public Employee update(int employeeId, EmployeeDTO updatedEmployee) throws GeneralException {
    Employee employee = getById(employeeId);
    AtomicBoolean roleChange = new AtomicBoolean(false);
    if (Objects.nonNull(updatedEmployee.getEmail())) employee.setEmail(updatedEmployee.getEmail());
    if (Objects.nonNull(updatedEmployee.getFirstName()))
      employee.setFirstName(updatedEmployee.getFirstName());
    if (Objects.nonNull(updatedEmployee.getLastName()))
      employee.setLastName(updatedEmployee.getLastName());
    if (Objects.nonNull(updatedEmployee.getPhoneNo()))
      employee.setPhoneNo(updatedEmployee.getPhoneNo());
    if (Objects.nonNull(updatedEmployee.getDepartment())) {
      Optional<Department> d =
          departmentRepository.findById(updatedEmployee.getDepartment().getId());
      d.ifPresent(employee::setDepartment);
    }
    if (!updatedEmployee.getRole().isEmpty()) {
      List<Role> oldRole = employee.getRoles();
      List<Role> roles =
          updatedEmployee.getRole().stream()
              .map(r -> roleRepository.findById(r.getId()).get())
              .collect(Collectors.toList());

      employee.setRoles(roles);
      if (!sameRole(oldRole, roles)) roleChange.set(true);
    }
    employee.setUpdatedAt(new Date());
    Employee savedEmployee = employeeRepository.save(employee);
    if (roleChange.get()) {
      CompletableFuture.runAsync(
          () -> {
            RoleChangeEvent event = new RoleChangeEvent(this, savedEmployee, roleChange.get());
            applicationEventPublisher.publishEvent(event);
          });
    }
    return savedEmployee;
  }

  private boolean sameRole(List<Role> oldList, List<Role> newRole) {
    for (Role or : oldList)
      for (Role nr : newRole) {
        if (Objects.equals(nr.getName(), or.getName()) && oldList.size() == newRole.size())
          return true;
      }
    return false;
  }

  @CacheEvict(
      value = {"allEmployees", "departmentById", "employeeById2", "employeeByEmail"},
      allEntries = true)
  public Employee signUp(RegistrationRequest request) {
    Employee newEmployee = new Employee();
    String password = "password1.com";
    newEmployee.setPassword(bCryptPasswordEncoder.encode(password));
    newEmployee.setDepartment(request.getDepartment());
    newEmployee.setFirstName(request.getFirstName());
    newEmployee.setEmail(request.getEmail());
    newEmployee.setPhoneNo(request.getPhoneNo());
    newEmployee.setLastName(request.getLastName());
    newEmployee.setRoles(request.getEmployeeRole());
    newEmployee.setEnabled(true);
    return employeeRepository.save(newEmployee);
  }

  @CacheEvict(
      value = {"allEmployees", "departmentById", "employeeById2", "employeeByEmail"},
      allEntries = true)
  public Employee changePassword(String password, String email) {
    Employee employee = findEmployeeByEmail(email);

    employee.setPassword(bCryptPasswordEncoder.encode(password));
    return employeeRepository.save(employee);
  }

  @SneakyThrows
  @Cacheable(value = "employeeById2", key = "#employeeId")
  public Employee findEmployeeById(int employeeId) {
    return employeeRepository
        .findById(employeeId)
        .orElseThrow(() -> new GeneralException("EMPLOYEE NOT FOUND", HttpStatus.NOT_FOUND));
  }

  @SneakyThrows
  @Cacheable(value = "employeeByEmail", key = "#email")
  public Employee findEmployeeByEmail(String email) {
    return employeeRepository
        .findByEmailAndEnabledIsTrue(email)
        .orElseThrow(() -> new GeneralException(EMPLOYEE_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  @SneakyThrows
  @Cacheable(value = "generalManager", unless = "#result.getEnabled == false")
  public Employee getGeneralManager() {
    Role role =
        roleRepository
            .findByName("ROLE_GENERAL_MANAGER")
            .orElseThrow(() -> new GeneralException(ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));
    return employeeRepository
        .getGeneralManager(role.getId())
        .orElseThrow(() -> new GeneralException(EMPLOYEE_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  @SneakyThrows
  @Cacheable(value = "managerByRoleName", key = "#roleName", unless = "#result.getEnabled == false")
  public Employee getManagerByRoleName(String roleName) {
    Role role =
        roleRepository
            .findByName(roleName)
            .orElseThrow(() -> new GeneralException(ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));
    return employeeRepository
        .findManagerByRoleId(role.getId())
        .orElseThrow(() -> new GeneralException(EMPLOYEE_NOT_FOUND, HttpStatus.NOT_FOUND));
  }


  @Cacheable(
      value = "departmentHOD",
      key = "#department.getId()",
      unless = "#result.getEnabled == false")
  public Employee getDepartmentHOD(Department department) throws GeneralException {
    Role r =
        roleRepository
            .findByName("ROLE_HOD")
            .orElseThrow(() -> new GeneralException(ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));
    return employeeRepository
        .findDepartmentHod(department.getId(), r.getId())
        .orElseThrow(() -> new GeneralException(EMPLOYEE_NOT_FOUND, HttpStatus.NOT_FOUND));
  }


  @Cacheable(value = "employeeByRoleId", key = "#roleId", unless = "#result.getEnabled == false")
  public Employee findRecentEmployeeWithRoleId(int roleId) throws GeneralException {
    return employeeRepository
        .findRecentEmployeeWithRoleId(roleId)
        .orElseThrow(
            () -> new GeneralException("EMPLOYEE WITH ROLE NOT FOUND", HttpStatus.NOT_FOUND));
  }

  public long count() {
    return employeeRepository.countAll() + 1;
  }
}
