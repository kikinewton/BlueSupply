package com.logistics.supply.service;

import com.logistics.supply.dto.EmployeeDTO;
import com.logistics.supply.model.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class EmployeeService extends AbstractDataService {

    public List<Employee> getAll() {
        log.info("Get all employees");
        List<Employee> employees = new ArrayList<>();
        try {
            List<Employee> employeeList = employeeRepository.findAll();
            employeeList.forEach(employees::add);
        }
        catch (Exception e) {
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

    public Employee update(int employeeId, EmployeeDTO updatedEmployee) {
        Employee employee = getById(employeeId);
        employee.setEmail(updatedEmployee.getEmail());
        employee.setFirstName(updatedEmployee.getFirstName());
        employee.setLastName(updatedEmployee.getLastName());
        employee.setPhoneNo(updatedEmployee.getPhoneNo());
        employee.setEnabled(updatedEmployee.getEnabled());
        employee.setUpdatedAt(new Date());
        employee.setEmployeeLevel(updatedEmployee.getEmployeeLevel());
        employee.setDepartment(updatedEmployee.getDepartment());
        try {

            Employee saved = employeeRepository.save(employee);
            return saved;
        }
        catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


}
