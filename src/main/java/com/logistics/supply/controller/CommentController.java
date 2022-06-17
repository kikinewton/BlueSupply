package com.logistics.supply.controller;

import com.logistics.supply.dto.BulkCommentDTO;
import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.enums.ProcurementType;
import com.logistics.supply.model.*;
import com.logistics.supply.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

import static com.logistics.supply.util.Constants.SUCCESS;
import static com.logistics.supply.util.Helper.failedResponse;
import static com.logistics.supply.util.Helper.notFound;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {
  private final RequestItemCommentService requestItemCommentService;
  private final FloatCommentService floatCommentService;
  private final PettyCashCommentService pettyCashCommentService;
  private final EmployeeService employeeService;
  private final RoleService roleService;

  @PostMapping("/comment/{procurementType}")
  public ResponseEntity<?> addRequestComment(
      @Valid @RequestBody BulkCommentDTO comments,
      @Valid @PathVariable ProcurementType procurementType,
      Authentication authentication) {
    try {
      Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
      EmployeeRole role = roleService.getEmployeeRole(authentication);

      switch (procurementType) {
        case LPO:
          List<RequestItemComment> commentResult = requestItemCommentService.saveBulkRequestItemComments(comments, employee, role);
          if (commentResult.isEmpty()) return failedResponse("COMMENT NOT SAVED");
          ResponseDTO response = new ResponseDTO("COMMENTS SAVED", SUCCESS, commentResult);
          return ResponseEntity.ok(response);
        case FLOAT:
          List<FloatComment> floatComments = floatCommentService.saveFloatComments(comments, employee, role);
          if (floatComments.isEmpty()) return failedResponse("COMMENT NOT SAVED");
          ResponseDTO responseFloat = new ResponseDTO("COMMENTS SAVED", SUCCESS, floatComments);
          return ResponseEntity.ok(responseFloat);
        case PETTY_CASH:
          List<PettyCashComment> pettyCashComments = pettyCashCommentService.savePettyCashComments(comments, employee, role);
          if (pettyCashComments.isEmpty()) return failedResponse("COMMENTS NOT SAVED");
          ResponseDTO responsePettyComment =
              new ResponseDTO("COMMENT SAVED", SUCCESS, pettyCashComments);
          return ResponseEntity.ok(responsePettyComment);
      }

    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("ADD COMMENT FAILED");
  }

  @GetMapping(value = "/comment/unread")
  public ResponseEntity<?> findUnReadComment(
      @RequestParam ProcurementType procurementType, Authentication authentication) {
    try {
      switch (procurementType) {
        case LPO:
          Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
          List<CommentResponse<RequestItemComment>> comments =
              requestItemCommentService.findCommentsNotRead(employee.getId());
          ResponseDTO responseLpoComment =
              new ResponseDTO("FETCH UNREAD LPO COMMENT", SUCCESS, comments);
          return ResponseEntity.ok(responseLpoComment);
      }
    } catch (Exception e) {
      log.error(e.toString());
    }
    return notFound("NO COMMENT FOUND");
  }

  private boolean hodNotRelatedToRequestItem(Employee employee, Optional<RequestItem> requestItem) {
    return employee.getRoles().get(0).getName().equalsIgnoreCase(EmployeeRole.ROLE_HOD.name())
        && employee.getDepartment() == requestItem.get().getUserDepartment();
  }



}
