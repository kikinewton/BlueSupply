package com.logistics.supply.controller;

import com.logistics.supply.dto.ItemUpdateDTO;
import com.logistics.supply.dto.PagedResponseDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.*;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.RequestItemCommentService;
import com.logistics.supply.service.RequestItemService;
import com.logistics.supply.service.TrackRequestStatusService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.SUCCESS;
import static com.logistics.supply.util.Helper.failedResponse;
import static com.logistics.supply.util.Helper.notFound;

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
  private final RequestItemCommentService requestItemCommentService;
  private final TrackRequestStatusService trackRequestStatusService;

  @GetMapping(value = "/requestItems")
  @PreAuthorize("hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_ADMIN')")
  public ResponseEntity<?> listRequestItems(
      @RequestParam(defaultValue = "0", required = false) int pageNo,
      @RequestParam(defaultValue = "100", required = false) int pageSize,
      @RequestParam(required = false, defaultValue = "false") Boolean toBeApproved,
      @RequestParam(required = false, defaultValue = "false") Boolean approved) {
    List<RequestItem> items = new ArrayList<>();

    if (approved) {

      items.addAll(requestItemService.getApprovedItems());
      ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, items);
      return ResponseEntity.ok(response);
    }
    if (toBeApproved) {

      items.addAll(requestItemService.getEndorsedItemsWithAssignedSuppliers());
      ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, items);
      return ResponseEntity.ok(response);
    }

    items.addAll(requestItemService.findAll(pageNo, pageSize));
    ResponseDTO response =
        new ResponseDTO(
            "REQUEST ITEMS FOUND",
            SUCCESS,
            items.stream()
                .sorted(Comparator.comparing(RequestItem::getId))
                .collect(Collectors.toList()));
    return ResponseEntity.ok(response);
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
    List<RequestItem> items = new ArrayList<>();
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    if (toBeReviewed) {

      items.addAll(
          requestItemService.findRequestItemsToBeReviewed(
              RequestReview.PENDING, employee.getDepartment().getId()));

      if (items.isEmpty()) return notFound("REQUEST ITEM NOT FOUND");

      ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, items);
      return ResponseEntity.ok(response);
    }

    items.addAll(requestItemService.getRequestItemForHOD(employee.getDepartment().getId()));
    if (items.isEmpty()) return notFound("REQUEST ITEM NOT FOUND");

    ResponseDTO response = new ResponseDTO("REQUEST ITEM FOUND", SUCCESS, items);
    return ResponseEntity.ok(response);
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
      ResponseDTO response =
          new ResponseDTO("ENDORSED ITEMS WITH PRICES FROM SUPPLIER", SUCCESS, items);
      return ResponseEntity.ok(response);
    }
    List<RequestItem> items =
        requestItemService.getEndorsedRequestItemsForDepartment(employee.getDepartment().getId());

    if (items.isEmpty()) return notFound("REQUEST ITEMS NOT FOUND");

    ResponseDTO response = new ResponseDTO("ENDORSED REQUEST ITEM", SUCCESS, items);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get the list of endorsed items for procurement to work on")
  @GetMapping("/requestItems/endorsed")
  @PreAuthorize(" hasRole('ROLE_PROCUREMENT_MANAGER') or hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseEntity<?> listAllEndorsedRequestItems(
      Authentication authentication,
      @RequestParam(required = false, defaultValue = "false") Boolean withSupplier) {
    if (Objects.isNull(authentication)) return failedResponse("Auth token is required");
    List<RequestItem> items = new ArrayList<>();

    if (withSupplier) {
      items.addAll(requestItemService.getEndorsedItemsWithSuppliers());
      ResponseDTO response = new ResponseDTO("ENDORSED REQUEST ITEMS", SUCCESS, items);
      return ResponseEntity.ok(response);
    }

    items.addAll(requestItemService.getEndorsedItemsWithoutSuppliers());
    if (items.isEmpty()) return notFound("REQUEST ITEMS NOT FOUND");

    ResponseDTO response = new ResponseDTO("ENDORSED REQUEST ITEMS", SUCCESS, items);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/requestItemsForEmployee")
  public ResponseEntity<?> listRequestItemsForEmployee(
      Authentication authentication,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize) {
    if (Objects.isNull(authentication)) return failedResponse("Auth token is required");

    List<RequestItem> items = new ArrayList<>();
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());

    items.addAll(requestItemService.findByEmployee(employee, pageNo, pageSize));
    Set<RequestItem> itemsWithComment =
        items.stream()
            .map(
                x -> {
                  List<RequestItemComment> comments =
                      requestItemCommentService.findByRequestItemId(x.getId());
                  x.setComment(comments);
                  return x;
                })
            .collect(Collectors.toSet());
    if (itemsWithComment.isEmpty()) return notFound("REQUEST ITEMS NOT FOUND");

    ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, itemsWithComment);
    return ResponseEntity.ok(response);
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
      if (Objects.isNull(result)) return failedResponse("ITEM UPDATE FAILED");
      ResponseDTO response = new ResponseDTO("ITEM UPDATE SUCCESSFUL", SUCCESS, result);
      return ResponseEntity.ok(response);
    }
    return failedResponse("UPDATE FAILED");
  }

  @Operation(summary = "Get the list of endorsed items for department HOD")
  @GetMapping(value = "/requestItems/departmentHistory")
  @PreAuthorize("hasRole('ROLE_HOD')")
  public ResponseEntity<?> getRequestHistoryByDepartment(
      Authentication authentication,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize) {
    if (authentication == null) return failedResponse("Auth token required");
    Department department =
        employeeService.findEmployeeByEmail(authentication.getName()).getDepartment();
    if (department == null) return failedResponse("INVALID DEPARTMENT");
    Page<RequestItem> items =
        requestItemService.requestItemsHistoryByDepartment(department, pageNo, pageSize);
    PagedResponseDTO.MetaData metaData =
        new PagedResponseDTO.MetaData(
            items.getNumberOfElements(), items.getSize(), items.getNumber(), items.getTotalPages());
    PagedResponseDTO response =
        new PagedResponseDTO("FETCH SUCCESSFUL", SUCCESS, metaData, items.getContent());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/requestItems/{requestItemId}/status")
  public ResponseEntity<?> getStatusOfRequestItem(
      @PathVariable("requestItemId") int requestItemId) {
    TrackRequestDTO result = trackRequestStatusService.getRequestStage(requestItemId);
    if (result == null) return failedResponse("GET REQUEST STATUS FAILED");
    ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, result);
    return ResponseEntity.ok(response);
  }
}
