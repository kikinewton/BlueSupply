package com.logistics.supply.controller;

import com.logistics.supply.dto.RequestItemDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.enums.EmployeeLevel;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.service.AbstractRestService;
import jdk.jshell.EvalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.logistics.supply.util.Constants.*;

@RestController
@Slf4j
@RequestMapping(value = "/api")
public class RequestItemController extends AbstractRestService {

  @GetMapping(value = "/requestItems")
  public ResponseDTO<List<RequestItem>> getAll(
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "15") int pageSize) {
    try {
      List<RequestItem> itemList = requestItemService.findAll(pageNo, pageSize);
      if (!itemList.isEmpty()) return new ResponseDTO<>("SUCCESS", itemList, "REQUEST_ITEMS_FOUND");
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO<>("ERROR", null, "REQUEST_ITEMS_NOT_FOUND");
  }

  @GetMapping(value = "/requestItems/{requestItemId}")
  public ResponseDTO<RequestItem> getById(@PathVariable int requestItemId) {
    try {
      Optional<RequestItem> item = requestItemService.findById(requestItemId);
      if (item.isPresent()) return new ResponseDTO<>("SUCCESS", item.get(), "REQUEST_ITEM_FOUND");
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO<>("ERROR", null, "REQUEST_ITEM_NOT_FOUND");
  }

  @GetMapping(value = "/requestItems/departments/{departmentId}/employees/{employeeId}")
  public ResponseDTO<List<RequestItem>> getRequestItemsByDepartmemnt(
      @PathVariable("departmentId") int departmentId, @PathVariable("employeeId") int employeeId) {
    if (!employeeService.verifyEmployeeRole(employeeId, "HOD"))
      return new ResponseDTO<>(HttpStatus.FORBIDDEN.name(), null, "OPERATION_NOT_ALLOWED");
    try {
      List<RequestItem> items = requestItemService.getRequestItemForHOD(departmentId);
      return new ResponseDTO<>(SUCCESS, items, "REQUEST_ITEM_FOUND");
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), null, "REQUEST_ITEM_NOT_FOUND");
  }

  @PostMapping(value = "/requestItems")
  public ResponseDTO<RequestItem> createRequestItem(@RequestBody RequestItemDTO itemDTO) {
    RequestItem requestItem = new RequestItem();
    requestItem.setReason(itemDTO.getReason());
    requestItem.setName(itemDTO.getName());
    requestItem.setPurpose(itemDTO.getPurpose());
    requestItem.setQuantity(itemDTO.getQuantity());
    requestItem.setEmployee(itemDTO.getEmployee());
    try {
      RequestItem result = requestItemService.create(requestItem);
      if (Objects.nonNull(result))
        return new ResponseDTO<>(HttpStatus.CREATED.name(), result, "REQUEST_ITEM_CREATED");
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, "REQUEST_ITEM_NOT_CREATED");
  }

  @PutMapping(value = "/requestItems/{requestItemId}/employees/{employeeId}/endorse")
  public ResponseDTO endorseRequest(
      @PathVariable("requestItemId") int requestItemId,
      @PathVariable("employeeId") int employeeId) {
    try {

      Employee employee = employeeService.getById(employeeId);

      if (Objects.nonNull(employee) && employee.getRoles().equals(EmployeeLevel.HOD.name())) {
        Optional<RequestItem> requestItem = requestItemService.findById(requestItemId);

        if (requestItem.isPresent()
            && Objects.isNull(requestItem.get().getSupplier())
            && !requestItem.get().getEndorsement().equals(EndorsementStatus.ENDORSED)) {
          String message = requestItemService.endorseRequest(requestItem.get().getId());
          log.info(message);
          return new ResponseDTO("SUCCESS", HttpStatus.OK.name());
        }
        return new ResponseDTO("ERROR", HttpStatus.NOT_FOUND.name());
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO("ERROR", HttpStatus.NOT_FOUND.name());
  }

  @PutMapping(value = "/requestItems/{requestItemId}/employees/{employeeId}/approve")
  public ResponseDTO approveRequest(@PathVariable int requestItemId, @PathVariable int employeeId) {
    if (Objects.isNull(requestItemId) && Objects.isNull(employeeId)) {
      return new ResponseDTO("ERROR", HttpStatus.NOT_FOUND.name());
    }
    try {
      Employee employee = employeeService.getById(employeeId);
      if (Objects.nonNull(employee)
          && employee.getRoles().equals(EmployeeLevel.GENERAL_MANAGER.name())) {
        Optional<RequestItem> requestItem = requestItemService.findById(requestItemId);
        if (requestItem.isPresent()
            && requestItem.get().getStatus().equals(RequestStatus.PENDING)
            && requestItem.get().getEndorsement().equals(EndorsementStatus.ENDORSED)) {
          requestItemService.approveRequest(requestItemId);
          log.info("Approval completed");
          return new ResponseDTO("SUCCESS", HttpStatus.OK.name());
        }
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO("ERROR", HttpStatus.NOT_FOUND.name());
  }

  @PutMapping(value = "/requestItems/{requestItemId}/employees/{employeeId}/cancel")
  public ResponseDTO cancelRequest(@PathVariable int requestItemId, @PathVariable int employeeId) {
    if (Objects.isNull(requestItemId) && Objects.isNull(employeeId)) {
      return new ResponseDTO("ERROR", HttpStatus.NOT_FOUND.name());
    }
    try {
      Employee employee = employeeService.getById(employeeId);
      if (Objects.nonNull(employee)
          && (employee.getRoles().equals(EmployeeLevel.HOD.name())
              || employee.getRoles().equals(EmployeeLevel.GENERAL_MANAGER.name()))) {
        Optional<RequestItem> requestItem = requestItemService.findById(requestItemId);
        requestItem.ifPresent(x -> requestItemService.cancelRequest(requestItemId, employeeId));
        return new ResponseDTO("SUCCESS", HttpStatus.OK.name());
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO("ERROR", HttpStatus.NOT_FOUND.name());
  }

  @GetMapping("/requestItems/approvedItems")
  public ResponseDTO<List<RequestItem>> getApprovedRequestItems() {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemService.getApprovedItems());
      return new ResponseDTO<>(HttpStatus.FOUND.name(), items, "SUCCESS");
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), items, "ERROR");
  }

  @GetMapping("/requestItems/endorsedItems")
  public ResponseDTO<List<RequestItem>> getEndorsedRequestItems() {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemService.getEndorsedItems());
      return new ResponseDTO<>(HttpStatus.FOUND.name(), items, "SUCCESS");
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), items, "ERROR");
  }

  @GetMapping(value = "/requestItems/employees/{employeeId}")
  public ResponseDTO<List<RequestItem>> getCountNofEmployeeRequestItem(
      @PathVariable("employeeId") int employeeId) {
    List<RequestItem> items = new ArrayList<>();
    try {
      int count = 10;
      items.addAll(requestItemService.getCountNofEmployeeRequest(count, employeeId));
      return new ResponseDTO<>(HttpStatus.FOUND.name(), items, "SUCCESS");
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), items, "ERROR");
  }

  @GetMapping(value = "/requestItems/employees/{employeeId}/generalManager")
  public ResponseDTO<List<RequestItem>> getRequestForGM(
      @PathVariable("employeeId") int employeeId) {
    Employee employee = employeeService.findEmployeeById(employeeId);
    if (Objects.isNull(employee))
      return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), null, ERROR);
    if (employeeService.verifyEmployeeRole(employeeId, EmployeeLevel.GENERAL_MANAGER.name())) {
      List<RequestItem> items = new ArrayList<>();
      items.addAll(requestItemService.getRequestItemForGeneralManager());
      return new ResponseDTO<>(HttpStatus.OK.name(), items, SUCCESS);
    }
    return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), null, ERROR);
  }
}
