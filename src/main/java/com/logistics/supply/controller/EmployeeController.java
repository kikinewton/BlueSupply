package com.logistics.supply.controller;

import com.logistics.supply.dto.ChangePasswordDTO;
import com.logistics.supply.dto.EmployeeDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.Employee;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.service.AbstractRestService;
import com.logistics.supply.auth.AuthService;
import com.logistics.supply.util.CommonHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

import static com.logistics.supply.util.CommonHelper.MatchBCryptPassword;
import static com.logistics.supply.util.CommonHelper.isValidEmailAddress;
import static com.logistics.supply.util.Constants.*;

@RestController
@Slf4j
@RequestMapping("/api")
public class EmployeeController extends AbstractRestService {

  @Autowired private AuthService authService;

  @Autowired
  private BCryptPasswordEncoder bCryptPasswordEncoder;

  @Autowired
  private EmployeeRepository employeeRepository;

  @GetMapping("/employees")
  public ResponseDTO<List<Employee>> getEmployees() {
    try {
      List<Employee> employees = employeeService.getAll();
      if (Objects.nonNull(employees)) {
        return new ResponseDTO<List<Employee>>("SUCCESS", employees, HttpStatus.OK.name());
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return new ResponseDTO<>("ERROR", null, HttpStatus.NOT_FOUND.name());
  }

  @GetMapping("/employees/{employeeId}")
  public ResponseDTO<Employee> getEmployeeById(@PathVariable int employeeId) {
    try {
      Employee employee = employeeService.getById(employeeId);
      if (Objects.nonNull(employee)) {
        return new ResponseDTO<>("SUCCESS", employee, HttpStatus.OK.name());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ResponseDTO<>("ERROR", null, HttpStatus.NOT_FOUND.name());
  }

  @DeleteMapping("/employees/{employeeId}")
  public ResponseDTO deleteEmployee(@PathVariable Integer employeeId) {
    try {
      Employee employee = employeeService.getById(employeeId);
      if (Objects.nonNull(employee)) {
        employeeService.deleteById(employeeId);
        return new ResponseDTO("SUCCESS", HttpStatus.OK.name());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ResponseDTO("ERROR", HttpStatus.NOT_FOUND.name());
  }

  @PostMapping("/employees")
  public ResponseDTO<Employee> addNewEmployee(@RequestBody EmployeeDTO employee) {
    String password = "password1.com";
    String hashPWD = CommonHelper.GenerateBCryptEncoder(password);
    log.info("HASHED PASSWORD: " + hashPWD);
    Employee newEmployee = new Employee();
//    newEmployee.setEmployeeLevel(employee.getEmployeeLevel());
    newEmployee.setFirstName(employee.getFirstName());
    newEmployee.setLastName(employee.getLastName());
    newEmployee.setPhoneNo(employee.getPhoneNo());
    newEmployee.setEmail(employee.getEmail());
    newEmployee.setDepartment(employee.getDepartment());
    newEmployee.setPassword(hashPWD);
    try {
      if (Objects.nonNull(newEmployee) && isValidEmailAddress(newEmployee.getEmail())) {
        Employee emp = employeeService.create(newEmployee);
        return new ResponseDTO<Employee>("SUCCESS", emp, HttpStatus.CREATED.name());
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO<Employee>(HttpStatus.BAD_REQUEST.name(), null, "EMPLOYEE_NOT_ADDED");
  }

  @PutMapping(value = "/employees/{employeeId}")
  public ResponseDTO<Employee> updateEmployee(
      @RequestBody EmployeeDTO updateEmployee, @PathVariable int employeeId) {
    if (Objects.nonNull(employeeId) && Objects.nonNull(updateEmployee)) {
      try {
        Employee employee = employeeService.getById(employeeId);
        if (Objects.nonNull(employee)) {
          Employee e = employeeService.update(employeeId, updateEmployee);
          return new ResponseDTO<>(HttpStatus.CREATED.name(), e, SUCCESS);
        }
      } catch (Exception e) {
        log.error(e.getMessage());
        e.printStackTrace();
      }
    }
    return new ResponseDTO<>(HttpStatus.EXPECTATION_FAILED.name(), null, ERROR);
  }

  @PutMapping(value = "/employees/{employeeId}/changePassword")
  public ResponseDTO<Object> selfChangePassword(
          @PathVariable("employeeId") int employeeId, @RequestBody ChangePasswordDTO changePasswordDTO) {
    Employee user = employeeService.findEmployeeById(employeeId);
    if (Objects.isNull(user))
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), "USER DOES NOT EXIST", ERROR);

    boolean isPasswordValid =
            MatchBCryptPassword(user.getPassword(), changePasswordDTO.getOldPassword());

    if (isPasswordValid
            && changePasswordDTO.getNewPassword().length() > 5
            && user.getEnabled()) {
      String encodedNewPassword = bCryptPasswordEncoder.encode(changePasswordDTO.getNewPassword());
      user.setPassword(encodedNewPassword);
      employeeRepository.save(user);
      return new ResponseDTO<>(HttpStatus.OK.name(), null, "PASSWORD WAS SUCCESSFULLY CHANGED");
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

//  @PostMapping(value = "/signup")
//  public ResponseDTO<Employee> registerEmployee(@RequestBody EmployeeDTO employeeDTO) {
//    try{
//      Employee employee = authService.register(employeeDTO);
//      return new ResponseDTO<>(HttpStatus.CREATED.name(), employee, SUCCESS);
//    }
//    catch (Exception e) {
//      log.error(e.getMessage());
//    }
//    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
//  }

}
