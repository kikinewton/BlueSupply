package com.logistics.supply.controller;

import com.logistics.supply.auth.AppUserDetails;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.model.CancelledRequestItem;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.service.AbstractRestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
public class RequestItemController extends AbstractRestService {

  private final EmailSender emailSender;

  public RequestItemController(EmailSender emailSender) {
    this.emailSender = emailSender;
  }

  @GetMapping(value = "/requestItems")
  @PreAuthorize("hasRole('ROLE_GENERAL_MANAGER')")
  public ResponseDTO<List<RequestItem>> getAll(
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
        return new ResponseDTO<>(HttpStatus.FOUND.name(), items, "SUCCESS");
      } catch (Exception e) {
        log.error(e.getMessage());
        e.printStackTrace();
      }
      return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), items, "ERROR");
    }
    if (toBeApproved.equals("toBeApproved")) {
      System.out.println(2);
      try {
        items.addAll(requestItemService.getEndorsedItemsWithAssignedSuppliers());
        return new ResponseDTO<>(HttpStatus.FOUND.name(), items, "SUCCESS");
      } catch (Exception e) {
        log.error(e.getMessage());
        e.printStackTrace();
      }
      return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), items, "ERROR");
    }
    if (floatOrPettyCash.equals("floatOrPettyCash")) {
      try {
        items.addAll(requestItemService.getEndorsedFloatOrPettyCash());
        return new ResponseDTO<>(HttpStatus.OK.name(), items, "SUCCESS");
      } catch (Exception e) {
        e.printStackTrace();
      }
      return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), items, "ERROR");
    }
    if (approved.equalsIgnoreCase("NA") && toBeApproved.equalsIgnoreCase("NA")) {
      System.out.println(3);
      items.addAll(requestItemService.findAll(pageNo, pageSize));
      if (!items.isEmpty())
        return new ResponseDTO<>(
            "SUCCESS",
            items.stream()
                .sorted(Comparator.comparing(RequestItem::getCreatedDate))
                .collect(Collectors.toList()),
            "REQUEST_ITEMS_FOUND");
    }
    return new ResponseDTO<>(HttpStatus.OK.name(), null, "REQUEST_ITEMS_NOT_FOUND");
  }

  @GetMapping(value = "/requestItems/{requestItemId}")
  public ResponseDTO<RequestItem> getById(@PathVariable int requestItemId) {
    try {
      Optional<RequestItem> item = requestItemService.findById(requestItemId);
      if (item.isPresent()) return new ResponseDTO<>("SUCCESS", item.get(), "REQUEST_ITEM_FOUND");
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO<>("ERROR", null, "REQUEST_ITEM_NOT_FOUND");
  }

  @GetMapping(value = "/requestItemsByDepartment")
  @PreAuthorize("hasRole('ROLE_HOD')")
  public ResponseDTO<List<RequestItem>> getRequestItemsByDepartment(
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
        return new ResponseDTO<>(HttpStatus.OK.name(), result, "SUCCESS");
      } catch (Exception e) {
        e.printStackTrace();
      }
      return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), items, "ERROR");
    }
    try {
      items.addAll(requestItemService.getRequestItemForHOD(employee.getDepartment().getId()));
      return new ResponseDTO<>(SUCCESS, items, "REQUEST_ITEM_FOUND");
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), null, "REQUEST_ITEM_NOT_FOUND");
  }

  @GetMapping(value = "/requestItemsByDepartment/endorsed")
  @PreAuthorize("hasRole('ROLE_HOD')")
  public ResponseDTO<List<RequestItem>> getEndorsedRequestItemsForDepartment(
          Authentication authentication) {

    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    try {
      List<RequestItem> items =
          requestItemService.getEndorsedRequestItemsForDepartment(employee.getDepartment().getId());
      if (Objects.nonNull(items))
        return new ResponseDTO<>(HttpStatus.OK.name(), items, "REQUEST_ITEM_FOUND");

    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), null, "REQUEST_ITEM_NOT_FOUND");
  }

  @PutMapping(value = "/requestItems/{requestItemId}/cancel")
  @PreAuthorize("hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_HOD')")
  public ResponseDTO<CancelledRequestItem> cancelRequest(Authentication authentication,
      @PathVariable("requestItemId") int requestItemId) {

    try {
      Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
      if (Objects.nonNull(employee)) {
        Optional<RequestItem> requestItem = requestItemService.findById(requestItemId);
        if (requestItem.isPresent()) {
          CancelledRequestItem result = requestItemService.cancelRequest(requestItemId, employee.getId());
          if (Objects.nonNull(result))
            return new ResponseDTO(HttpStatus.OK.name(), result, SUCCESS);
        }
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO(HttpStatus.NOT_FOUND.name(), null, ERROR);
  }

  @GetMapping("/requestItems/approvedItems")
  public ResponseDTO<List<RequestItem>> getApprovedRequestItems() {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemService.getApprovedItems());
      return new ResponseDTO<>(HttpStatus.FOUND.name(), items, "SUCCESS");
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), items, "ERROR");
  }

  @GetMapping("/requestItems/endorsedItems")
  public ResponseDTO<List<RequestItem>> getEndorsedRequestItems() {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemService.getEndorsedItems());
      return new ResponseDTO<>(HttpStatus.FOUND.name(), items, "SUCCESS");
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), items, "ERROR");
  }

  @GetMapping(value = "/requestItemsForEmployee")
  public ResponseDTO<List<RequestItem>> getCountNofEmployeeRequestItem(
      Authentication authentication, @RequestParam(required = false, defaultValue = "10") int count) {
    List<RequestItem> items = new ArrayList<>();
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    try {
      items.addAll(requestItemService.getCountNofEmployeeRequest(count, employee.getId()));
      return new ResponseDTO<>(HttpStatus.FOUND.name(), items, "SUCCESS");
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), items, "ERROR");
  }

  @PutMapping(value = "/requestItems/updateQuantity")
  public ResponseDTO<RequestItem> updateQuantityForNotEndorsedRequest(
      @RequestParam int requestItemId, @RequestParam int number)
      throws Exception {
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
      if (Objects.isNull(result))
        return new ResponseDTO<>(
            HttpStatus.BAD_REQUEST.name(), null, "Update to item quantity failed");
      return new ResponseDTO<>(HttpStatus.OK.name(), result, SUCCESS);
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, "ERROR");
  }
}
