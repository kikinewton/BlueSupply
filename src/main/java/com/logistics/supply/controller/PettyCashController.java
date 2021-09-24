package com.logistics.supply.controller;

import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.PettyCash;
import com.logistics.supply.service.PettyCashService;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
  public ResponseDTO<PettyCash> createPettyCash(@Valid @RequestBody PettyCash pettyCash) {
    try {
      PettyCash cash = pettyCashService.save(pettyCash);
      if (Objects.nonNull(cash)) {
        return new ResponseDTO<>(HttpStatus.CREATED.name(), cash, SUCCESS);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

  @GetMapping("/pettyCash")
  public ResponseDTO<List<PettyCash>> findAllPettyCash(
      @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
      @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
    try {
      var cashList = pettyCashService.findAllPettyCash(pageNo, pageSize);
      return new ResponseDTO<>(HttpStatus.OK.name(), cashList, SUCCESS);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }
}
