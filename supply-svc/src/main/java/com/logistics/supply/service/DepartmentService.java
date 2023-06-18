package com.logistics.supply.service;

import com.logistics.supply.dto.DepartmentDto;
import com.logistics.supply.exception.DepartmentNotFoundException;
import com.logistics.supply.model.Department;
import com.logistics.supply.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
  public Department add(DepartmentDto departmentDto) {

    log.info("Adding department {}", departmentDto);
    Department newDepartment = new Department();
    newDepartment.setDescription(departmentDto.getDescription());
    newDepartment.setName(departmentDto.getName());
    return departmentRepository.save(newDepartment);
  }

  @Cacheable(value = "allDepartment")
  public List<Department> getAll() {

    log.info("Fetch all department");
    return departmentRepository.findAll();
  }

  @Cacheable(value = "departmentById", key = "#departmentId")
  public Department getById(int departmentId) {

    log.info("Fetch department with id {}", departmentId);
    return departmentRepository
        .findById(departmentId)
        .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
  }

  @CacheEvict(
      value = {"allDepartment", "departmentById"},
      allEntries = true)
  public void delete(int departmentId) {

    log.info("Delete department with id: {}", departmentId);
    departmentRepository.deleteById(departmentId);
  }

  @CacheEvict(
      value = {"allDepartment", "departmentById"},
      allEntries = true)
  public Department update(int departmentId, DepartmentDto departmentDTO) {

    log.info("Update department with id: {} with values: {}", departmentId, departmentDTO);
    Department department = getById(departmentId);
    department.setName(departmentDTO.getName());
    department.setDescription(departmentDTO.getDescription());
    return departmentRepository.save(department);
  }
}
