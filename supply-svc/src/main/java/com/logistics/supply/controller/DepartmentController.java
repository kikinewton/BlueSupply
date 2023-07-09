package com.logistics.supply.controller;

import com.logistics.supply.dto.DepartmentDto;
import com.logistics.supply.dto.ResponseDto;
import com.logistics.supply.model.Department;
import com.logistics.supply.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api")
public class DepartmentController {

  private final DepartmentService departmentService;

  @PostMapping(value = "/departments")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<ResponseDto<Department>> addDepartment(
          @RequestBody DepartmentDto department) {

    Department result = departmentService.add(department);
    return ResponseDto.wrapSuccessResult(result, "DEPARTMENT ADDED");
  }

  @GetMapping(value = "/departments")
  public ResponseEntity<ResponseDto<List<Department>>> getAllDepartments() {

    List<Department> departments = departmentService.getAll();
    return ResponseDto.wrapSuccessResult(departments, "FETCH DEPARTMENTS");
  }

  @GetMapping(value = "/departments/{departmentId}")
  public ResponseEntity<ResponseDto<Department>> getDepartmentById(
          @PathVariable("departmentId") int departmentId) {
        Department department = departmentService.getById(departmentId);
        return ResponseDto.wrapSuccessResult(department, "FETCH DEPARTMENT");
  }

  @DeleteMapping(value = "/departments/{departmentId}")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<Void> deleteDepartment(
          @PathVariable("departmentId") int departmentId) {

      departmentService.delete(departmentId);
      return new ResponseEntity<>(HttpStatus.OK);
  }

  @PutMapping("departments/{departmentId}")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<ResponseDto<Department>> updateDepartment(
      @PathVariable("departmentId") int departmentId,
      @RequestBody @Valid DepartmentDto departmentDTO) {

      Department department = departmentService.update(departmentId, departmentDTO);
      return ResponseDto.wrapSuccessResult(department, "DEPARTMENT UPDATED");
  }
}
