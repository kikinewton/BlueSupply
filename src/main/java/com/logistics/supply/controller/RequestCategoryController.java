package com.logistics.supply.controller;

import com.logistics.supply.dto.RequestCategoryDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.RequestCategory;
import com.logistics.supply.service.RequestCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.logistics.supply.util.Constants.FETCH_SUCCESSFUL;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RequestCategoryController {

  private final RequestCategoryService requestCategoryService;

  @GetMapping(value = "/requestCategory")
  public ResponseEntity<?> getAllRequestCategories() {
    List<RequestCategory> categories = requestCategoryService.findAll();
    return ResponseDTO.wrapSuccessResult(categories, FETCH_SUCCESSFUL);
  }

  @PutMapping(value = "/requestCategory/{categoryId}")
  public ResponseEntity<?> updateRequestCategory(
      @Valid @RequestBody RequestCategoryDTO requestCategory,
      @PathVariable("categoryId") int categoryId)
      throws GeneralException {
    RequestCategory category = requestCategoryService.update(categoryId, requestCategory);
    return ResponseDTO.wrapSuccessResult(category, "UPDATE SUCCESSFUL");
  }

  @PostMapping(value = "/requestCategory")
  public ResponseEntity<?> addRequestCategory(
      @Valid @RequestBody RequestCategoryDTO requestCategory) throws GeneralException {
    RequestCategory category = new RequestCategory();
    category.setName(requestCategory.getName());
    category.setDescription(requestCategory.getDescription());
    RequestCategory result = requestCategoryService.add(category);
    return ResponseDTO.wrapSuccessResult(result, FETCH_SUCCESSFUL);
  }

  @GetMapping(value = "requestCategory/{requestCategoryId}")
  public ResponseEntity<?> findRequestCategoryById(
      @PathVariable("requestCategoryId") int requestCategoryId) throws GeneralException {
    RequestCategory category = requestCategoryService.findById(requestCategoryId);
    return ResponseDTO.wrapSuccessResult(category, FETCH_SUCCESSFUL);
  }
}
