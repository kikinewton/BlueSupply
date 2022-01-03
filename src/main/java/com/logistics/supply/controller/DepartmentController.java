package com.logistics.supply.controller;

import com.logistics.supply.dto.DepartmentDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.Department;
import com.logistics.supply.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

import static com.logistics.supply.util.Constants.SUCCESS;
import static com.logistics.supply.util.Helper.failedResponse;

@Slf4j
@RestController
@RequestMapping(value = "/api")
public class DepartmentController {

  @Autowired DepartmentService departmentService;

  @PostMapping(value = "/departments")
  public ResponseEntity<?> addDepartment(@RequestBody DepartmentDTO department) {

    Department newDepartment = new Department();
    newDepartment.setDescription(department.getDescription());
    newDepartment.setName(department.getName());
    try {
      Department result = departmentService.add(newDepartment);
      if (Objects.nonNull(result)) {
        ResponseDTO response = new ResponseDTO("DEPARTMENT_ADDED", SUCCESS, result);
        return ResponseEntity.ok(response);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("DEPARTMENT_NOT_ADDED");
  }

  @GetMapping(value = "/departments")
  public ResponseEntity<?> getAllDepartments() {
    List<Department> departmentList = departmentService.getAll();
    ResponseDTO response = new ResponseDTO("FETCH_ALL_DEPARTMENTS", SUCCESS, departmentList);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/departments/{departmentId}")
  public ResponseEntity<?> getDepartmentById(@PathVariable("departmentId") int departmentId) {
    if (Objects.nonNull(departmentId)) {
      try {
        Department dep = departmentService.getById(departmentId);
        ResponseDTO response = new ResponseDTO("DEPARTMENT_FOUND", SUCCESS, dep);
        return ResponseEntity.ok(response);
      } catch (Exception e) {
        log.error(e.getMessage());
      }
    }
    return failedResponse("GET_DEPARTMENT_FAILED");
  }

  @DeleteMapping(value = "/departments/{departmentId}")
  public ResponseEntity<?> deleteDepartment(@PathVariable("departmentId") int departmentId) {
    try {
      departmentService.delete(departmentId);
      ResponseDTO response = new ResponseDTO("DEPARTMENT_DELETED", SUCCESS, null);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("DELETE_FAILED");
  }

  @PutMapping("departments/{departmentId}")
  public ResponseEntity<?> updateDepartment(
      @PathVariable("departmentId") int departmentId,
      @RequestBody @Valid DepartmentDTO departmentDTO) {

    try {
      Department update = departmentService.update(departmentId, departmentDTO);
      ResponseDTO response = new ResponseDTO("DEPARTMENT_UPDATED", SUCCESS, update);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("DEPARTMENT_NOT_UPDATED");
  }


}
