package com.logistics.supply.controller;

import com.logistics.supply.dto.*;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.ProcurementType;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.event.ApproveRequestItemEvent;
import com.logistics.supply.event.ApproveRequestItemEventListener;
import com.logistics.supply.event.BulkRequestItemEvent;
import com.logistics.supply.event.CancelRequestItemEvent;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.RequestItemRepository;
import com.logistics.supply.service.AbstractRestService;
import com.logistics.supply.util.CommonHelper;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.logistics.supply.enums.RequestStatus.*;
import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@RestController
@Slf4j
@RequestMapping("/api")
public class MultiplierItemsController extends AbstractRestService {

  @Autowired private ApplicationEventPublisher applicationEventPublisher;

  @Autowired private RequestItemRepository requestItemRepository;

  Set<String> nonNulls =
      new HashSet<>(Arrays.asList("name", "reason", "purpose", "quantity", "employee"));
  List<ReqItems> failed = new ArrayList<>();
  List<RequestItem> completed = new ArrayList<>();

  @PostMapping("/multipleRequestItems")
  public ResponseDTO addBulkRequest(@RequestBody MultipleItemDTO multipleItemDTO) throws Exception {
    List<ReqItems> item = multipleItemDTO.getMultipleRequestItem();
    for (ReqItems x : item) {
      String[] nullValues = CommonHelper.getNullPropertyNames(x);
      Arrays.asList(nullValues).forEach(c -> System.out.println("Null value: " + c));

      Set<String> l = new HashSet<>(Arrays.asList(nullValues));

      if (Arrays.stream(nullValues).count() > 0) {
        log.info("Null value found");
        failed.add(x);
      } else {
        RequestItem result = createRequestItem(x, multipleItemDTO.getEmployee_id());
        if (Objects.nonNull(result)) completed.add(result);
      }
    }
    return new ResponseDTO(HttpStatus.OK.name(), null, "REQUEST SENT");
  }

  private RequestItem createRequestItem(ReqItems itemDTO, int employee_id) {
    RequestItem requestItem = new RequestItem();
    requestItem.setReason(itemDTO.getReason());
    requestItem.setName(itemDTO.getName());
    requestItem.setPurpose(itemDTO.getPurpose());
    requestItem.setQuantity(itemDTO.getQuantity());
    requestItem.setRequestType(itemDTO.getRequestType());
    Employee employee = employeeService.getById(employee_id);
    Department userDepartment = departmentService.getById(itemDTO.getUserDepartment().getId());
    requestItem.setUserDepartment(userDepartment);
    requestItem.setEmployee(employee);
    try {
      RequestItem result = requestItemService.create(requestItem);

      if (Objects.nonNull(result)) return result;
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  @PutMapping(value = "requestItems/bulkEndorse")
  @Secured(value = "ROLE_HOD")
  public ResponseDTO endorseBulkRequestItems(
      @RequestBody MultipleEndorsementDTO endorsementDTO)
      throws Exception {
    List<RequestItem> items = endorsementDTO.getEndorsedList();

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
//              .map(r -> requestItemService.updateRequestReview(r.getId(), RequestReview.HOD_REVIEW))
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

  @PutMapping(value = "requestItems/bulkCancel")
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
      return new ResponseDTO(HttpStatus.OK.name(), cancels, SUCCESS);
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
//                        requestItemService.updateRequestReview(r.getId(), RequestReview.GM_REVIEW))
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

}
