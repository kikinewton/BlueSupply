package com.logistics.supply.controller;

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
import com.logistics.supply.util.CommonHelper;
import com.logistics.supply.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
    String[] nullValues = CommonHelper.getNullPropertyNames(procurementDTO);
    System.out.println("count of null properties: " + Arrays.stream(nullValues).count());

    Set<String> l = new HashSet<>(Arrays.asList(nullValues));
    if (l.size() > 0) {
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
    }

    Employee employee = employeeService.findEmployeeById(employeeId);
    if (Objects.isNull(employee) || !employee.getRoles().equals(EmployeeLevel.PROCUREMENT_OFFICER))
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);

    Optional<RequestItem> item = requestItemService.findById(requestItemId);
    if (!item.isPresent()) return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), null, ERROR);

    if (item.get().getAmount() > 0 && item.get().getQuantity() < 1) {
      return new ResponseDTO<>(HttpStatus.NOT_ACCEPTABLE.name(), null, ERROR);
    }
    try {
      if (item.get().getEndorsement().equals(EndorsementStatus.ENDORSED)
          && item.get().getStatus().equals(RequestStatus.PENDING)
          && Objects.isNull(item.get().getSupplier())) {
        RequestItem result =
            procurementService.assignProcurementDetails(item.get(), procurementDTO);
        if (Objects.isNull(result))
          return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), null, ERROR);
        String emailContent =
            CommonHelper.buildEmail(
                "General Manager",
                REQUEST_PENDING_APPROVAL_LINK,
                REQUEST_PENDING_APPROVAL_TITLE,
                REQUEST_APPROVAL_MAIL);
        emailSender.sendMail(
            "bsupply901@gmail.com", "", EmailType.GENERAL_MANAGER_APPROVAL_MAIL, emailContent);
        return new ResponseDTO<>(HttpStatus.OK.name(), result, SUCCESS);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }
}
