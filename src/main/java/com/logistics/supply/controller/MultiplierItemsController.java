package com.logistics.supply.controller;

import com.logistics.supply.dto.BulkRequestItemDTO;
import com.logistics.supply.dto.MultipleItemDTO;
import com.logistics.supply.dto.ReqItems;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.event.ApproveRequestItemEvent;
import com.logistics.supply.event.BulkRequestItemEvent;
import com.logistics.supply.event.CancelRequestItemEvent;
import com.logistics.supply.model.CancelledRequestItem;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.RequestItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@RestController
@Slf4j
@RequestMapping("/api")
public class MultiplierItemsController {

  @Autowired EmployeeService employeeService;
  @Autowired RequestItemService requestItemService;
  @Autowired ApplicationEventPublisher applicationEventPublisher;

  @PostMapping("/multipleRequestItems")
  public ResponseEntity<?> addBulkRequest(
      @RequestBody @Valid MultipleItemDTO multipleItemDTO, Authentication authentication)
      throws Exception {
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    List<ReqItems> items = multipleItemDTO.getMultipleRequestItem();
    List<RequestItem> createdItems =
        items.stream()
            .map(i -> requestItemService.createRequestItem(i, employee))
            .collect(Collectors.toList());
    if (createdItems.isEmpty()) return failedResponse("FAILED");

    ResponseDTO response = new ResponseDTO("CREATED_REQUEST_ITEMS", SUCCESS, createdItems);
    return ResponseEntity.ok(response);
  }


//  private ResponseEntity<?> reviewRequestAfterProcurementHod(
//      Authentication authentication, BulkRequestItemDTO bulkRequestItem) {
//    if (!authentication.getAuthorities().equals(EmployeeRole.ROLE_HOD))
//      return failedResponse("FORBIDDEN_ACCESS");
//    Set<RequestItem> items = bulkRequestItem.getRequestItems();
//    List<RequestItem> reviewList =
//        items.stream()
//            .filter(
//                i ->
//                    Objects.isNull(i.getRequestReview())
//                        && i.getEndorsement().equals(EndorsementStatus.PENDING))
//            .peek(System.out::println)
//            .map(r -> requestItemService.updateRequestReview(r.getId(), RequestReview.HOD_REVIEW))
//            .collect(Collectors.toList());
//    if(!reviewList.isEmpty()) {
//
//    }
//    return failedResponse("HOD_REVIEW_FAILED");
//  }

  private ResponseEntity<?> endorseRequest(
      Authentication authentication, BulkRequestItemDTO bulkRequestItem) throws Exception {
    if (!authentication.getAuthorities().equals(EmployeeRole.ROLE_HOD))
      return failedResponse("FORBIDDEN_ACCESS");
    Set<RequestItem> items = bulkRequestItem.getRequestItems();
    List<RequestItem> endorse =
        items.stream()
            .filter(
                x ->
                    (Objects.isNull(x.getSuppliedBy())
                        && x.getEndorsement().equals(EndorsementStatus.PENDING)
                        && Objects.isNull(x.getEndorsementDate())))
            .map(y -> requestItemService.endorseRequest(y.getId()))
            .collect(Collectors.toList());

    if (endorse.size() > 0) {

      BulkRequestItemEvent requestItemEvent = new BulkRequestItemEvent(this, endorse);
      applicationEventPublisher.publishEvent(requestItemEvent);
      ResponseDTO response = new ResponseDTO("REQUEST_ENDORSED", SUCCESS, endorse);
      return ResponseEntity.ok(response);
    }
    return failedResponse("FAILED_TO_ENDORSE");
  }

  @PutMapping(value = "requestItems/updateStatus/{statusChange}")
  @PreAuthorize("hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_HOD')")
  public ResponseEntity<?> updateMultipleRequestItem(
      Authentication authentication,
      @RequestBody BulkRequestItemDTO bulkRequestItem,
      @PathVariable("statusChange") String statusChange)
      throws Exception {
    statusChange = statusChange.toUpperCase(Locale.ROOT);
    switch (statusChange) {
      case "ENDORSE":
        return endorseRequest(authentication, bulkRequestItem);
      case "APPROVE":
        return approveRequestGM(authentication, bulkRequestItem);
      case "CANCEL":
        return cancelRequest(authentication, bulkRequestItem);
      default:
        return failedResponse("UPDATE_STATUS_FAILED");
    }
  }

  private ResponseEntity<?> cancelRequest(
      Authentication authentication, BulkRequestItemDTO bulkRequestItem) {
    int employeeId = employeeService.findEmployeeByEmail(authentication.getName()).getId();
    List<CancelledRequestItem> cancels =
        bulkRequestItem.getRequestItems().stream()
            .map(i -> requestItemService.cancelRequest(i.getId(), employeeId))
            .filter(c -> Objects.nonNull(c))
            .collect(Collectors.toList());
    if (cancels.size() > 0) {
      CancelRequestItemEvent cancelRequestItemEvent = new CancelRequestItemEvent(this, cancels);
      applicationEventPublisher.publishEvent(cancelRequestItemEvent);
      ResponseDTO response = new ResponseDTO("CANCELLED_REQUEST", SUCCESS, cancels);
      return ResponseEntity.ok(response);
    }
    return failedResponse("CANCEL_REQUEST_FAILED");
  }

  private ResponseEntity<?> approveRequestGM(
      Authentication authentication, BulkRequestItemDTO bulkRequestItem) {
    if (!authentication.getAuthorities().equals(EmployeeRole.ROLE_GENERAL_MANAGER))
      return failedResponse("FORBIDDEN_ACCESS");
    List<Boolean> approvedItems =
        bulkRequestItem.getRequestItems().stream()
            .map(item -> requestItemService.approveRequest(item.getId()))
            .map(y -> y.equals(Boolean.TRUE))
            .collect(Collectors.toList());
    if (approvedItems.size() > 0) {
      List<RequestItem> approved =
          bulkRequestItem.getRequestItems().stream()
              .filter(r -> requestItemService.findApprovedItemById(r.getId()).isPresent())
              .map(a -> requestItemService.findById(a.getId()).get())
              .collect(Collectors.toList());
      ApproveRequestItemEvent requestItemEvent = new ApproveRequestItemEvent(this, approved);
      applicationEventPublisher.publishEvent(requestItemEvent);
      ResponseDTO response = new ResponseDTO("APPROVAL_SUCCESSFUL", SUCCESS, approved);
      return ResponseEntity.ok(response);
    }
    return failedResponse("APPROVAL_FAILED");
  }

  public ResponseEntity<ResponseDTO> failedResponse(String message) {
    ResponseDTO failed = new ResponseDTO(message, ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
