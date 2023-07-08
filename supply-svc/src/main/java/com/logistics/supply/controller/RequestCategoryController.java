package com.logistics.supply.controller;

import com.logistics.supply.dto.RequestCategoryDto;
import com.logistics.supply.dto.ResponseDto;
import com.logistics.supply.model.RequestCategory;
import com.logistics.supply.service.RequestCategoryService;
import com.logistics.supply.util.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.logistics.supply.util.Constants.UPDATE_SUCCESSFUL;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RequestCategoryController {

  private final RequestCategoryService requestCategoryService;

  @GetMapping(value = "/requestCategories")
  public ResponseEntity<ResponseDto<List<RequestCategory>>> getAllRequestCategories() {

    List<RequestCategory> categories = requestCategoryService.findAll();
    return ResponseDto.wrapSuccessResult(categories, Constants.FETCH_SUCCESSFUL);
  }

  @PutMapping(value = "/requestCategories/{categoryId}")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<ResponseDto<RequestCategory>> updateRequestCategory(
      @Valid @RequestBody RequestCategoryDto requestCategory,
      @PathVariable("categoryId") int categoryId) {

    RequestCategory category = requestCategoryService.update(categoryId, requestCategory);
    return ResponseDto.wrapSuccessResult(category, UPDATE_SUCCESSFUL);
  }

  @PostMapping(value = "/requestCategories")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<ResponseDto<RequestCategory>> addRequestCategory(
      @Valid @RequestBody RequestCategoryDto requestCategory) {

    RequestCategory result = requestCategoryService.add(requestCategory);
    return ResponseDto.wrapSuccessResult(result, "REQUEST CATEGORY CREATED");
  }

  @GetMapping(value = "requestCategories/{requestCategoryId}")
  public ResponseEntity<ResponseDto<RequestCategory>> findRequestCategoryById(
      @PathVariable("requestCategoryId") int requestCategoryId)  {

    RequestCategory category = requestCategoryService.findById(requestCategoryId);
    return ResponseDto.wrapSuccessResult(category, Constants.FETCH_SUCCESSFUL);
  }
}
