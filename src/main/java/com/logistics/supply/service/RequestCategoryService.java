package com.logistics.supply.service;

import com.logistics.supply.model.RequestCategory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class RequestCategoryService extends AbstractDataService {

  public RequestCategory create(RequestCategory requestCategory) {
    return requestCategoryRepository.save(requestCategory);
  }

  public RequestCategory findById(int requestCategoryId) {
    return requestCategoryRepository
        .findById(requestCategoryId)
        .orElseThrow(() -> new NoSuchElementException("Request category does not exist"));
  }

  public List<RequestCategory> findAll() {
    return requestCategoryRepository.findAll();
  }
}
