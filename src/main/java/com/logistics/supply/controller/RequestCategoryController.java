package com.logistics.supply.controller;

import com.logistics.supply.dto.RequestCategoryDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.RequestCategory;
import com.logistics.supply.service.AbstractRestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
  public ResponseDTO<List<RequestCategory>> getAllRequestCategories() {
    try {
      List<RequestCategory> categories = new ArrayList<>(requestCategoryService.findAll());
      return new ResponseDTO<>(HttpStatus.OK.name(), categories, SUCCESS);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

  @PostMapping(value = "/requestCategory")
  public ResponseDTO<RequestCategory> addRequestCategory(RequestCategoryDTO requestCategory) {
    RequestCategory category = new RequestCategory();
    category.setDescription(requestCategory.getDescription());
    category.setName(requestCategory.getName());
    try {
      RequestCategory result = requestCategoryService.create(category);
      return new ResponseDTO<>(HttpStatus.OK.name(), result, SUCCESS);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

  @GetMapping(value = "requestCategory/{requestCategoryId}")
  public ResponseDTO<RequestCategory> findRequestCategoryById(
      @PathVariable("requestCategoryId") int requestCategoryId) {
    RequestCategory category = requestCategoryService.findById(requestCategoryId);
    if (Objects.nonNull(category))
      return new ResponseDTO<>(HttpStatus.OK.name(), category, SUCCESS);
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

}
