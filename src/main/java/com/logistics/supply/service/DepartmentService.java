package com.logistics.supply.service;

import com.logistics.supply.dto.DepartmentDTO;
import com.logistics.supply.model.Department;
import com.logistics.supply.repository.DepartmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DepartmentService {

  @Autowired DepartmentRepository departmentRepository;

  public Department add(Department newDepartment) {
    try {
      return departmentRepository.save(newDepartment);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public List<Department> getAll() {
    try {
      return departmentRepository.findAll();
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return new ArrayList<>();
  }

  public Department getByName(String name) {
    Department dep = departmentRepository.findByName(name);
    return dep;
  }

  public Department getById(int departmentId) {
    try {
      Optional<Department> department = departmentRepository.findById(departmentId);
      return department.orElseThrow(Exception::new);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public void delete(int departmentId) {
    log.info("Delete department with id: " + departmentId);
    try {
      departmentRepository.deleteById(departmentId);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  public Department update(int departmentId, DepartmentDTO departmentDTO) {
    Department department = getById(departmentId);
    department.setName(departmentDTO.getName());
    department.setDescription(departmentDTO.getDescription());
    log.info("Update department with id: " + departmentId);
    try {
      return departmentRepository.save(department);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return department;
  }
}
