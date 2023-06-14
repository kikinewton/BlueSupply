package com.logistics.supply.controller;

import com.logistics.supply.dto.*;
import com.logistics.supply.enums.*;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.event.ApproveRequestItemEvent;
import com.logistics.supply.event.BulkRequestItemEvent;
import com.logistics.supply.event.CancelRequestItemEvent;
import com.logistics.supply.exception.NotFoundException;
import com.logistics.supply.model.*;
import com.logistics.supply.service.*;
import com.logistics.supply.util.Constants;
import com.logistics.supply.util.EmailSenderUtil;
import com.logistics.supply.util.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/api")
@RequiredArgsConstructor
public class MultiplierItemsController {

  private final EmployeeService employeeService;
  private final QuotationService quotationService;
  private final RequestItemService requestItemService;
  private final PettyCashService pettyCashService;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final FloatOrderService floatOrderService;
  private final EmailSenderUtil senderUtil;

  @Value("${config.mail.template}")
  String emailTemplate;

  @PostMapping("/multipleRequestItems")
  public ResponseEntity<?> addBulkRequest(
          @RequestBody @Valid MultipleItemDTO multipleItemDTO, Authentication authentication) {
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    List<RequestItemDTO> createdItems =
        requestItemService.createRequestItem(multipleItemDTO.getMultipleRequestItem(), employee);
    return ResponseDTO.wrapSuccessResult(createdItems, "CREATED REQUEST ITEMS");
  }

  @PostMapping("/bulkFloatOrPettyCash/{procurementType}")
  public ResponseEntity<?> addBulkFloatOrPettyCash(
      @Valid @RequestBody FloatOrPettyCashDTO bulkItems,
      @PathVariable("procurementType") ProcurementType procurementType,
      Authentication authentication) {
    if (authentication == null) return Helper.failedResponse("Auth token is required");
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    if (procurementType.equals(ProcurementType.FLOAT)) {
      FloatOrder saveFloatOrder = floatOrderService.saveFloatOrder(bulkItems, employee);
      return ResponseDTO.wrapSuccessResult(saveFloatOrder, "CREATED FLOAT ITEMS");
    }
    if (procurementType.equals(ProcurementType.PETTY_CASH)) {
      PettyCashOrder pettyCashOrder = pettyCashService.saveAll(bulkItems, employee);
      return ResponseDTO.wrapSuccessResult(
          pettyCashOrder.getPettyCash(), "CREATED PETTY CASH ITEMS");
    }
    return Helper.failedResponse("FAILED TO CREATE PETTY CASH");
  }

  @Caching(
      evict = {
        @CacheEvict(value = "requestItemsByToBeReviewed", allEntries = true),
        @CacheEvict(value = "requestItemsHistoryByDepartment", allEntries = true)
      })
  @PutMapping(value = "requestItems/updateStatus/{statusChange}")
  @PreAuthorize("hasRole('ROLE_HOD') or hasRole('ROLE_GENERAL_MANAGER')")
  public ResponseEntity<?> updateMultipleRequestItem(
      @Valid @RequestBody BulkRequestItemDTO bulkRequestItem,
      @PathVariable("statusChange") UpdateStatus statusChange,
      Authentication authentication) {
    switch (statusChange) {
      case ENDORSE:
        return endorseRequest(authentication, bulkRequestItem.getRequestItems());
      case APPROVE:
        return approveRequestGM(authentication, bulkRequestItem.getRequestItems());
      case CANCEL:
        return cancelRequest(authentication, bulkRequestItem.getRequestItems());
      case HOD_REVIEW:
        return reviewRequestAfterProcurement(authentication, bulkRequestItem.getRequestItems());
      default:
        return Helper.failedResponse("UPDATE STATUS FAILED");
    }
  }

  private Boolean checkAuthorityExist(Authentication authentication, EmployeeRole role) {
    return authentication.getAuthorities().stream()
        .map(x -> x.getAuthority().equalsIgnoreCase(role.name()))
        .filter(x -> x)
        .findAny()
        .get();
  }

  private ResponseEntity<?> cancelRequest(Authentication authentication, List<RequestItem> items) {
    int employeeId = employeeService.findEmployeeByEmail(authentication.getName()).getId();
    List<CancelledRequestItem> cancels =
        items.stream()
            .map(i -> requestItemService.cancelRequest(i.getId(), employeeId))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    if (cancels.isEmpty()) {
      CancelRequestItemEvent cancelRequestItemEvent = new CancelRequestItemEvent(this, cancels);
      applicationEventPublisher.publishEvent(cancelRequestItemEvent);
      ResponseDTO response = new ResponseDTO("CANCELLED REQUEST", Constants.SUCCESS, cancels);
      return ResponseEntity.ok(response);
    }
    return Helper.failedResponse("CANCEL REQUEST FAILED");
  }

  private ResponseEntity<?> approveRequestGM(
      Authentication authentication, List<RequestItem> items) {
    if (!checkAuthorityExist(authentication, EmployeeRole.ROLE_GENERAL_MANAGER))
      return Helper.failedResponse("FORBIDDEN ACCESS");
    List<Boolean> approvedItems =
        items.stream()
            .map(item -> {
              try {
                return requestItemService.approveRequest(item.getId());
              } catch (GeneralException e) {
                throw new RuntimeException(e);
              }
            })
            .map(y -> y.equals(Boolean.TRUE))
            .collect(Collectors.toList());
    if (!approvedItems.isEmpty()) {
      List<RequestItem> approved =
          items.stream()
              .filter(r -> requestItemService.findApprovedItemById(r.getId()).isPresent())
              .map(a -> requestItemService.findById(a.getId()).get())
              .collect(Collectors.toList());
      ApproveRequestItemEvent requestItemEvent = new ApproveRequestItemEvent(this, approved);
      applicationEventPublisher.publishEvent(requestItemEvent);
      ResponseDTO response = new ResponseDTO("APPROVAL SUCCESSFUL", Constants.SUCCESS, approved);
      return ResponseEntity.ok(response);
    }
    return Helper.failedResponse("APPROVAL FAILED");
  }

  private ResponseEntity<?> reviewRequestAfterProcurement(
      Authentication authentication, List<RequestItem> items) {
    if (!checkAuthorityExist(authentication, EmployeeRole.ROLE_HOD))
      return Helper.failedResponse("FORBIDDEN ACCESS");
    Set<RequestItem> reviewList =
        items.stream()
            //            .filter(i -> Objects.nonNull(i.getRequestCategory()))
            .map(r -> requestItemService.updateRequestReview(r.getId(), RequestReview.HOD_REVIEW))
            .collect(Collectors.toSet());
    if (!reviewList.isEmpty()) {
      Optional<Quotation> optionalQuotation =
          reviewList.stream().findAny().map(this::filterFinalQuotation);
      optionalQuotation.ifPresent(q -> quotationService.reviewByHod(q.getId()));
      sendApproveEmailToGM();
      return ResponseDTO.wrapSuccessResult(reviewList, "HOD REVIEW SUCCESSFUL");
    }
    return Helper.failedResponse("HOD REVIEW FAILED");
  }

  private Quotation filterFinalQuotation(RequestItem r) {
    Set<Quotation> quotations = r.getQuotations();
    quotations.removeIf(q -> !q.getSupplier().getId().equals(r.getSuppliedBy()));
    return quotations.stream()
        .findFirst()
        .orElseThrow(() -> new NotFoundException("Quotation for request item with id: %s not found".formatted(r.getId())));
  }

  private ResponseEntity<?> endorseRequest(Authentication authentication, List<RequestItem> items) {
    if (!checkAuthorityExist(authentication, EmployeeRole.ROLE_HOD))
      return Helper.failedResponse("FORBIDDEN ACCESS");

    List<RequestItem> endorse =
        items.stream()
            .filter(
                x ->
                    (Objects.isNull(x.getSuppliedBy())
                        && x.getEndorsement().equals(EndorsementStatus.PENDING)
                        && Objects.isNull(x.getEndorsementDate())))
            .map(y -> requestItemService.endorseRequest(y.getId()))
            .collect(Collectors.toList());

    if (!endorse.isEmpty()) {
      CompletableFuture.runAsync(
          () -> {
            BulkRequestItemEvent requestItemEvent = null;
            try {
              requestItemEvent = new BulkRequestItemEvent(this, endorse);
            } catch (Exception e) {
              log.error(e.toString());
            }
            applicationEventPublisher.publishEvent(requestItemEvent);
          });
      return ResponseDTO.wrapSuccessResult(endorse, "REQUEST ENDORSED");
    }
    return Helper.failedResponse("FAILED TO ENDORSE");
  }

  private void sendApproveEmailToGM() {
    CompletableFuture.runAsync(
        () -> {
          Employee generalManager = employeeService.getGeneralManager();
          String message =
              MessageFormat.format(
                  "Dear {0}, Kindly check on request items ready for approval",
                  generalManager.getFullName());
          senderUtil.sendComposeAndSendEmail(
              "APPROVE REQUEST ITEMS", message, emailTemplate, EmailType.REQUEST_ITEM_APPROVAL_GM, generalManager.getEmail());
        });
  }
}
