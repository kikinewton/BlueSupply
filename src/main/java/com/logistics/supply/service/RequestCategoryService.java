package com.logistics.supply.service;

import com.logistics.supply.dto.RequestCategoryDTO;
import com.logistics.supply.model.RequestCategory;
import com.logistics.supply.repository.RequestCategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class RequestCategoryService {

  @Autowired RequestCategoryRepository requestCategoryRepository;

  public RequestCategory add(RequestCategory requestCategory) {
    try {
      return requestCategoryRepository.save(requestCategory);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public RequestCategory update(int id, RequestCategoryDTO requestCategory) {
    try {
      Optional<RequestCategory> rc =
          Optional.ofNullable(findById(id))
              .map(
                  r -> {
                    if (Objects.nonNull(requestCategory.getName()))
                      r.setName(requestCategory.getName());
                    if (Objects.nonNull(requestCategory.getDescription()))
                      r.setDescription(requestCategory.getDescription());
                    return requestCategoryRepository.save(r);
                  });
      if (rc.isPresent()) return rc.get();
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public RequestCategory findById(int requestCategoryId) {
    try {
      return requestCategoryRepository.findById(requestCategoryId).get();
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public List<RequestCategory> findAll() {
    try {
      return requestCategoryRepository.findAll();
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }
}
