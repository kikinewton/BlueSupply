package com.logistics.supply.controller;

import com.logistics.supply.dto.DepartmentDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.Department;
import com.logistics.supply.service.AbstractRestService;
import com.logistics.supply.service.DepartmentService;
import com.logistics.supply.util.CommonHelper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.jaxb.hbm.internal.CacheAccessTypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.security.PublicKey;
import java.util.*;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

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
    return new ResponseDTO<Department>(
        HttpStatus.EXPECTATION_FAILED.name(), null, "DEPARTMENT_NOT_ADDED");
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
  public ResponseDTO<Department> getDepartmentById(@PathVariable("departmentId") int departmentId) {
    if (Objects.nonNull(departmentId)) {
      try {
        Department dep = departmentService.getById(departmentId);
        return new ResponseDTO(HttpStatus.OK.name(), dep, "DEPARTMENT_FOUND");
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

  @PutMapping("departments/{departmentId}")
  public ResponseDTO<Department> updateDepartment(
      @PathVariable("departmentId") int departmentId, @RequestBody DepartmentDTO departmentDTO) {
    Department department = departmentService.getById(departmentId);
    if (Objects.isNull(department)) return new ResponseDTO<>(ERROR, null, "DEPARTMENT NOT FOUND");

    String[] nullValues = CommonHelper.getNullPropertyNames(departmentDTO);
    System.out.println("count of null properties: " + Arrays.stream(nullValues).count());

    Set<String> l = new HashSet<>(Arrays.asList(nullValues));
    if (l.size() > 0) {
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, "ERROR");
    }

    try {
      Department update = departmentService.update(departmentId, departmentDTO);
      return new ResponseDTO<>(SUCCESS, update, "DEPARTMENT UPDATED");
    } catch (Exception e) {
      log.error("Update failed", e);
      e.printStackTrace();
    }
    return new ResponseDTO<>(ERROR, null, "DEPARTMENT NOT UPDATED");
  }
}
