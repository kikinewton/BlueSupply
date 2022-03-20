package com.logistics.supply.controller;

import com.logistics.supply.dto.BulkRequestItemDTO;
import com.logistics.supply.dto.FloatOrPettyCashDTO;
import com.logistics.supply.dto.MultipleItemDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.ProcurementType;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.enums.UpdateStatus;
import com.logistics.supply.event.*;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.FloatOrderRepository;
import com.logistics.supply.repository.PettyCashOrderRepository;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.PettyCashService;
import com.logistics.supply.service.RequestItemService;
import com.logistics.supply.util.IdentifierUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.SUCCESS;
import static com.logistics.supply.util.Helper.failedResponse;

@RestController
@Slf4j
@RequestMapping("/api")
public class MultiplierItemsController {

  @Autowired EmployeeService employeeService;
  @Autowired RequestItemService requestItemService;
  @Autowired PettyCashService pettyCashService;
  @Autowired ApplicationEventPublisher applicationEventPublisher;
  @Autowired FloatOrderRepository floatOrderRepository;
  @Autowired PettyCashOrderRepository pettyCashOrderRepository;

  @PostMapping("/multipleRequestItems")
  public ResponseEntity<?> addBulkRequest(
      @RequestBody @Valid MultipleItemDTO multipleItemDTO, Authentication authentication)
      throws Exception {
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    List<RequestItem> createdItems =
        multipleItemDTO.getMultipleRequestItem().stream()
            .map(i -> requestItemService.createRequestItem(i, employee))
            .collect(Collectors.toList());
    if (createdItems.isEmpty()) return failedResponse("FAILED");
    CompletableFuture.runAsync(
        () -> {
          BulkRequestItemEvent requestItemEvent = null;
          try {
            requestItemEvent = new BulkRequestItemEvent(this, createdItems);
          } catch (Exception e) {
            log.error(e.toString());
          }
          applicationEventPublisher.publishEvent(requestItemEvent);
        });
    ResponseDTO response = new ResponseDTO("CREATED REQUEST ITEMS", SUCCESS, createdItems);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/bulkFloatOrPettyCash/{procurementType}")
  public ResponseEntity<?> addBulkFloatOrPettyCash(
      @Valid @RequestBody FloatOrPettyCashDTO bulkItems,
      @PathVariable("procurementType") ProcurementType procurementType,
      Authentication authentication) {
    if (authentication == null) return failedResponse("Auth token is required");
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    if (procurementType.equals(ProcurementType.FLOAT)) {
      FloatOrder order = new FloatOrder();
      order.setRequestedBy(bulkItems.getRequestedBy());
      order.setRequestedByPhoneNo(bulkItems.getRequestedByPhoneNo());
      order.setAmount(bulkItems.getAmount());
      order.setDepartment(employee.getDepartment());
      order.setCreatedBy(employee);
      order.setDescription(bulkItems.getDescription());
      String ref =
          IdentifierUtil.idHandler(
              "FLT",
              employee.getDepartment().getName(),
              String.valueOf(floatOrderRepository.count()));
      order.setFloatOrderRef(ref);
      bulkItems.getItems().stream()
          .forEach(
              i -> {
                Floats fl = new Floats();
                fl.setDepartment(employee.getDepartment());
                fl.setEstimatedUnitPrice(i.getUnitPrice());
                fl.setItemDescription(i.getName());
                fl.setQuantity(i.getQuantity());
                fl.setFloatOrder(order);
                fl.setCreatedBy(employee);
                fl.setFloatRef(ref);
                order.addFloat(fl);
              });
      FloatOrder saved = null;
      try {
        saved = floatOrderRepository.save(order);
      } catch (Exception e) {
        log.error(e.toString());
      }
      if (Objects.nonNull(saved)) {
          FloatOrder finalSaved = saved;
          CompletableFuture.runAsync(
            () -> {
              FloatEvent floatEvent = new FloatEvent(this, finalSaved);
              applicationEventPublisher.publishEvent(floatEvent);
            });
        ResponseDTO response = new ResponseDTO("CREATED FLOAT ITEMS", SUCCESS, saved);
        return ResponseEntity.ok(response);
      }
      return failedResponse("FAILED TO CREATE FLOATS");
    }
    if (procurementType.equals(ProcurementType.PETTY_CASH)) {
      PettyCashOrder pettyCashOrder = new PettyCashOrder();
      pettyCashOrder.setRequestedBy(bulkItems.getRequestedBy());
//        Set<RequestDocument> documents = new HashSet<>();
//        bulkItems.getItems().stream().forEach(i -> {
//            documents.add(i.getDocuments().);
//        });
//        pettyCashOrder.setSupportingDocument(objectStream);
      pettyCashOrder.setRequestedByPhoneNo(bulkItems.getRequestedByPhoneNo());
      AtomicReference<String> ref = new AtomicReference<>("");
      bulkItems.getItems().stream()
          .forEach(
              i -> {
                PettyCash pettyCash = new PettyCash();
                pettyCash.setDepartment(employee.getDepartment());
                pettyCash.setName(i.getName());
                pettyCash.setPurpose(i.getPurpose());
                pettyCash.setAmount(i.getUnitPrice());
                pettyCash.setQuantity(i.getQuantity());
                pettyCash.setCreatedBy(employee);
                ref.set(
                    IdentifierUtil.idHandler(
                        "PTC",
                        employee.getDepartment().getName(),
                        String.valueOf(pettyCashService.count())));
                pettyCash.setPettyCashRef(String.valueOf(ref));
                pettyCashOrder.addPettyCash(pettyCash);
              });
      PettyCashOrder saved = null;
      try {
        pettyCashOrder.setPettyCashOrderRef(String.valueOf(ref));
        if (pettyCashOrder != null) saved = pettyCashOrderRepository.save(pettyCashOrder);

      } catch (Exception e) {
        log.error(e.toString());
      }
      if (!saved.getPettyCash().isEmpty()) {
        PettyCashOrder finalSaved = saved;
        CompletableFuture.runAsync(
            () -> {
              PettyCashEvent pettyCashEvent = new PettyCashEvent(this, finalSaved.getPettyCash());
              applicationEventPublisher.publishEvent(pettyCashEvent);
            });

        ResponseDTO response =
            new ResponseDTO("CREATED PETTY CASH ITEMS", SUCCESS, saved.getPettyCash());
        return ResponseEntity.ok(response);
      }
      return failedResponse("FAILED TO CREATE PETTY CASH");
    }
    return failedResponse("FAILED");
  }

  @PutMapping(value = "requestItems/updateStatus/{statusChange}")
  @PreAuthorize("hasRole('ROLE_HOD') or hasRole('ROLE_GENERAL_MANAGER')")
  public ResponseEntity<?> updateMultipleRequestItem(
      @Valid @RequestBody BulkRequestItemDTO bulkRequestItem,
      @PathVariable("statusChange") UpdateStatus statusChange,
      Authentication authentication)
      throws Exception {
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
        return failedResponse("UPDATE STATUS FAILED");
    }
  }

  private Boolean checkAuthorityExist(Authentication authentication, EmployeeRole role) {
    return authentication.getAuthorities().stream()
        .map(x -> x.getAuthority().equalsIgnoreCase(role.name()))
        .filter(x -> x == true)
        .findAny()
        .get();
  }

  private ResponseEntity<?> cancelRequest(Authentication authentication, List<RequestItem> items) {
    int employeeId = employeeService.findEmployeeByEmail(authentication.getName()).getId();
    List<CancelledRequestItem> cancels =
        items.stream()
            .map(i -> requestItemService.cancelRequest(i.getId(), employeeId))
            .filter(c -> Objects.nonNull(c))
            .collect(Collectors.toList());
    if (cancels.size() > 0) {
      CancelRequestItemEvent cancelRequestItemEvent = new CancelRequestItemEvent(this, cancels);
      applicationEventPublisher.publishEvent(cancelRequestItemEvent);
      ResponseDTO response = new ResponseDTO("CANCELLED REQUEST", SUCCESS, cancels);
      return ResponseEntity.ok(response);
    }
    return failedResponse("CANCEL REQUEST FAILED");
  }

  private ResponseEntity<?> approveRequestGM(
      Authentication authentication, List<RequestItem> items) {
    if (!checkAuthorityExist(authentication, EmployeeRole.ROLE_GENERAL_MANAGER))
      return failedResponse("FORBIDDEN ACCESS");
    List<Boolean> approvedItems =
        items.stream()
            .map(item -> requestItemService.approveRequest(item.getId()))
            .map(y -> y.equals(Boolean.TRUE))
            .collect(Collectors.toList());
    if (approvedItems.size() > 0) {
      List<RequestItem> approved =
          items.stream()
              .filter(r -> requestItemService.findApprovedItemById(r.getId()).isPresent())
              .map(a -> requestItemService.findById(a.getId()).get())
              .collect(Collectors.toList());
      ApproveRequestItemEvent requestItemEvent = new ApproveRequestItemEvent(this, approved);
      applicationEventPublisher.publishEvent(requestItemEvent);
      ResponseDTO response = new ResponseDTO("APPROVAL SUCCESSFUL", SUCCESS, approved);
      return ResponseEntity.ok(response);
    }
    return failedResponse("APPROVAL FAILED");
  }

  private ResponseEntity<?> reviewRequestAfterProcurement(
      Authentication authentication, List<RequestItem> items) {
    if (!checkAuthorityExist(authentication, EmployeeRole.ROLE_HOD))
      return failedResponse("FORBIDDEN ACCESS");
    Set<RequestItem> reviewList =
        items.stream()
            .filter(i -> Objects.nonNull(i.getRequestCategory()))
            .map(r -> requestItemService.updateRequestReview(r.getId(), RequestReview.HOD_REVIEW))
            .collect(Collectors.toSet());
    if (!reviewList.isEmpty()) {
      ResponseDTO response = new ResponseDTO("HOD REVIEW SUCCESSFUL", SUCCESS, reviewList);
      CompletableFuture.runAsync(() -> {});

      return ResponseEntity.ok(response);
    }
    return failedResponse("HOD REVIEW FAILED");
  }

  private ResponseEntity<?> endorseRequest(Authentication authentication, List<RequestItem> items)
      throws Exception {
    if (!checkAuthorityExist(authentication, EmployeeRole.ROLE_HOD))
      return failedResponse("FORBIDDEN ACCESS");

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
      ResponseDTO response = new ResponseDTO("REQUEST ENDORSED", SUCCESS, endorse);
      return ResponseEntity.ok(response);
    }
    return failedResponse("FAILED TO ENDORSE");
  }
}
