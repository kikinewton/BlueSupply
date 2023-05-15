package com.logistics.supply.controller;

import com.logistics.supply.dto.DepartmentDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.Department;
import com.logistics.supply.util.Constants;
import com.logistics.supply.util.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import com.logistics.supply.service.DepartmentService;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api")
public class DepartmentController {

  private final DepartmentService departmentService;

  @PostMapping(value = "/departments")
  public ResponseEntity<?> addDepartment(@RequestBody DepartmentDTO department) {

    Department newDepartment = new Department();
    newDepartment.setDescription(department.getDescription());
    newDepartment.setName(department.getName());
      Department result = departmentService.add(newDepartment);

        ResponseDTO response = new ResponseDTO("DEPARTMENT ADDED", Constants.SUCCESS, result);
        return ResponseEntity.ok(response);

  }

  @GetMapping(value = "/departments")
  public ResponseEntity<?> getAllDepartments() {
    List<Department> departmentList = departmentService.getAll();
    ResponseDTO response = new ResponseDTO("FETCH ALL DEPARTMENTS", Constants.SUCCESS, departmentList);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/departments/{departmentId}")
  public ResponseEntity<?> getDepartmentById(@PathVariable("departmentId") int departmentId) {
    if (Objects.nonNull(departmentId)) {
      try {
        Department dep = departmentService.getById(departmentId);
        ResponseDTO response = new ResponseDTO("DEPARTMENT FOUND", Constants.SUCCESS, dep);
        return ResponseEntity.ok(response);
      } catch (Exception e) {
        log.error(e.getMessage());
      }
    }
    return Helper.failedResponse("GET DEPARTMENT FAILED");
  }

  @DeleteMapping(value = "/departments/{departmentId}")
  public ResponseEntity<?> deleteDepartment(@PathVariable("departmentId") int departmentId) {
    try {
      departmentService.delete(departmentId);
      ResponseDTO response = new ResponseDTO("DEPARTMENT DELETED", Constants.SUCCESS, null);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return Helper.failedResponse("DELETE FAILED");
  }

  @PutMapping("departments/{departmentId}")
  public ResponseEntity<?> updateDepartment(
      @PathVariable("departmentId") int departmentId,
      @RequestBody @Valid DepartmentDTO departmentDTO) {

    try {
      Department update = departmentService.update(departmentId, departmentDTO);
      ResponseDTO response = new ResponseDTO("DEPARTMENT UPDATED", Constants.SUCCESS, update);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return Helper.failedResponse("DEPARTMENT NOT UPDATED");
  }


}
