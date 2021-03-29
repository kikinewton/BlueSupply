package com.logistics.supply.controller;

import com.logistics.supply.dto.MultipleEndorsementDTO;
import com.logistics.supply.dto.MultipleItemDTO;
import com.logistics.supply.dto.ReqItems;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.enums.EmployeeLevel;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.service.AbstractRestService;
import com.logistics.supply.util.CommonHelper;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.logistics.supply.util.CommonHelper.buildEmail;
import static com.logistics.supply.util.Constants.*;

@RestController
@Slf4j
@RequestMapping("/api")
public class MultiplierItemsController extends AbstractRestService {

  private final EmailSender emailSender;

  Set<String> nonNulls =
      new HashSet<>(Arrays.asList("name", "reason", "purpose", "quantity", "employee"));
  List<ReqItems> failed = new ArrayList<>();
  List<RequestItem> completed = new ArrayList<>();

  public MultiplierItemsController(EmailSender emailSender) {
    this.emailSender = emailSender;
  }

  @PostMapping("/multipleRequestItems")
  public ResponseDTO addBulkRequest(@RequestBody MultipleItemDTO multipleItemDTO) {
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
    failed.forEach((x) -> log.info(x.toString()));
    //    Map<String, Object> data = new HashMap<>();
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
    Employee employee = employeeService.getById(employee_id);
    requestItem.setEmployee(employee);
    try {
      RequestItem result = requestItemService.create(requestItem);

      if (Objects.nonNull(result)) {
        try {
          Employee hod = employeeService.getHODOfDepartment(result.getEmployee().getDepartment());
          String emailContent =
              buildEmail(
                  hod.getLastName(),
                  REQUEST_PENDING_ENDORSEMENT_LINK,
                  REQUEST_PENDING_ENDORSEMENT_TITLE,
                  REQUEST_ENDORSEMENT_MAIL);
          emailSender.sendMail(hod.getEmail(), EmailType.NEW_REQUEST_MAIL, emailContent);
        } catch (Exception e) {
          log.error(e.getMessage());
        }

        return result;
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  @PutMapping(value = "requestItems/bulkEndorse/employees/{employeeId}")
  public ResponseDTO endorseBulkRequestItems(
      @PathVariable("employeeId") int employeeId,
      @RequestBody MultipleEndorsementDTO endorsementDTO) {

    boolean isHod = employeeService.verifyEmployeeRole(employeeId, EmployeeLevel.HOD.name());
    if (!isHod) return new ResponseDTO(ERROR ,HttpStatus.FORBIDDEN.name());
    List<RequestItem> items = endorsementDTO.getEndorsedList();

    List<String> endorse =
        items.stream()
            .filter(
                x ->
                    Objects.isNull(x.getSupplier())
                        && x.getEndorsement().equals(EndorsementStatus.ENDORSED))
            .map(y -> requestItemService.endorseRequest(y.getId()))
            .collect(Collectors.toList());
    if (endorse.size() > 0) {
      String emailContent =
          buildEmail(
              "PROCUREMENT",
              REQUEST_PENDING_PROCUREMENT_DETAILS_LINK,
              REQUEST_PENDING_PROCUREMENT_DETAILS_TITLE,
              PROCUREMENT_DETAILS_MAIL);
      try {
        emailSender.sendMail(
            DEFAULT_PROCUREMENT_MAIL, EmailType.PROCUREMENT_REVIEW_MAIL, emailContent);
      } catch (Exception e) {
        log.error(e.getMessage());
        e.printStackTrace();
      }
      return new ResponseDTO(SUCCESS, HttpStatus.OK.name());
    }
    return new ResponseDTO(ERROR, HttpStatus.BAD_REQUEST.name());
  }
}
