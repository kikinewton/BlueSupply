package com.logistics.supply.controller;

import com.logistics.supply.dto.RequestCategoryDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.RequestCategory;
import com.logistics.supply.service.AbstractRestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@Slf4j
@RestController
@RequestMapping("/api")
public class RequestCategoryController extends AbstractRestService {

  @GetMapping(value = "/requestCategory")
  public ResponseEntity<?> getAllRequestCategories() {
    try {
      List<RequestCategory> categories = new ArrayList<>(requestCategoryService.findAll());
      ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, categories);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("FETCH_FAILED");
  }

  @PutMapping(value = "/requestCategory/{categoryId}")
  public ResponseEntity<?> updateRequestCategory(
      @Valid @RequestBody RequestCategoryDTO requestCategory,
      @PathVariable("categoryId") int categoryId) {
    try {
      RequestCategory category = requestCategoryService.update(categoryId, requestCategory);
      if (Objects.nonNull(category)) {
        ResponseDTO response = new ResponseDTO("UPDATE_SUCCESSFUL", SUCCESS, category);
        return ResponseEntity.ok(response);
      }
    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("UPDATE_FAILED");
  }

  @PostMapping(value = "/requestCategory")
  public ResponseEntity<?> addRequestCategory(
      @Valid @RequestBody RequestCategoryDTO requestCategory) {
    RequestCategory category = new RequestCategory();
    try {
      category.setName(requestCategory.getName());
      category.setDescription(requestCategory.getDescription());
      RequestCategory result = requestCategoryService.add(category);
      if (Objects.nonNull(result)) {
        ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, result);
        return ResponseEntity.ok(response);
      }

    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("ADD_FAILED");
  }

  @GetMapping(value = "requestCategory/{requestCategoryId}")
  public ResponseEntity<?> findRequestCategoryById(
      @PathVariable("requestCategoryId") int requestCategoryId) {
    RequestCategory category = requestCategoryService.findById(requestCategoryId);
    if (Objects.nonNull(category)) {
      ResponseDTO response = new ResponseDTO<>("FETCH_REQUEST_CATEGORIES", SUCCESS, category);
      return ResponseEntity.ok(response);
    }
    return failedResponse("FETCH_FAILED");
  }

  private ResponseEntity<ResponseDTO> failedResponse(String message) {
    ResponseDTO failed = new ResponseDTO(message, ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
