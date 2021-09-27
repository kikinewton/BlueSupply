package com.logistics.supply.controller;

import com.logistics.supply.dto.*;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.event.ApproveRequestItemEvent;
import com.logistics.supply.event.BulkRequestItemEvent;
import com.logistics.supply.event.CancelRequestItemEvent;
import com.logistics.supply.model.CancelledRequestItem;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.RequestItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
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
        items.stream().map(i -> requestItemService.createRequestItem(i, employee)).collect(Collectors.toList());
    if (createdItems.isEmpty()) return failedResponse("FAILED");

    ResponseDTO response = new ResponseDTO("CREATED_REQUEST_ITEMS", SUCCESS, null);
    return ResponseEntity.ok(response);
  }



  @PutMapping(value = "requestItems/bulkEndorse")
  @Secured(value = "ROLE_HOD")
  public ResponseDTO endorseBulkRequestItems(@RequestBody BulkRequestItemDTO bulkRequestItem)
      throws Exception {
    Set<RequestItem> items = bulkRequestItem.getRequestItems();

    //    if (review.equals("review")) {
    //      System.out.println("review = " + review);
    //      System.out.println("items = " + items);
    //
    //      List<RequestItem> reviewList =
    //          items.stream()
    //              .filter(
    //                  i ->
    //                      Objects.isNull(i.getRequestReview())
    //                          && i.getEndorsement().equals(EndorsementStatus.PENDING))
    //              .peek(System.out::println)
    //              .map(r -> requestItemService.updateRequestReview(r.getId(),
    // RequestReview.HOD_REVIEW))
    //              .collect(Collectors.toList());
    //      if (reviewList.size() > 0) return new ResponseDTO(SUCCESS, HttpStatus.OK.name());
    //    }
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
      return new ResponseDTO(SUCCESS, HttpStatus.OK.name());
    }
    return new ResponseDTO(ERROR, HttpStatus.BAD_REQUEST.name());
  }

  @PutMapping(value = "requestItems/updateStatus")
  @PreAuthorize("hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_HOD')")
  public ResponseDTO<List<CancelledRequestItem>> cancelMultipleRequestItem(
      @RequestBody CancelRequestDTO cancelRequestDTO) {
    List<CancelledRequestItem> cancels =
        cancelRequestDTO.getCancelList().stream()
            .map(i -> requestItemService.cancelRequest(i.getId(), cancelRequestDTO.getEmployeeId()))
            .filter(c -> Objects.nonNull(c))
            .collect(Collectors.toList());
    if (Objects.nonNull(cancels) && cancels.size() > 0) {
      CancelRequestItemEvent cancelRequestItemEvent = new CancelRequestItemEvent(this, cancels);
      applicationEventPublisher.publishEvent(cancelRequestItemEvent);
      return new ResponseDTO("CANCELLED_REQUEST", SUCCESS, cancels);
    }
    return new ResponseDTO(HttpStatus.NOT_FOUND.name(), null, ERROR);
  }

  @PutMapping(value = "requestItems/bulkApproval")
  @PreAuthorize("hasRole('ROLE_GENERAL_MANAGER')")
  public ResponseDTO approveMultipleRequestItem(
      @RequestBody MultipleRequestItemDTO approvalDTO,
      @RequestParam(required = false, defaultValue = "NA") String review) {
    System.out.println("start.....");

    //    if (review.equals("review")) {
    //      try {
    //        List<RequestItem> reviewList =
    //            approvalDTO.getApprovalList().stream()
    //                .filter(
    //                    i ->
    //                        i.getRequestReview().equals(RequestReview.HOD_REVIEW)
    //                            && i.getEndorsement().equals(EndorsementStatus.PENDING))
    //                .map(
    //                    r ->
    //                        requestItemService.updateRequestReview(r.getId(),
    // RequestReview.GM_REVIEW))
    //                .collect(Collectors.toList());
    //        if (reviewList.size() > 0) return new ResponseDTO(SUCCESS, HttpStatus.OK.name());
    //      } catch (Exception e) {
    //        e.printStackTrace();
    //      }
    //      return new ResponseDTO("ERROR", HttpStatus.NOT_FOUND.name());
    //    }
    List<Boolean> approvedItems =
        approvalDTO.getRequestList().stream()
            .map(item -> requestItemService.approveRequest(item.getId()))
            .map(y -> y.equals(Boolean.TRUE))
            .collect(Collectors.toList());
    if (approvedItems.size() > 0) {
      List<RequestItem> approved =
          approvalDTO.getRequestList().stream()
              .filter(r -> requestItemService.findApprovedItemById(r.getId()).isPresent())
              .map(a -> requestItemService.findById(a.getId()).get())
              .collect(Collectors.toList());
      ApproveRequestItemEvent requestItemEvent = new ApproveRequestItemEvent(this, approved);
      applicationEventPublisher.publishEvent(requestItemEvent);
      return new ResponseDTO("SUCCESS", HttpStatus.OK.name());
    }
    return new ResponseDTO("ERROR", HttpStatus.NOT_FOUND.name());
  }

  public ResponseEntity<ResponseDTO> failedResponse(String message) {
    ResponseDTO failed = new ResponseDTO(message, ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
