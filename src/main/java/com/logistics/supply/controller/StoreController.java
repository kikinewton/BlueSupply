//package com.logistics.supply.controller;
//
//import com.logistics.supply.dto.ResponseDTO;
//import com.logistics.supply.dto.StoreDTO;
//import com.logistics.supply.model.Store;
//import com.logistics.supply.service.EmployeeService;
//import com.logistics.supply.service.StoreService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import javax.validation.Valid;
//import java.util.List;
//
//import static com.logistics.supply.util.Constants.SUCCESS;
//import static com.logistics.supply.util.Helper.failedResponse;
//import static com.logistics.supply.util.Helper.notFound;
//
//@Slf4j
//@RequestMapping("/api")
//@RestController
//@RequiredArgsConstructor
//public class StoreController {
//
//  private final EmployeeService employeeService;
//  private final StoreService storeService;
//
//  @PostMapping(value = "/stores")
//  @PreAuthorize("hasRole('ROLE_ADMIN')")
//  public ResponseEntity<?> createStore(@RequestBody @Valid StoreDTO storeDTO) {
//    Store saved = storeService.save(storeDTO);
//    if (saved == null) return failedResponse("CREATE STORE FAILED");
//    ResponseDTO response = new ResponseDTO("STORE ADDED", SUCCESS, saved);
//    return ResponseEntity.ok(response);
//  }
//
//  @GetMapping(value = "/stores")
//  public ResponseEntity<?> getStores() {
//    List<Store> stores = storeService.findAll();
//    if (stores.isEmpty()) return notFound("STORES NOT FOUND");
//    ResponseDTO response = new ResponseDTO<>("FETCH STORES SUCCESSFUL", SUCCESS, stores);
//    return ResponseEntity.ok(response);
//  }
//
////  @GetMapping(value = "/storesByDepartment")
////  public ResponseEntity<?> findStoreByUserDepartment(Authentication authentication) {
////    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
////    Department department = employee.getDepartment();
////    Store store = storeService.findByDepartment(department);
////    if (store == null) return failedResponse("FETCH_STORE_FAILED");
////    ResponseDTO response = new ResponseDTO<>("FETCH_SUCCESSFUL", SUCCESS, store);
////    return ResponseEntity.ok(response);
////  }
//}
