package com.logistics.supply.controller;

import com.logistics.supply.dto.RequestItemDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.enums.EmployeeLevel;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.service.AbstractRestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.logistics.supply.util.CommonHelper.buildEmail;
import static com.logistics.supply.util.Constants.*;

@RestController
@Slf4j
@RequestMapping(value = "/api")
public class RequestItemController extends AbstractRestService {

  private final EmailSender emailSender;

  public RequestItemController(EmailSender emailSender) {
    this.emailSender = emailSender;
  }

  @GetMapping(value = "/requestItems")
//  @PreAuthorize("hasRole('REGULAR')")
  public ResponseDTO<List<RequestItem>> getAll(
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "50") int pageSize) {
    try {
      List<RequestItem> itemList = requestItemService.findAll(pageNo, pageSize);
      if (!itemList.isEmpty())
        return new ResponseDTO<>(
            "SUCCESS",
            itemList.stream()
                .sorted(Comparator.comparing(RequestItem::getCreatedDate))
                .collect(Collectors.toList()),
            "REQUEST_ITEMS_FOUND");
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
  @PreAuthorize("hasRole('ROLE_HOD')")
  public ResponseDTO<List<RequestItem>> getRequestItemsByDepartment(
      @PathVariable("departmentId") int departmentId, @PathVariable("employeeId") int employeeId) {
    if (!employeeService.verifyEmployeeRole(employeeId,  EmployeeRole.ROLE_HOD))
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
        return new ResponseDTO<>(HttpStatus.CREATED.name(), result, "REQUEST_ITEM_CREATED");
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, "REQUEST_ITEM_NOT_CREATED");
  }

  @PutMapping(value = "/requestItems/{requestItemId}/employees/{employeeId}/endorse")
  @PreAuthorize("hasRole('ROLE_HOD')")
  public ResponseDTO endorseRequest(
      @PathVariable("requestItemId") int requestItemId,
      @PathVariable("employeeId") int employeeId) {
    try {

      Employee employee = employeeService.getById(employeeId);

      if (Objects.nonNull(employee) && employee.getRole().contains(EmployeeLevel.HOD)) {
        Optional<RequestItem> requestItem = requestItemService.findById(requestItemId);

        if (requestItem.isPresent()
            && Objects.isNull(requestItem.get().getSuppliedBy())
            && !requestItem.get().getEndorsement().equals(EndorsementStatus.ENDORSED)) {
          RequestItem request = requestItemService.endorseRequest(requestItem.get().getId());
          if (Objects.nonNull(request)) {
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
            return new ResponseDTO("SUCCESS", HttpStatus.OK.name());
          }
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
          && employee.getRole().contains(EmployeeLevel.GENERAL_MANAGER)) {
        Optional<RequestItem> requestItem = requestItemService.findById(requestItemId);
        if (requestItem.isPresent()
            && requestItem.get().getStatus().equals(RequestStatus.PENDING)
            && requestItem.get().getEndorsement().equals(EndorsementStatus.ENDORSED)) {
          boolean approved = requestItemService.approveRequest(requestItemId);
          if (approved) {
            log.info("Approval completed");
            String emailContent =
                buildEmail(
                    "PROCUREMENT",
                    APPROVED_REQUEST_LINK,
                    REQUEST_APPROVED_TITLE,
                    APPROVED_REQUEST_MAIL);
            try {
              emailSender.sendMail(
                  DEFAULT_PROCUREMENT_MAIL, EmailType.APPROVED_REQUEST_MAIL, emailContent);
            } catch (Exception e) {
              System.out.println(e.getMessage());
              log.error(e.getMessage());
            }
            return new ResponseDTO("SUCCESS", HttpStatus.OK.name());
          }
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
          && (employee.getRole().contains(EmployeeLevel.HOD)
              || employee.getRole().contains(EmployeeLevel.GENERAL_MANAGER))) {
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
    if (employeeService.verifyEmployeeRole(employeeId, EmployeeRole.ROLE_GENERAL_MANAGER)) {
      List<RequestItem> items = new ArrayList<>();
      items.addAll(requestItemService.getRequestItemForGeneralManager());
      List<RequestItem> result =
          items.stream()
              .sorted(Comparator.comparing(RequestItem::getCreatedDate).reversed())
              .collect(Collectors.toList());
      return new ResponseDTO<>(HttpStatus.OK.name(), result, SUCCESS);
    }
    return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), null, ERROR);
  }


}
