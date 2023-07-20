package com.logistics.supply.service;

import com.logistics.supply.dto.ChangePasswordDto;
import com.logistics.supply.dto.EmployeeDto;
import com.logistics.supply.dto.RegistrationRequest;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.event.RoleChangeEvent;
import com.logistics.supply.event.listener.EmployeeDisabledEventListener;
import com.logistics.supply.exception.EmailAlreadyExistException;
import com.logistics.supply.exception.EmployeeNotFoundException;
import com.logistics.supply.exception.NotFoundException;
import com.logistics.supply.exception.RoleNotFoundException;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.Role;
import com.logistics.supply.repository.DepartmentRepository;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.repository.RoleRepository;
import com.logistics.supply.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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

    log.info("Get all employees");
    return employeeRepository.findAll();
  }

  @CacheEvict(
      value = {"allEmployees", "departmentById", "employeeById2", "employeeByEmail"},
      allEntries = true)
  public Employee save(Employee employee) {

    log.info("Attempting to save data for employee withe email: {}", employee.getEmail());
    return employeeRepository.save(employee);
  }

  @Cacheable(value = "employeeById", key = "#employeeId")
  public Employee getEmployeeById(int employeeId) {

    log.info("Find employee with id: {}", employeeId);
    return employeeRepository
        .findById(employeeId)
        .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
  }

  public void deleteById(int employeeId) {
    log.info("Attempting to delete employee with id: {}", employeeId);
  }

  @CacheEvict(
      value = {"allEmployees", "departmentById", "employeeById2", "employeeByEmail"},
      allEntries = true)
  public Employee disableEmployee(int employeeId) {

    log.info("Disable user with employee id : {}", employeeId);
    Employee employee = getEmployeeById(employeeId);
    employee.setEnabled(false);
    employeeRepository.disableEmployee(employeeId);
    sendDisabledEmailNotification(employee);
    return employee;
  }

  private void sendDisabledEmailNotification(Employee employee) {
    CompletableFuture.runAsync(
            () -> {
              EmployeeDisabledEventListener.EmployeeDisableEvent disableEvent =
                      new EmployeeDisabledEventListener.EmployeeDisableEvent(this, employee);
              applicationEventPublisher.publishEvent(disableEvent);
            });
  }

  @CacheEvict(
      value = {"allEmployees", "departmentById", "employeeById2", "employeeByEmail"},
      allEntries = true)
  public Employee changeEnableStatus(int employeeId) {

    Employee employee = getEmployeeById(employeeId);
    employee.setEnabled(!employee.isEnabled());
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
  public Employee update(int employeeId, EmployeeDto updatedEmployee) {
    Employee employee = getEmployeeById(employeeId);

    log.info("Update info of employee {} with details {}", employee, updatedEmployee);
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
              .map(r -> roleRepository.findById(r.getId())
                      .orElseThrow(()-> new RoleNotFoundException(r.getId())))
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

  private boolean sameRole(List<Role> oldList, List<Role> newList) {
    if (oldList.size() != newList.size()) {
      return false;
    }

    for (Role oldRole : oldList) {
      boolean foundMatch = false;
      for (Role newRole : newList) {
        if (Objects.equals(oldRole.getName(), newRole.getName())) {
          foundMatch = true;
          break;
        }
      }
      if (!foundMatch) {
        return false;
      }
    }

    return true;
  }

  @CacheEvict(
      value = {"allEmployees", "departmentById", "employeeById2", "employeeByEmail"},
      allEntries = true)
  public Employee signUp(RegistrationRequest request) {
    String email = request.getEmail();
    Optional<Employee> employee = employeeRepository.findByEmail(email);

    if (employee.isPresent()) {
      throw new EmailAlreadyExistException(email);
    }

    Employee newEmployee = new Employee();
    String password = "password1.com";
    newEmployee.setPassword(bCryptPasswordEncoder.encode(password));
    newEmployee.setDepartment(request.getDepartment());
    newEmployee.setFirstName(request.getFirstName());
    newEmployee.setEmail(email);
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
        .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
  }

  @SneakyThrows
  @Cacheable(value = "employeeByEmail", key = "#email")
  public Employee findEmployeeByEmail(String email) {

    log.info("Find employee with email: {}", email);
    return employeeRepository
        .findByEmailAndEnabledIsTrue(email)
        .orElseThrow(() -> new EmployeeNotFoundException(email));
  }

  @SneakyThrows
  @Cacheable(value = "generalManager", unless = "#result.isEnabled() == false")
  public Employee getGeneralManager() {
    Role role =
        roleRepository
            .findByName("ROLE_GENERAL_MANAGER")
            .orElseThrow(
                () -> new GeneralException(Constants.ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));
    return employeeRepository
        .getGeneralManager(role.getId())
        .orElseThrow(
            () -> new GeneralException(Constants.EMPLOYEE_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  @SneakyThrows
  @Cacheable(value = "managerByRoleName", key = "#roleName", unless = "#result.isEnabled() == false")
  public Employee getManagerByRoleName(String roleName) {
    Role role =
        roleRepository
            .findByName(roleName)
            .orElseThrow(
                () -> new GeneralException(Constants.ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));
    return employeeRepository
        .findManagerByRoleId(role.getId())
        .orElseThrow(
            () -> new GeneralException(Constants.EMPLOYEE_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  @Cacheable(
      value = "departmentHOD",
      key = "#department.getId()",
      unless = "#result.isEnabled() == false")
  public Employee getDepartmentHOD(Department department) {
    Role r =
        roleRepository
            .findByName("ROLE_HOD")
            .orElseThrow(() -> new NotFoundException("Role HOD not found"));
    return employeeRepository
        .findDepartmentHod(department.getId(), r.getId())
        .orElseThrow(
            () ->
                new NotFoundException(
                    "HOD of department %s not found".formatted(department.getName())));
  }

  @Cacheable(value = "employeeByRoleId", key = "#roleId", unless = "#result.isEnabled() == false")
  public Employee findRecentEmployeeWithRoleId(int roleId) {

    return employeeRepository
        .findRecentEmployeeWithRoleId(roleId)
        .orElseThrow(
            () -> new NotFoundException("Employee with role id: %s not found".formatted(roleId)));
  }

  public long count() {
    return employeeRepository.countAll() + 1;
  }

  public Employee selfPasswordChange(
          Employee employee,
          ChangePasswordDto changePasswordDto) {

    log.info("Employee with email {} is initiating a password change", employee.getEmail());
    String encodedNewPassword = bCryptPasswordEncoder.encode(changePasswordDto.getNewPassword());
    employee.setPassword(encodedNewPassword);
    return employeeRepository.save(employee);
  }
}
