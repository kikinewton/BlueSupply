package com.logistics.supply.controller;

import com.logistics.supply.dto.EmployeeDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.Employee;
import com.logistics.supply.service.AbstractRestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

import static com.logistics.supply.util.CommonHelper.isValidEmailAddress;

@RestController
@Slf4j
@RequestMapping("/api")
public class EmployeeController extends AbstractRestService {

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
            if(Objects.nonNull(employee)) {
                return new ResponseDTO<>("SUCCESS", employee, HttpStatus.OK.name());
            }
        }
        catch (Exception e) {
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
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseDTO("ERROR", HttpStatus.NOT_FOUND.name());
    }

    @PostMapping("/employees")
    public ResponseDTO<Employee> addNewEmployee(@RequestBody EmployeeDTO employee) {
        Employee newEmployee = new Employee();
        newEmployee.setEmployeeLevel(employee.getEmployeeLevel());
        newEmployee.setEnabled(employee.getEnabled());
        newEmployee.setFirstName(employee.getFirstName());
        newEmployee.setLastName(employee.getLastName());
        newEmployee.setPhoneNo(employee.getPhoneNo());
        newEmployee.setEmail(employee.getEmail());
        newEmployee.setDepartment(employee.getDepartment());
        try {
            if (Objects.nonNull(newEmployee) && isValidEmailAddress(newEmployee.getEmail())) {
                Employee emp = employeeService.create(newEmployee);
                return new ResponseDTO<Employee>("SUCCESS", emp, HttpStatus.CREATED.name());
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return new ResponseDTO<Employee>("ERROR", null, "EMPLOYEE_NOT_ADDED");
    }

    @PutMapping(value = "/employees/{employeeId}")
    public ResponseDTO<Employee> updateEmployee(@RequestBody EmployeeDTO updateEmployee, @PathVariable int employeeId) {
        if (Objects.nonNull(employeeId) && Objects.nonNull(updateEmployee)) {
            try {
                Employee employee = employeeService.getById(employeeId);
                if (Objects.nonNull(employee)) {
                    Employee e = employeeService.update(employeeId, updateEmployee);
                    return new ResponseDTO<>(HttpStatus.CREATED.name(), e, "SUCCESS");
                }
            }
            catch (Exception e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }
        }
        return new ResponseDTO<>(HttpStatus.EXPECTATION_FAILED.name(), null, "ERROR");
    }
}
