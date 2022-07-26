package com.logistics.supply.service;

import com.logistics.supply.dto.DepartmentDTO;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.Department;
import com.logistics.supply.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DepartmentService {
  private final DepartmentRepository departmentRepository;

  @CacheEvict(
      value = {"allDepartment", "departmentById"},
      allEntries = true)
  public Department add(Department newDepartment) {
    return departmentRepository.save(newDepartment);
  }

  @Cacheable(value = "allDepartment")
  public List<Department> getAll() {
    return departmentRepository.findAll();
  }

  @Cacheable(value = "departmentById", key = "#departmentId")
  public Department getById(int departmentId) throws GeneralException {
    return departmentRepository
        .findById(departmentId)
        .orElseThrow(() -> new GeneralException("DEPARTMENT NOT FOUND", HttpStatus.NOT_FOUND));
  }

  @CacheEvict(
      value = {"allDepartment", "departmentById"},
      allEntries = true)
  public void delete(int departmentId) {
    departmentRepository.deleteById(departmentId);
  }

  @CacheEvict(
      value = {"allDepartment", "departmentById"},
      allEntries = true)
  public Department update(int departmentId, DepartmentDTO departmentDTO) throws GeneralException {
    Department department = getById(departmentId);
    department.setName(departmentDTO.getName());
    department.setDescription(departmentDTO.getDescription());
    try {
      return departmentRepository.save(department);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    throw new GeneralException("UPDATE DEPARTMENT FAILED", HttpStatus.BAD_REQUEST);
  }
}
