package com.logistics.supply.service;

import com.logistics.supply.model.Department;
import com.logistics.supply.model.PettyCash;
import com.logistics.supply.repository.PettyCashRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PettyCashService {

  @Autowired PettyCashRepository pettyCashRepository;

  public PettyCash save(PettyCash pettyCash) {
    try {
      return pettyCashRepository.save(pettyCash);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public List<PettyCash> findByDepartment(Department department) {
    try {
      return pettyCashRepository.findByDepartment(department.getId());
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return new ArrayList<>();
  }

  public PettyCash findByRef(String pettyCashRef) {
    Optional<PettyCash> result = pettyCashRepository.findByPettyCashRef(pettyCashRef);
    if (result.isPresent()) {
      return result.get();
    }
    return null;
  }

  public long count() {
    return pettyCashRepository.count() + 1 ;
  }

  public List<PettyCash> findByEmployee(int employeeId, int pageNo, int pageSize) {
    List<PettyCash> cashList = new ArrayList<>();
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("created_date").descending());
      cashList.addAll(
          pettyCashRepository.findByEmployee(employeeId, pageable).getContent());
      return cashList;
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return cashList;
  }

  public List<PettyCash> findApprovedPettyCash(int pageNo, int pageSize) {
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return pettyCashRepository.findApprovedPettyCash(pageable).getContent();
    }
    catch (Exception e) {
      log.error(e.toString());
    }
    return new ArrayList<>();
  }

  public List<PettyCash> findEndorsedPettyCash() {
    try{
      return pettyCashRepository.findEndorsedPettyCash();
    }
    catch (Exception e) {
      log.error(e.toString());
    }
    return new ArrayList<>();
  }

  public List<PettyCash> findAllPettyCash(int pageNo, int pageSize) {
    List<PettyCash> cashList = new ArrayList<>();
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      Page<PettyCash> pettyCashPage = pettyCashRepository.findAll(pageable);
      cashList.addAll(pettyCashPage.getContent());
      return cashList;
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return cashList;
  }

  public PettyCash findById(int pettyCashId) {
    return pettyCashRepository.findById(pettyCashId).orElse(null);
  }
}
