package com.logistics.supply.controller;

import com.logistics.supply.dto.ProcurementDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.service.AbstractRestService;
import com.logistics.supply.util.CommonHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

@RestController
@Slf4j
@RequestMapping(value = "/api")
public class ProcurementController extends AbstractRestService {

  @PutMapping(value = "/procurement/{requestItemId}")
  public ResponseDTO<RequestItem> addProcurementInfo(
      @PathVariable int requestItemId, @RequestBody ProcurementDTO procurementDTO) {
    String[] nullValues = CommonHelper.getNullPropertyNames(procurementDTO);
    System.out.println("count of null properties: " + Arrays.stream(nullValues).count());

    Set<String> l = Set.of(nullValues);
    if (l.size() > 0) {
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, "ERROR");
    }

    RequestItem item = requestItemService.findById(requestItemId).get();
    if (item.getAmount() > 0 && item.getQuantity() < 1) {
      return new ResponseDTO<>(HttpStatus.NOT_ACCEPTABLE.name(), null, "ERROR");
    }
    try {
      if (item.getEndorsement().equals(EndorsementStatus.ENDORSED)
          && item.getStatus().equals(RequestStatus.PENDING)
          && Objects.isNull(item.getSupplier())) {
        RequestItem result = procurementService.assignProcurementDetails(item, procurementDTO);
        return new ResponseDTO<>(HttpStatus.OK.name(), result, "SUCCESS");
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, "ERROR");
  }
}
