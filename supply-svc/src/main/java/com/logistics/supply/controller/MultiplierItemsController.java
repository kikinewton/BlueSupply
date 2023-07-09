package com.logistics.supply.controller;

import com.logistics.supply.dto.*;
import com.logistics.supply.model.*;
import com.logistics.supply.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@Validated
@Slf4j
@RequestMapping("/api")
@RequiredArgsConstructor
public class MultiplierItemsController {

  private final EmployeeService employeeService;
  private final RequestItemService requestItemService;
  private final PettyCashService pettyCashService;
  private final FloatOrderService floatOrderService;

  @PostMapping("/multipleRequestItems")
  public ResponseEntity<ResponseDto<List<RequestItemDto>>> addBulkRequest(
      Authentication authentication, @Valid @RequestBody MultipleItemDto multipleItemDto) {
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    List<RequestItemDto> createdItems =
        requestItemService.createRequestItem(multipleItemDto.getMultipleRequestItem(), employee);
    return ResponseDto.wrapSuccessResult(createdItems, "CREATED REQUEST ITEMS");
  }

  @PostMapping("/bulkFloat")
  public ResponseEntity<ResponseDto<FloatOrderDto>> addBulkFloat(
      Authentication authentication, @Valid @RequestBody FloatOrPettyCashDto bulkItems) {

    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    FloatOrder saveFloatOrder = floatOrderService.saveFloatOrder(bulkItems, employee);
    FloatOrderDto floatOrderDto = FloatOrderDto.toDto(saveFloatOrder);
    return ResponseDto.wrapSuccessResult(floatOrderDto, "CREATED FLOAT ITEMS");
  }

  @PostMapping("/bulkPettyCash")
  public ResponseEntity<ResponseDto<Set<PettyCash>>> addBulkPettyCash(
      Authentication authentication, @Valid @RequestBody FloatOrPettyCashDto bulkItems) {

    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    PettyCashOrder pettyCashOrder = pettyCashService.saveAll(bulkItems, employee);
    return ResponseDto.wrapSuccessResult(pettyCashOrder.getPettyCash(), "CREATED PETTY CASH ITEMS");
  }


  @Caching(
          evict = {
                  @CacheEvict(value = "requestItemsByToBeReviewed", allEntries = true),
                  @CacheEvict(value = "requestItemsHistoryByDepartment", allEntries = true)
          })
  @PutMapping(value = "requestItems/bulkEndorse")
  @PreAuthorize("hasRole('ROLE_HOD')")
  public ResponseEntity<ResponseDto<List<RequestItemDto>>> endorseRequestItems(
       @Valid @RequestBody BulkRequestItemDto bulkRequestItem) {
    List<RequestItem> requestItems =
        requestItemService.endorseBulkRequestItems(bulkRequestItem.getRequestItems());
    List<RequestItemDto> requestItemDtoList =
        requestItems.stream().map(RequestItemDto::toDto).collect(Collectors.toList());
    return ResponseDto.wrapSuccessResult(requestItemDtoList, "REQUEST ENDORSED");
  }

  @Caching(
          evict = {
                  @CacheEvict(value = "requestItemsByToBeReviewed", allEntries = true),
                  @CacheEvict(value = "requestItemsHistoryByDepartment", allEntries = true)
          })
  @PutMapping(value = "requestItems/bulkApprove")
  @PreAuthorize("hasRole('ROLE_GENERAL_MANAGER')")
  public ResponseEntity<ResponseDto<List<RequestItemDto>>> approveRequestItemsByGeneralManager(
          @Valid @RequestBody BulkRequestItemDto bulkRequestItem) {
    List<RequestItem> requestItems =
            requestItemService.approveBulkRequestItems(bulkRequestItem.getRequestItems());
    List<RequestItemDto> requestItemDtoList =
            requestItems.stream().map(RequestItemDto::toDto).collect(Collectors.toList());
    return ResponseDto.wrapSuccessResult(requestItemDtoList, "REQUEST APPROVED");
  }

  @Caching(
          evict = {
                  @CacheEvict(value = "requestItemsByToBeReviewed", allEntries = true),
                  @CacheEvict(value = "requestItemsHistoryByDepartment", allEntries = true)
          })
  @PutMapping(value = "requestItems/bulkHodReview")
  @PreAuthorize("hasRole('ROLE_HOD')")
  public ResponseEntity<ResponseDto<List<RequestItemDto>>> hodReviewRequestItems(
          @Valid @RequestBody BulkRequestItemDto bulkRequestItem) {

    Set<RequestItem> requestItems =
            requestItemService.updateBulkRequestReview(bulkRequestItem.getRequestItems());
    List<RequestItemDto> requestItemDtoList =
            requestItems.stream().map(RequestItemDto::toDto).collect(Collectors.toList());
    return ResponseDto.wrapSuccessResult(requestItemDtoList, "HOD REVIEW SUCCESSFUL");
  }

  @Caching(
          evict = {
                  @CacheEvict(value = "requestItemsByToBeReviewed", allEntries = true),
                  @CacheEvict(value = "requestItemsHistoryByDepartment", allEntries = true)
          })
  @PutMapping(value = "requestItems/bulkCancel")
  @PreAuthorize("hasRole('ROLE_HOD') or hasRole('ROLE_GENERAL_MANAGER')")
  public ResponseEntity<ResponseDto<List<CancelledRequestItem>>> cancelledRequestItems(
          Authentication authentication,
          @Valid @RequestBody BulkRequestItemDto bulkRequestItem) {

    String email = authentication.getName();

    List<CancelledRequestItem> cancelledRequestItems = requestItemService.cancelledRequestItems(email, bulkRequestItem.getRequestItems());
    return ResponseDto.wrapSuccessResult(cancelledRequestItems, "CANCELLED REQUEST");
  }

}
