package com.logistics.supply.controller;

import com.logistics.supply.dto.ResponseDto;
import com.logistics.supply.dto.StoreDto;
import com.logistics.supply.model.Store;
import com.logistics.supply.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class StoreController {

  private final StoreService storeService;

  @PostMapping(value = "/stores")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<ResponseDto<Store>> createStore(
          @Valid @RequestBody StoreDto storeDTO) {

    Store saved = storeService.save(storeDTO);
    return ResponseDto.wrapSuccessResult(saved, "STORE ADDED");
  }

  @PutMapping(value = "/stores/{storeId}")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<ResponseDto<Void>> updateStore(
          @PathVariable("storeId") int storeId,
          @Valid @RequestBody StoreDto storeDTO) {

    storeService.update(storeId, storeDTO);
    return ResponseDto.wrapSuccessResult(null, "STORE UPDATE");
  }


  @GetMapping(value = "/stores")
  public ResponseEntity<ResponseDto<List<Store>>> getStores() {

    List<Store> stores = storeService.findAll();
    return ResponseDto.wrapSuccessResult(stores, "FETCH STORES SUCCESSFUL");
  }

}
