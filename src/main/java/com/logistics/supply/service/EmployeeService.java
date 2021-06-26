package com.logistics.supply.service;

import com.logistics.supply.dto.EmployeeDTO;
import com.logistics.supply.dto.RegistrationRequest;
import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.ApplicationUserRole;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.enums.EmployeeLevel;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.EmployeeRole;
// import com.logistics.supply.repository.EmployeeRoleRepository;
import com.logistics.supply.util.CommonHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.logistics.supply.util.CommonHelper.buildNewUserEmail;
import static com.logistics.supply.util.Constants.NEW_USER_PASSWORD_MAIL;

@Service
@Slf4j
@AllArgsConstructor
public class EmployeeService extends AbstractDataService {

  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final EmailSender emailSender;
  private final String EMPLOYEE_NOT_FOUND = "Employee not found";

  public List<Employee> getAll() {
    log.info("Get all employees");
    List<Employee> employees = new ArrayList<>();
    try {
      List<Employee> employeeList = employeeRepository.findAll();
      employees.addAll(employeeList);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return employees;
  }

  public Employee getById(int employeeId) {
    Optional<Employee> employee = employeeRepository.findById(employeeId);
    try {
      return employee.orElseThrow(Exception::new);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public void deleteById(int employeeId) {
    try {
      employeeRepository.deleteById(employeeId);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Employee create(Employee employee) {
    try {
      return employeeRepository.save(employee);

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @Transactional(rollbackFor = Exception.class)
  public Employee update(int employeeId, EmployeeDTO updatedEmployee) {
    Employee employee = getById(employeeId);
    if (Objects.nonNull(updatedEmployee.getEmail())) employee.setEmail(updatedEmployee.getEmail());
    if (Objects.nonNull(updatedEmployee.getFirstName()))
      employee.setFirstName(updatedEmployee.getFirstName());
    if (Objects.nonNull(updatedEmployee.getLastName()))
      employee.setLastName(updatedEmployee.getLastName());
    if (Objects.nonNull(updatedEmployee.getPhoneNo()))
      employee.setPhoneNo(updatedEmployee.getPhoneNo());
    if (Objects.nonNull(updatedEmployee.getDepartment()))
      employee.setDepartment(updatedEmployee.getDepartment());
    if (Objects.nonNull(updatedEmployee.getRole())) {
      employee.getRole().clear();
      employee.getRole().addAll(updatedEmployee.getRole());
    }
    employee.setUpdatedAt(new Date());
    try {
      Employee saved = employeeRepository.save(employee);
      return saved;
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  @Transactional(rollbackFor = Exception.class)
  public Employee changeRole(int employeeId, List<EmployeeRole> roles) {
    Employee employee = findEmployeeById(employeeId);
    try {
      employee.setRole(roles);
      employeeRepository.save(employee);
      return employee;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public Employee signUp(RegistrationRequest request) {
    boolean employeeExist = employeeRepository.findByEmail(request.getEmail()).isPresent();

    if (employeeExist) {
      throw new IllegalStateException("Employee with email already exist");
    }
    Employee newEmployee = new Employee();
    String password1 = CommonHelper.generatePassword("b$", 12);
    //    log.info("Employee email: " + request.getEmail() + " Password: " + password);
    String password = "password1.com";
    newEmployee.setPassword(bCryptPasswordEncoder.encode(password));
    newEmployee.setDepartment(request.getDepartment());
    newEmployee.setFirstName(request.getFirstName());
    newEmployee.setEmail(request.getEmail());
    newEmployee.setPhoneNo(request.getPhoneNo());
    newEmployee.setLastName(request.getLastName());
    //    Set<EmployeeRole> userRole = new HashSet<>();
    //    EmployeeRole role = new EmployeeRole(request.getEmployeeLevel());
    //    userRole.add(role);
    newEmployee.setRole(request.getEmployeeRole());
    newEmployee.setEnabled(true);

    Employee result = employeeRepository.save(newEmployee);
    if (Objects.nonNull(result)) {
      return result;
    }
    return null;
  }

  public Employee findEmployeeById(int employeeId) {
    Employee employee = null;
    try {
      return employeeRepository.findById(employeeId).get();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public Employee findEmployeeByEmail(String email) {
    Employee employee = null;
    try {
      return employeeRepository.findByEmail(email).orElseThrow(Exception::new);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public boolean verifyEmployeeRole(int employeeId, EmployeeRole employeeRole) {
    Employee employee = findEmployeeById(employeeId);
    if (Objects.isNull(employee)) return false;
    else if (employee.getRole().contains(employeeRole)) {
      System.out.println("Employee with id " + employeeId + " has role " + employeeRole);
      return true;
    }
    return false;
  }

  public List<Employee> findAllEmployees() {
    List<Employee> employees = new ArrayList<>();
    try {
      employees.addAll(employeeRepository.findAll());
      return employees;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return employees;
  }

  public Employee getGeneralManager(int roleId) {
    Employee employee = employeeRepository.getGeneralManager(roleId);
    if (Objects.nonNull(employee)) return employee;
    return null;
  }

  public Employee getDepartmentHOD(Department department) {
    Employee employee =
        employeeRepository.findDepartmentHod(department.getId(), EmployeeRole.ROLE_HOD.ordinal());
    return employee;
  }

  public Employee getHODOfDepartment(Department department) {
    Optional<Employee> hod =
        employeeRepository.findAll().stream()
            .filter(
                x ->
                    x.getDepartment().equals(department) && x.getRole().contains(EmployeeLevel.HOD))
            .findFirst();
    if (hod.isPresent()) return hod.get();
    return null;
  }

  public boolean verifyEmployeeDepartment(int employeeId, int departmentId) {
    Employee employee = findEmployeeById(employeeId);
    if (employee.getDepartment().getId() == departmentId) return true;
    return false;
  }
}
