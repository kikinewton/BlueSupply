package com.logistics.supply.service;

import com.logistics.supply.dto.ChangePasswordDto;
import com.logistics.supply.dto.EmployeeDto;
import com.logistics.supply.dto.RegistrationRequest;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EmployeeService {

    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder bCryptPasswordEncoder;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Cacheable(value = "allEmployees", unless = "#result.isEmpty == true")
    public List<Employee> getAll() {

        log.info("Get all employees");
        return employeeRepository.findAll();
    }

    @CacheEvict(
            value = {"allEmployees", "departmentById", "employeeById", "employeeByEmail"},
            allEntries = true)
    public Employee save(Employee employee) {

        log.info("Attempting to save data for employee with email: {}", employee.getEmail());
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
        employeeRepository.deleteById(employeeId);
    }

    @CacheEvict(
            value = {"allEmployees", "departmentById", "employeeById", "employeeByEmail"},
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
            value = {"allEmployees", "departmentById", "employeeById", "employeeByEmail"},
            allEntries = true)
    public Employee changeEnableStatus(int employeeId) {

        Employee employee = getEmployeeById(employeeId);
        employee.setEnabled(!employee.isEnabled());
        return employeeRepository.save(employee);
    }

    @CacheEvict(
            value = {"allEmployees", "departmentById", "employeeById", "employeeByEmail"},
            allEntries = true)
    public Employee create(Employee employee) {
        log.info("Attempting to save data for employee with email: {}", employee.getEmail());
        return employeeRepository.save(employee);
    }

    @Transactional
    @CacheEvict(
            value = {"allEmployees", "departmentById", "employeeById", "employeeByEmail"},
            allEntries = true)
    public Employee update(int employeeId, EmployeeDto updatedEmployee) {
        Employee employee = getEmployeeById(employeeId);

        log.info("Update info of employee {} with details {}", employee, updatedEmployee);
        boolean roleChange = false;
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
                                    .orElseThrow(() -> new RoleNotFoundException(r.getId())))
                            .collect(Collectors.toList());

            employee.setRoles(roles);
            if (!sameRole(oldRole, roles)) roleChange = true;
        }
        Employee savedEmployee = employeeRepository.save(employee);
        if (roleChange) {
            CompletableFuture.runAsync(
                    () -> {
                        RoleChangeEvent event = new RoleChangeEvent(this, savedEmployee, true);
                        applicationEventPublisher.publishEvent(event);
                    });
        }
        return savedEmployee;
    }

    private boolean sameRole(List<Role> oldList, List<Role> newList) {
        if (oldList.size() != newList.size()) return false;
        Set<String> oldNames = oldList.stream().map(Role::getName).collect(Collectors.toSet());
        return newList.stream().map(Role::getName).allMatch(oldNames::contains);
    }

    @CacheEvict(
            value = {"allEmployees", "departmentById", "employeeById", "employeeByEmail"},
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
            value = {"allEmployees", "departmentById", "employeeById", "employeeByEmail"},
            allEntries = true)
    public Employee changePassword(String password, String email) {
        employeeRepository.updatePasswordByEmail(bCryptPasswordEncoder.encode(password), email);
        return findEmployeeByEmail(email);
    }

    public Employee findEmployeeById(int employeeId) {
        return getEmployeeById(employeeId);
    }

    @Cacheable(value = "employeeByEmail", key = "#email")
    public Employee findEmployeeByEmail(String email) {

        log.info("Find employee with email: {}", email);
        return employeeRepository
                .findByEmailAndEnabledIsTrue(email)
                .orElseThrow(() -> new EmployeeNotFoundException(email));
    }

    @Cacheable(value = "generalManager", unless = "#result.isEnabled() == false")
    public Employee getGeneralManager() {
        return employeeRepository
                .findManagerByRoleName("ROLE_GENERAL_MANAGER")
                .orElseThrow(
                        () -> new NotFoundException(
                                "Employee with role: ROLE_GENERAL_MANAGER not found"));
    }

    @Cacheable(value = "managerByRoleName", key = "#roleName", unless = "#result.isEnabled() == false")
    public Employee getManagerByRoleName(String roleName) {
        return employeeRepository
                .findManagerByRoleName(roleName)
                .orElseThrow(
                        () -> new NotFoundException("Employee with role: %s not found".formatted(roleName)));
    }

    @Cacheable(
            value = "departmentHOD",
            key = "#department.getId()",
            unless = "#result.isEnabled() == false")
    public Employee getDepartmentHOD(Department department) {
        return employeeRepository
                .findDepartmentHodByRoleName(department.getId(), "ROLE_HOD")
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
        return employeeRepository.countAll();
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
