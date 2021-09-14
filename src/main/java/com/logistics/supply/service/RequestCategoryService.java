package com.logistics.supply.service;

import com.logistics.supply.model.RequestCategory;
import com.logistics.supply.repository.RequestCategoryRepository;
import com.logistics.supply.repository.RequestItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class RequestCategoryService  {

  @Autowired
  RequestCategoryRepository requestCategoryRepository;

  public RequestCategory add(RequestCategory requestCategory) {
    try {
      return requestCategoryRepository.save(requestCategory);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public RequestCategory findById(int requestCategoryId) {
    try {
      return requestCategoryRepository.findById(requestCategoryId).get();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public List<RequestCategory> findAll() {
    try {
      return requestCategoryRepository.findAll();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
