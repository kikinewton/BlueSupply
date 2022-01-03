package com.logistics.supply.controller;

import com.logistics.supply.dto.ItemUpdateDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.RequestItemComment;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.RequestItemCommentService;
import com.logistics.supply.service.RequestItemService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
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
@Slf4j
@RequestMapping(value = "/api")
@CrossOrigin(
    origins = {
      "https://etornamtechnologies.github.io/skyblue-request-frontend-react",
      "http://localhost:4000"
    },
    allowedHeaders = "*")
public class RequestItemController {

  private final EmailSender emailSender;
  @Autowired RequestItemService requestItemService;
  @Autowired EmployeeService employeeService;
  @Autowired private RequestItemCommentService requestItemCommentService;

  public RequestItemController(EmailSender emailSender) {
    this.emailSender = emailSender;
  }

  @GetMapping(value = "/requestItems")
  @PreAuthorize("hasRole('ROLE_GENERAL_MANAGER')")
  public ResponseEntity<?> listRequestItems(
      @RequestParam(defaultValue = "0", required = false) int pageNo,
      @RequestParam(defaultValue = "100", required = false) int pageSize,
      @RequestParam(required = false, defaultValue = "false") Boolean toBeApproved,
      @RequestParam(required = false, defaultValue = "false") Boolean approved) {
    List<RequestItem> items = new ArrayList<>();

    if (approved) {
      try {
        items.addAll(requestItemService.getApprovedItems());
        ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, items);
        return ResponseEntity.ok(response);
      } catch (Exception e) {
        log.error(e.getMessage());
      }
    }
    if (toBeApproved) {
      try {
        items.addAll(requestItemService.getEndorsedItemsWithAssignedSuppliers());
        ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, items);
        return ResponseEntity.ok(response);
      } catch (Exception e) {
        log.error(e.getMessage());
      }
    }

    items.addAll(requestItemService.findAll(pageNo, pageSize));
    ResponseDTO response =
        new ResponseDTO(
            "REQUEST_ITEMS_FOUND",
            SUCCESS,
            items.stream()
                .sorted(Comparator.comparing(RequestItem::getId))
                .collect(Collectors.toList()));
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/requestItems/{requestItemId}")
  public ResponseEntity<?> getRequestItemById(@PathVariable int requestItemId) {
    try {
      Optional<RequestItem> item = requestItemService.findById(requestItemId);
      if (item.isPresent()) {
        ResponseDTO response = new ResponseDTO("REQUEST_ITEM_FOUND", SUCCESS, item.get());
        return ResponseEntity.ok(response);
      }

    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("REQUEST_ITEM_NOT_FOUND");
  }

  @GetMapping(value = "/requestItemsByDepartment")
  @PreAuthorize("hasRole('ROLE_HOD')")
  public ResponseEntity<?> listRequestItemsByDepartment(
      Authentication authentication,
      @RequestParam(required = false, defaultValue = "false") Boolean toBeReviewed) {
    List<RequestItem> items = new ArrayList<>();
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    if (toBeReviewed) {
      try {
        items.addAll(
            requestItemService.findRequestItemsToBeReviewed(
                RequestReview.PENDING, employee.getDepartment().getId()));

        ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, items);
        return ResponseEntity.ok(response);
      } catch (Exception e) {
        log.error(e.getMessage());
      }
      return failedResponse("FETCH_FAILED");
    }
    try {
      items.addAll(requestItemService.getRequestItemForHOD(employee.getDepartment().getId()));
      ResponseDTO response = new ResponseDTO("REQUEST_ITEM_FOUND", SUCCESS, items);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("REQUEST_ITEM_NOT_FOUND");
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

    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    try {
      if (review && Objects.nonNull(quotationId)) {
        List<RequestItem> items =
            requestItemService.getItemsWithFinalPriceUnderQuotation(Integer.parseInt(quotationId));
        ResponseDTO response =
            new ResponseDTO("ENDORSED_ITEMS_WITH_PRICES_FROM_SUPPLIER", SUCCESS, items);
        return ResponseEntity.ok(response);
      }
      List<RequestItem> items =
          requestItemService.getEndorsedRequestItemsForDepartment(employee.getDepartment().getId());
      ResponseDTO response = new ResponseDTO("ENDORSED_REQUEST_ITEM", SUCCESS, items);
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return notFound("REQUEST_ITEMS_NOT_FOUND");
  }

  @Operation(
      summary = "Get the list of endorsed items for procurement to work on")
  @GetMapping("/requestItems/endorsed")
  @PreAuthorize(" hasRole('ROLE_PROCUREMENT_MANAGER') or hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseEntity<?> listAllEndorsedRequestItems(
      Authentication authentication,
      @RequestParam(required = false, defaultValue = "false") Boolean withSupplier) {
    List<RequestItem> items = new ArrayList<>();
    try {
      if (withSupplier) {
        items.addAll(requestItemService.getEndorsedItemsWithSuppliers());
        ResponseDTO response = new ResponseDTO("ENDORSED_REQUEST_ITEMS", SUCCESS, items);
        return ResponseEntity.ok(response);
      }
      items.addAll(requestItemService.getEndorsedItemsWithoutSuppliers());
      ResponseDTO response = new ResponseDTO("ENDORSED_REQUEST_ITEMS", SUCCESS, items);
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return notFound("REQUEST_ITEMS_NOT_FOUND");
  }

  @GetMapping(value = "/requestItemsForEmployee")
  public ResponseEntity<?> listRequestItemsForEmployee(
      Authentication authentication,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "100") int pageSize) {
    List<RequestItem> items = new ArrayList<>();
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    try {
      items.addAll(requestItemService.findByEmployee(employee, pageNo, pageSize));
      var itemsWithComment =
          items.stream()
              .map(
                  x -> {
                    List<RequestItemComment> comments =
                        requestItemCommentService.findByRequestItemId(x.getId());
                    x.setComment(comments);
                    return x;
                  })
              .collect(Collectors.toSet());
      ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, itemsWithComment);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return notFound("REQUEST_ITEMS_NOT_FOUND");
  }


  @Operation(summary = "Change quantity or name of items requested", tags = "REQUEST_ITEM")
  @PutMapping(value = "/requestItems/{requestItemId}")
  public ResponseEntity<?> updateQuantityForNotEndorsedRequest(
      @PathVariable("requestItemId") int requestItemId,
      @Valid @RequestBody ItemUpdateDTO itemUpdateDTO,
      Authentication authentication)
      throws Exception {
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
      if (Objects.isNull(result)) return failedResponse("ITEM_UPDATE_FAILED");
      ResponseDTO response = new ResponseDTO("ITEM_UPDATE_SUCCESSFUL", SUCCESS, result);
      return ResponseEntity.ok(response);
    }
    return failedResponse("UPDATE_FAILED");
  }
}
