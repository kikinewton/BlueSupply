package com.logistics.supply.controller;

import com.logistics.supply.dto.RequestItemListDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.LocalPurchaseOrder;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.service.LocalPurchaseOrderService;
import com.logistics.supply.service.RequestItemService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LpoController {

  final RequestItemService requestItemService;
  final LocalPurchaseOrderService localPurchaseOrderService;

  @Operation(summary = "Add LPO draft ", tags = "Procurement")
  @PostMapping(value = "/localPurchaseOrders")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> updateRequestItems(@RequestBody RequestItemListDTO requestItems) {
    try {
      Set<RequestItem> result =
          requestItemService.assignProcurementDetailsToItems(requestItems.getItems());
      if (result.isEmpty()) return failedResponse("MISSING_REQUEST_ITEMS_FOR_LPO");
      LocalPurchaseOrder lpo = new LocalPurchaseOrder();
      lpo.setDeliveryDate(requestItems.getDeliveryDate());
      lpo.setRequestItems(result);
      lpo.setSupplierId(result.stream().findFirst().get().getSuppliedBy());
      LocalPurchaseOrder newLpo = localPurchaseOrderService.saveLPO(lpo);
      if (Objects.nonNull(newLpo)) {
        ResponseDTO response = new ResponseDTO("LPO_DRAFT_CREATED_SUCCESSFULLY", SUCCESS, newLpo);
        return ResponseEntity.ok(response);
      }

    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("LPO_DRAFT_FAILED");
  }

  @GetMapping(value = "/localPurchaseOrders")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> findAllLPOS() {
    List<LocalPurchaseOrder> lpos = localPurchaseOrderService.findAll();
    ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, lpos);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/localPurchaseOrders/{lpoId}")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> findLPOById(@PathVariable("lpoId") int lpoId) {
    LocalPurchaseOrder lpo = localPurchaseOrderService.findLpoById(lpoId);
    if (Objects.nonNull(lpo)) {
      ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, lpo);
      return ResponseEntity.ok(response);
    }
    return failedResponse("FETCH_FAILED");
  }

  private ResponseEntity<ResponseDTO> failedResponse(String message) {
    ResponseDTO failed = new ResponseDTO(message, ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
