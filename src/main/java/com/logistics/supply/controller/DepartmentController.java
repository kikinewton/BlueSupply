package com.logistics.supply.controller;

import com.logistics.supply.dto.DepartmentDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.Department;
import com.logistics.supply.service.AbstractRestService;
import com.logistics.supply.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping(value = "/api")
public class DepartmentController extends AbstractRestService {


    @PostMapping(value = "/departments")
    public ResponseDTO<Department> addDepartment(@RequestBody DepartmentDTO department) {
        Department newDepartment = new Department();
        newDepartment.setDescription(department.getDescription());
        newDepartment.setName(department.getName());
        try {
            Department result = departmentService.add(newDepartment);
            if (Objects.nonNull(result)) {
                return new ResponseDTO<Department>(HttpStatus.CREATED.name(), result, "DEPARTMENT_ADDED");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseDTO<Department>(HttpStatus.EXPECTATION_FAILED.name(), null, "DEPARTMENT_NOT_ADDED");
    }

    @GetMapping(value = "/departments")
    public ResponseDTO getAllDepartments() {
        try {
            List<Department> departmentList = departmentService.getAll();
            if (Objects.nonNull(departmentList)) {
                return new ResponseDTO(HttpStatus.OK.name(), departmentList, "DEPARTMENTS_FOUND");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseDTO("DEPARTMENT_NOT_FOUND", "ERROR");
    }

    @GetMapping(value = "/departments/{departmentId}")
    public ResponseDTO getDepartmentById(@PathVariable("departmentId") int departmentId) {
        if (Objects.nonNull(departmentId)) {
            try {
                Department dep = departmentService.getById(departmentId);
                return new ResponseDTO("DEPARTMENT_FOUND", HttpStatus.OK.name(), dep.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ResponseDTO("GET_DEPARTMENT_FAILED", "ERROR");
    }

    @DeleteMapping(value = "/departments/{departmentId}")
    public ResponseDTO deleteDepartment(@PathVariable("departmentId") int departmentId) {
        log.info("Delete department with id: " + departmentId);
        if (Objects.nonNull(departmentId)) {
            try {
                departmentService.delete(departmentId);
                return new ResponseDTO("DEPARTMENT_DELETED", "SUCCESS");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new ResponseDTO("DELETE_FAILED", "ERROR");
        }
        return new ResponseDTO("DELETE_FAILED", "ERROR");
    }


}
