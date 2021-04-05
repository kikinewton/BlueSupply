package com.logistics.supply.service;

import com.logistics.supply.model.RequestCategory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequestCategoryService extends AbstractDataService {

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
