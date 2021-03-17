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
import java.util.Optional;
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

    Optional<RequestItem> item = requestItemService.findById(requestItemId);
    if (!item.isPresent()) return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), null, "ERROR");

    if (item.get().getAmount() > 0 && item.get().getQuantity() < 1) {
      return new ResponseDTO<>(HttpStatus.NOT_ACCEPTABLE.name(), null, "ERROR");
    }
    try {
      if (item.get().getEndorsement().equals(EndorsementStatus.ENDORSED)
          && item.get().getStatus().equals(RequestStatus.PENDING)
          && Objects.isNull(item.get().getSupplier())) {
        RequestItem result = procurementService.assignProcurementDetails(item.get(), procurementDTO);
        if (Objects.isNull(result)) return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), null, "ERROR");
        return new ResponseDTO<>(HttpStatus.OK.name(), result, "SUCCESS");
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, "ERROR");
  }
}
