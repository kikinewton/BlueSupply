package com.logistics.supply.controller;

import com.logistics.supply.dto.MultipleSuppliersDTO;
import com.logistics.supply.dto.ProcurementDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.enums.EmployeeLevel;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.service.AbstractRestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.logistics.supply.util.CommonHelper.*;
import static com.logistics.supply.util.Constants.*;

@RestController
@Slf4j
@RequestMapping(value = "/api")
public class ProcurementController extends AbstractRestService {

  @Autowired private final EmailSender emailSender;

  public ProcurementController(EmailSender emailSender) {
    this.emailSender = emailSender;
  }

  @PutMapping(value = "/procurement/{employeeId}/requestItem/{requestItemId}")
  public ResponseDTO<RequestItem> addProcurementInfo(
      @PathVariable("employeeId") int employeeId,
      @PathVariable("requestItemId") int requestItemId,
      @RequestBody ProcurementDTO procurementDTO) {
    String[] nullValues = getNullPropertyNames(procurementDTO);
    System.out.println("count of null properties: " + Arrays.stream(nullValues).count());

    Set<String> l = new HashSet<>(Arrays.asList(nullValues));
    if (l.size() > 0) {
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
    }

    Employee employee = employeeService.findEmployeeById(employeeId);
    System.out.println(
        EmployeeLevel.PROCUREMENT_OFFICER.name() + " " + employee.getRole().toString());
    if (Objects.isNull(employee)
        | !employee.getRole().equals(EmployeeLevel.PROCUREMENT_OFFICER.name()))
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);

    Optional<RequestItem> item = requestItemService.findById(requestItemId);
    if (!item.isPresent()) return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), null, ERROR);

    if (item.get().getTotalPrice().doubleValue() > 0 && item.get().getQuantity() < 1) {
      return new ResponseDTO<>(HttpStatus.NOT_ACCEPTABLE.name(), null, ERROR);
    }
    try {
      System.out.println("Trying to endorse after checking conditions");
      if (item.get().getEndorsement().equals(EndorsementStatus.ENDORSED)
          && item.get().getStatus().equals(RequestStatus.PENDING)
          && Objects.isNull(item.get().getSuppliedBy())) {
        System.out.println("Request can now be accessed for procurement details");
        RequestItem result =
            procurementService.assignProcurementDetails(item.get(), procurementDTO);
        requestItemService.saveRequest(item.get(), employee, RequestStatus.PENDING);
        if (Objects.isNull(result))
          return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), null, ERROR);
        Employee generalManager = employeeService.getGeneralManager();
        if (Objects.nonNull(generalManager)) {
          String emailContent =
              buildEmail(
                  generalManager.getLastName(),
                  REQUEST_PENDING_APPROVAL_LINK,
                  REQUEST_PENDING_APPROVAL_TITLE,
                  REQUEST_APPROVAL_MAIL);
          String generalManagerEmail = generalManager.getEmail();
          emailSender.sendMail(
              generalManagerEmail, EmailType.GENERAL_MANAGER_APPROVAL_MAIL, emailContent);
        }
        return new ResponseDTO<>(HttpStatus.OK.name(), result, SUCCESS);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

  @PutMapping(value = "/procurement/assignSuppliers/requestItems/{requestItemId}")
  public ResponseDTO<RequestItem> addSuppliersToRequestItem(
      @PathVariable("requestItemId") int requestItemId,
      @RequestBody MultipleSuppliersDTO suppliers) {
    Optional<RequestItem> requestItem = requestItemService.findById(requestItemId);
    if (!requestItem.isPresent())
      return new ResponseDTO<>(ERROR, null, HttpStatus.NOT_FOUND.name());

    RequestItem req = procurementService.assignMultipleSuppliers(requestItem.get(), suppliers);
    if (Objects.nonNull(req)) return new ResponseDTO<>(SUCCESS, req, HttpStatus.OK.name());

    return new ResponseDTO<>(ERROR, null, HttpStatus.BAD_REQUEST.name());
  }
}
