package com.logistics.supply.controller;

import com.logistics.supply.dto.MultipleEndorsementDTO;
import com.logistics.supply.dto.MultipleItemDTO;
import com.logistics.supply.dto.ReqItems;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.service.AbstractRestService;
import com.logistics.supply.util.CommonHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@RestController
@Slf4j
@RequestMapping("/api")
public class MultiplierItemsController extends AbstractRestService {



  Set<String> nonNulls =
      new HashSet<>(Arrays.asList("name", "reason", "purpose", "quantity", "employee"));
  List<ReqItems> failed = new ArrayList<>();
  List<RequestItem> completed = new ArrayList<>();


  @PostMapping("/multipleRequestItems")
//  @PreAuthorize("hasRole('ROLE_REGULAR'))")
  public ResponseDTO addBulkRequest(@RequestBody MultipleItemDTO multipleItemDTO) throws Exception {
    List<ReqItems> item = multipleItemDTO.getMultipleRequestItem();
    for (ReqItems x : item) {
      String[] nullValues = CommonHelper.getNullPropertyNames(x);
      System.out.println("count: " + Arrays.stream(nullValues).count());
      System.out.println("for " + x.toString());
      Arrays.asList(nullValues).forEach(c -> System.out.println("Null value: " + c));

      Set<String> l = new HashSet<>(Arrays.asList(nullValues));

      if (Arrays.stream(nullValues).count() > 0) {
        log.info("Null value found");
        failed.add(x);
      } else {
        RequestItem result = createRequestItem(x, multipleItemDTO.getEmployee_id());
        if (Objects.nonNull(result)) completed.add(result);
      }
    }
//    failed.forEach((x) -> log.info(x.toString()));
//    Map<String, List<RequestItem>> data = new HashMap<>();
//    data.put("SUCCESS", completed);
    //    data.put("ERROR", failed);

    return new ResponseDTO(HttpStatus.OK.name(), null, "REQUEST SENT");
  }

  private RequestItem createRequestItem(ReqItems itemDTO, int employee_id) {
    RequestItem requestItem = new RequestItem();
    requestItem.setReason(itemDTO.getReason());
    requestItem.setName(itemDTO.getName());
    requestItem.setPurpose(itemDTO.getPurpose());
    requestItem.setQuantity(itemDTO.getQuantity());
    requestItem.setRequestType(itemDTO.getRequestType());
    Employee employee = employeeService.getById(employee_id);
    Department userDepartment = departmentService.getById(itemDTO.getUserDepartment().getId());
    requestItem.setUserDepartment(userDepartment);
    requestItem.setEmployee(employee);
    try {
      RequestItem result = requestItemService.create(requestItem);

      if (Objects.nonNull(result)) return result;
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  @PutMapping(value = "requestItems/bulkEndorse/employees/{employeeId}")
//  @PreAuthorize("hasRole('ROLE_HOD')")
  public ResponseDTO endorseBulkRequestItems(
      @PathVariable("employeeId") int employeeId,
      @RequestBody MultipleEndorsementDTO endorsementDTO) throws Exception {
    boolean isHod = employeeService.verifyEmployeeRole(employeeId, EmployeeRole.ROLE_HOD);
    if (!isHod) return new ResponseDTO(ERROR, HttpStatus.FORBIDDEN.name());
    List<RequestItem> items = endorsementDTO.getEndorsedList();
    List<RequestItem> endorse =
        items.stream()
            .filter(
                x ->
                    (Objects.isNull(x.getSuppliedBy())
                        && x.getEndorsement().equals(EndorsementStatus.PENDING)
                        && Objects.isNull(x.getEndorsementDate())))
            .map(y -> requestItemService.endorseRequest(y.getId()))
            .collect(Collectors.toList());
    endorse.stream().forEach(System.out::println);
    if (endorse.size() > 0) {

//      BulkRequestItemEvent requestItemEvent = new BulkRequestItemEvent(this, endorse);
//      applicationEventPublisher.publishEvent(requestItemEvent);
      return new ResponseDTO(SUCCESS, HttpStatus.OK.name());
    }
    return new ResponseDTO(ERROR, HttpStatus.BAD_REQUEST.name());
  }
}
