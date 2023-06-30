package com.logistics.supply.controller;

import com.logistics.supply.dto.RequestCategoryDto;
import com.logistics.supply.dto.ResponseDto;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.service.RequestCategoryService;
import com.logistics.supply.model.RequestCategory;
import com.logistics.supply.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RequestCategoryController {

  private final RequestCategoryService requestCategoryService;

  @GetMapping(value = "/requestCategory")
  public ResponseEntity<?> getAllRequestCategories() {

    List<RequestCategory> categories = requestCategoryService.findAll();
    return ResponseDto.wrapSuccessResult(categories, Constants.FETCH_SUCCESSFUL);
  }

  @PutMapping(value = "/requestCategory/{categoryId}")
  public ResponseEntity<?> updateRequestCategory(
      @Valid @RequestBody RequestCategoryDto requestCategory,
      @PathVariable("categoryId") int categoryId)
      throws GeneralException {
    RequestCategory category = requestCategoryService.update(categoryId, requestCategory);
    return ResponseDto.wrapSuccessResult(category, "UPDATE SUCCESSFUL");
  }

  @PostMapping(value = "/requestCategory")
  public ResponseEntity<?> addRequestCategory(
      @Valid @RequestBody RequestCategoryDto requestCategory) throws GeneralException {
    RequestCategory category = new RequestCategory();
    category.setName(requestCategory.getName());
    category.setDescription(requestCategory.getDescription());
    RequestCategory result = requestCategoryService.add(category);
    return ResponseDto.wrapSuccessResult(result, Constants.FETCH_SUCCESSFUL);
  }

  @GetMapping(value = "requestCategory/{requestCategoryId}")
  public ResponseEntity<?> findRequestCategoryById(
      @PathVariable("requestCategoryId") int requestCategoryId) throws GeneralException {
    RequestCategory category = requestCategoryService.findById(requestCategoryId);
    return ResponseDto.wrapSuccessResult(category, Constants.FETCH_SUCCESSFUL);
  }
}
