package com.logistics.supply.controller;

import com.logistics.supply.dto.ItemUpdateDTO;
import com.logistics.supply.dto.PagedResponseDTO;
import com.logistics.supply.dto.RequestItemDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.TrackRequestDTO;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.RequestItemService;
import com.logistics.supply.service.TrackRequestStatusService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.FETCH_SUCCESSFUL;
import static com.logistics.supply.util.Constants.SUCCESS;
import static com.logistics.supply.util.Helper.failedResponse;

@RestController
@RequestMapping(value = "/api")
@CrossOrigin(
    origins = {
      "https://etornamtechnologies.github.io/skyblue-request-frontend-react",
      "http://localhost:4000"
    },
    allowedHeaders = "*")
@RequiredArgsConstructor
public class RequestItemController {
  private final RequestItemService requestItemService;
  private final EmployeeService employeeService;
  private final TrackRequestStatusService trackRequestStatusService;

  @GetMapping(value = "/requestItems")
  @PreAuthorize("hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_ADMIN')")
  public ResponseEntity<?> listRequestItems(
      @RequestParam(defaultValue = "0", required = false) int pageNo,
      @RequestParam(defaultValue = "300", required = false) int pageSize,
      @RequestParam(required = false, defaultValue = "false") Optional<Boolean> toBeApproved,
      @RequestParam(required = false, defaultValue = "false") Boolean approved) {
    List<RequestItem> items = new ArrayList<>();

    if (approved) {

      items.addAll(requestItemService.getApprovedItems());
      return ResponseDTO.wrapSuccessResult(items, FETCH_SUCCESSFUL);
    }
    if (toBeApproved.isPresent() && toBeApproved.get()) {

      items.addAll(requestItemService.getEndorsedItemsWithAssignedSuppliers());
      return ResponseDTO.wrapSuccessResult(items, FETCH_SUCCESSFUL);
    }

    items.addAll(requestItemService.findAll(pageNo, pageSize));

    List<RequestItem> sortedResult = items.stream()
            .sorted(Comparator.comparing(RequestItem::getId))
            .collect(Collectors.toList());
    return ResponseDTO.wrapSuccessResult(sortedResult, FETCH_SUCCESSFUL);
  }

  @GetMapping(value = "/requestItems/{requestItemId}")
  public ResponseEntity<?> getRequestItemById(@PathVariable int requestItemId) {

    Optional<RequestItem> item = requestItemService.findById(requestItemId);
    if (item.isPresent()) {
      ResponseDTO response = new ResponseDTO("REQUEST ITEM FOUND", SUCCESS, item.get());
      return ResponseEntity.ok(response);
    }

    return failedResponse("REQUEST ITEM NOT FOUND");
  }

  @GetMapping(value = "/requestItemsByDepartment")
  @PreAuthorize("hasRole('ROLE_HOD')")
  public ResponseEntity<?> listRequestItemsByDepartment(
      Authentication authentication,
      @RequestParam(required = false, defaultValue = "false") Boolean toBeReviewed) {
    if (Objects.isNull(authentication)) return failedResponse("Auth token is required");

    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    if (toBeReviewed) {
      List<RequestItemDTO> requestItemsDtoToBeReviewed = requestItemService.findRequestItemsDtoToBeReviewed(
              RequestReview.PENDING, employee.getDepartment().getId());
      return ResponseDTO.wrapSuccessResult(requestItemsDtoToBeReviewed, FETCH_SUCCESSFUL);
    }

    List<RequestItemDTO> items = requestItemService.getRequestItemForHOD(employee.getDepartment().getId());
    return ResponseDTO.wrapSuccessResult(items, FETCH_SUCCESSFUL);
  }

  @Operation(
      summary =
          "Get the list of endorsed items for department by HOD, with params get the request_items with assigned final supplier")
  @GetMapping(value = "/requestItemsByDepartment/endorsed")
  @PreAuthorize("hasRole('ROLE_HOD')")
  public ResponseEntity<?> listEndorsedRequestItemsForDepartment(
      Authentication authentication,
      @RequestParam(required = false, defaultValue = "false") Boolean review,
      @RequestParam(required = false) String quotationId) {
    if (Objects.isNull(authentication)) return failedResponse("Auth token is required");

    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());

    if (review && Objects.nonNull(quotationId)) {
      List<RequestItem> items =
          requestItemService.getItemsWithFinalPriceUnderQuotation(Integer.parseInt(quotationId));
      return ResponseDTO.wrapSuccessResult(items, "ENDORSED ITEMS WITH PRICES FROM SUPPLIER");
    }
    List<RequestItem> items =
        requestItemService.getEndorsedRequestItemsForDepartment(employee.getDepartment().getId());
    return ResponseDTO.wrapSuccessResult(items, "ENDORSED REQUEST ITEM");
  }

  @Operation(summary = "Get the list of endorsed items for procurement to work on")
  @GetMapping("/requestItems/endorsed")
  @PreAuthorize(" hasRole('ROLE_PROCUREMENT_MANAGER') or hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseEntity<?> listAllEndorsedRequestItems(
      Authentication authentication,
      @RequestParam(required = false, defaultValue = "false") Boolean withSupplier) {
    List<RequestItem> items = new ArrayList<>();

    if (withSupplier) {
      items.addAll(requestItemService.getEndorsedItemsWithSuppliers());
      ResponseDTO response = new ResponseDTO("ENDORSED REQUEST ITEMS", SUCCESS, items);
      return ResponseEntity.ok(response);
    }

    items.addAll(requestItemService.getEndorsedItemsWithoutSuppliers());
    return ResponseDTO.wrapSuccessResult(items, "ENDORSED REQUEST ITEMS");

  }

  @GetMapping(value = "/requestItemsForEmployee")
  public ResponseEntity<?> listRequestItemsForEmployee(
      Authentication authentication,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize) {
    if (Objects.isNull(authentication)) return failedResponse("Auth token is required");

    List<RequestItemDTO> items = new ArrayList<>();
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    items.addAll(requestItemService.findByEmployee(employee, pageNo, pageSize));
    return ResponseDTO.wrapSuccessResult(items, FETCH_SUCCESSFUL);
  }

  @Operation(summary = "Change quantity or name of items requested", tags = "REQUEST ITEM")
  @PutMapping(value = "/requestItems/{requestItemId}")
  public ResponseEntity<?> updateQuantityForNotEndorsedRequest(
      @PathVariable("requestItemId") int requestItemId,
      @Valid @RequestBody ItemUpdateDTO itemUpdateDTO,
      Authentication authentication)
      throws Exception {
    if (Objects.isNull(authentication)) return failedResponse("Auth token required");
    String email = authentication.getName();
    boolean requestItemExist =
        requestItemService
            .findById(requestItemId)
            .filter(
                x ->
                    x.getEmployee().getEmail().equalsIgnoreCase(email)
                        && x.getStatus().equals(RequestStatus.COMMENT))
            .isPresent();
    if (requestItemExist) {
      RequestItem result = requestItemService.updateItemQuantity(requestItemId, itemUpdateDTO);
      return ResponseDTO.wrapSuccessResult(result, "ITEM UPDATE SUCCESSFUL");
    }
    return failedResponse("UPDATE FAILED");
  }

  @Operation(summary = "Get the list of endorsed items for department HOD")
  @GetMapping(value = "/requestItems/departmentHistory")
  @PreAuthorize("hasRole('ROLE_HOD')")
  public ResponseEntity<?> getRequestHistoryByDepartment(
      Authentication authentication,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize)
      throws GeneralException {
    if (authentication == null) return failedResponse("Auth token required");
    Department department =
        employeeService.findEmployeeByEmail(authentication.getName()).getDepartment();
    Page<RequestItem> items =
        requestItemService.requestItemsHistoryByDepartment(department, pageNo, pageSize);
    return PagedResponseDTO.wrapSuccessResult(items, FETCH_SUCCESSFUL);
  }

  @GetMapping("/requestItems/{requestItemId}/status")
  public ResponseEntity<?> getStatusOfRequestItem(@PathVariable("requestItemId") int requestItemId)
      throws GeneralException {
    TrackRequestDTO result = trackRequestStatusService.getRequestStage(requestItemId);
    return ResponseDTO.wrapSuccessResult(result, FETCH_SUCCESSFUL);
  }

  @ResponseStatus(HttpStatus.ACCEPTED)
  @PutMapping("/requestItems/{requestItemId}/resolveComment")
  public void resolveCommentOnRequest(@PathVariable("requestItemId") int requestItemId) {
    requestItemService.resolveCommentOnRequest(requestItemId);
  }
}
