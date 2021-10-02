package com.logistics.supply.controller;

import com.logistics.supply.auth.AppUserDetails;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.RequestItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.util.*;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

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

  public RequestItemController(EmailSender emailSender) {
    this.emailSender = emailSender;
  }

  @GetMapping(value = "/requestItems")
  @PreAuthorize("hasRole('ROLE_GENERAL_MANAGER')")
  public ResponseEntity<?> getAll(
      @RequestParam(defaultValue = "0", required = false) int pageNo,
      @RequestParam(defaultValue = "20", required = false) int pageSize,
      @RequestParam(required = false, defaultValue = "NA") String toBeApproved,
      @RequestParam(required = false, defaultValue = "NA") String approved,
      @RequestParam(required = false, defaultValue = "NA") String floatOrPettyCash) {
    List<RequestItem> items = new ArrayList<>();

    if (approved.equals("approved")) {
      System.out.println(1);
      try {
        items.addAll(requestItemService.getApprovedItems());
        ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, items);
        return ResponseEntity.ok(response);
      } catch (Exception e) {
        log.error(e.getMessage());
      }
      failedResponse("FETCH_FAILED");
    }
    if (toBeApproved.equals("toBeApproved")) {
      System.out.println(2);
      try {
        items.addAll(requestItemService.getEndorsedItemsWithAssignedSuppliers());
        ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, items);
        return ResponseEntity.ok(response);
      } catch (Exception e) {
        log.error(e.getMessage());
      }
      return failedResponse("FETCH_APPROVED_LIST_FAILED");
    }
    if (floatOrPettyCash.equals("floatOrPettyCash")) {
      try {
        items.addAll(requestItemService.getEndorsedFloatOrPettyCash());
        ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, items);
        return ResponseEntity.ok(response);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return failedResponse("FETCH_PETTY_CASH_OR_LPO_FAILED");
    }
    if (approved.equalsIgnoreCase("NA") && toBeApproved.equalsIgnoreCase("NA")) {
      System.out.println(3);
      items.addAll(requestItemService.findAll(pageNo, pageSize));
      if (!items.isEmpty()) {
        ResponseDTO response =
            new ResponseDTO(
                "REQUEST_ITEMS_FOUND",
                SUCCESS,
                items.stream()
                    .sorted(Comparator.comparing(RequestItem::getCreatedDate))
                    .collect(Collectors.toList()));
        return ResponseEntity.ok(response);
      }
    }
    return failedResponse("REQUEST_ITEMS_NOT_FOUND");
  }

  @GetMapping(value = "/requestItems/{requestItemId}")
  public ResponseEntity<?> getById(@PathVariable int requestItemId) {
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
  public ResponseEntity<?> getRequestItemsByDepartment(
      Authentication authentication,
      @RequestParam(required = false, defaultValue = "NA") String toBeReviewed) {
    List<RequestItem> items = new ArrayList<>();
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    if (toBeReviewed.equals("toBeReviewed")) {
      try {
        items.addAll(requestItemService.findRequestItemsToBeReviewed(RequestReview.HOD_REVIEW));
        List<RequestItem> result =
            items.stream()
                .filter(i -> i.getUserDepartment().getId().equals(employee.getDepartment().getId()))
                .collect(Collectors.toList());
        ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, result);
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

  @GetMapping(value = "/requestItemsByDepartment/endorsed")
  @PreAuthorize("hasRole('ROLE_HOD')")
  public ResponseEntity<?> getEndorsedRequestItemsForDepartment(Authentication authentication) {

    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    try {
      List<RequestItem> items =
          requestItemService.getEndorsedRequestItemsForDepartment(employee.getDepartment().getId());
      ResponseDTO response = new ResponseDTO("REQUEST_ITEM_FOUND", SUCCESS, items);
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("REQUEST_ITEM_NOT_FOUND");
  }



  @GetMapping(value = "/requestItemsForEmployee")
  public ResponseEntity<?> getCountNofEmployeeRequestItem(
      Authentication authentication,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "20") int pageSize) {
    List<RequestItem> items = new ArrayList<>();
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    try {
      items.addAll(requestItemService.findByEmployee(employee, pageNo, pageSize));
      ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, items);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("FETCH_FAILED");
  }

  @PutMapping(value = "/requestItems/updateQuantity")
  public ResponseEntity<?> updateQuantityForNotEndorsedRequest(
      @RequestParam int requestItemId, @RequestParam @Positive int number) throws Exception {
    AppUserDetails principal =
        (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    boolean requestItemExist =
        requestItemService
            .findById(requestItemId)
            .filter(
                x ->
                    x.getEmployee().getEmail().equals(principal.getUsername())
                        && x.getEndorsement().equals(EndorsementStatus.PENDING)
                        && number > 0)
            .isPresent();
    if (requestItemExist) {
      RequestItem result = requestItemService.updateItemQuantity(requestItemId, number);
      if (Objects.isNull(result)) return failedResponse("QUANTITY_UPDATE_FAILED");
      ResponseDTO response = new ResponseDTO("QUANTITY_UPDATE_SUCCESSFUL", SUCCESS, result);
      return ResponseEntity.ok(response);
    }
    return failedResponse("UPDATE_FAILED");
  }

  private ResponseEntity<ResponseDTO> failedResponse(String message) {
    ResponseDTO failed = new ResponseDTO(message, ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
