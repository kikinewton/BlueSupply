package com.logistics.supply.service;

import com.logistics.supply.dto.RequestCategoryDto;
import com.logistics.supply.exception.RequestCategoryNotFoundException;
import com.logistics.supply.model.RequestCategory;
import com.logistics.supply.repository.RequestCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestCategoryService {
  private final RequestCategoryRepository requestCategoryRepository;

  @CacheEvict(
      value = {"requestCategoryDto", "categoryById"},
      allEntries = true)
  public RequestCategory add(RequestCategoryDto requestCategoryDto) {

    log.info("Add request requestCategory {}", requestCategoryDto);
    RequestCategory requestCategory = new RequestCategory();
    requestCategory.setName(requestCategoryDto.getName());
    requestCategory.setDescription(requestCategoryDto.getDescription());
      return requestCategoryRepository.save(requestCategory);
  }

  @CachePut(value = "requestCategory")
  public RequestCategory update(int requestCategoryId, RequestCategoryDto requestCategory) {

    log.info("Update request category with requestCategoryId: {}", requestCategoryId);
      RequestCategory category = findById(requestCategoryId);
      if (Objects.nonNull(requestCategory.getName())) category.setName(requestCategory.getName());
      if (Objects.nonNull(requestCategory.getDescription()))
        category.setDescription(requestCategory.getDescription());
      return requestCategoryRepository.save(category);
  }

  @Cacheable(value = "categoryById", key = "#requestCategoryId")
  public RequestCategory findById(int requestCategoryId) {

    log.info("Find request category with id: {}", requestCategoryId);
    return requestCategoryRepository
        .findById(requestCategoryId)
        .orElseThrow(() -> new RequestCategoryNotFoundException(requestCategoryId));
  }

  @Cacheable(value = "requestCategory", unless = "#result.isEmpty() == true")
  public List<RequestCategory> findAll() {

    log.info("Fetch all request categories");
    return requestCategoryRepository.findAll();
  }
}
