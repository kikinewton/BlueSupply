package com.logistics.supply.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.logistics.supply.dto.RequestCategoryDTO;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.RequestCategory;
import com.logistics.supply.repository.RequestCategoryRepository;
import java.util.List;
import java.util.Objects;

import static com.logistics.supply.util.Constants.CATEGORY_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestCategoryService {
  private final RequestCategoryRepository requestCategoryRepository;

  @CacheEvict(
      value = {"requestCategory", "categoryById"},
      allEntries = true)
  public RequestCategory add(RequestCategory requestCategory) throws GeneralException {
    try {
      return requestCategoryRepository.save(requestCategory);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    throw new GeneralException(CATEGORY_NOT_FOUND, HttpStatus.BAD_REQUEST);
  }

  @CachePut(value = "requestCategory")
  public RequestCategory update(int id, RequestCategoryDTO requestCategory)
      throws GeneralException {
    try {
      RequestCategory category = findById(id);
      if (Objects.nonNull(requestCategory.getName())) category.setName(requestCategory.getName());
      if (Objects.nonNull(requestCategory.getDescription()))
        category.setDescription(requestCategory.getDescription());
      return requestCategoryRepository.save(category);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException(CATEGORY_NOT_FOUND, HttpStatus.BAD_REQUEST);
  }

  @Cacheable(value = "categoryById", key = "#requestCategoryId")
  public RequestCategory findById(int requestCategoryId) throws GeneralException {
    return requestCategoryRepository
        .findById(requestCategoryId)
        .orElseThrow(() -> new GeneralException(CATEGORY_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  @Cacheable(value = "requestCategory", unless = "#result.isEmpty() == true")
  public List<RequestCategory> findAll() {
    return requestCategoryRepository.findAll();
  }
}
