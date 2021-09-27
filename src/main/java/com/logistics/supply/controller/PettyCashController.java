package com.logistics.supply.controller;

import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.PettyCash;
import com.logistics.supply.service.PettyCashService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@Slf4j
@RestController
@RequestMapping("/api")
public class PettyCashController {
  @Autowired PettyCashService pettyCashService;

  @PostMapping("/pettyCash")
  public ResponseEntity<?> createPettyCash(@Valid @RequestBody PettyCash pettyCash) {
    try {
      PettyCash cash = pettyCashService.save(pettyCash);
      if (Objects.nonNull(cash)) {
        ResponseDTO successResponse = new ResponseDTO("PETTY_CASH_CREATED", SUCCESS, cash);
        return ResponseEntity.ok(successResponse);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    ResponseDTO failedResponse = new ResponseDTO(ERROR, null, "FAILED");
    return ResponseEntity.badRequest().body(failedResponse);
  }

  @GetMapping("/pettyCash")
  public ResponseEntity<?> findAllPettyCash(
      @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
      @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
    try {
      List<PettyCash> cashList = pettyCashService.findAllPettyCash(pageNo, pageSize);
      ResponseDTO successResponse = new ResponseDTO("FETCH_PETTY_CASH", SUCCESS, cashList);
      return ResponseEntity.ok(successResponse);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    ResponseDTO failed = new ResponseDTO("FETCH_FAILED", ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
