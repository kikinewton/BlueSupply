package com.logistics.supply.controller;

import com.logistics.supply.dto.BulkCommentDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.enums.ProcurementType;
import com.logistics.supply.model.*;
import com.logistics.supply.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
  private final FloatOrderService floatOrderService;
  private final PettyCashCommentService pettyCashCommentService;
  private final PettyCashService pettyCashService;
  private final RequestItemService requestItemService;
  private final EmployeeService employeeService;
  private final RoleService roleService;

  @PostMapping("/comment/{procurementType}")
  @PreAuthorize("hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_HOD')")
  public ResponseEntity<?> addRequestComment(
      @Valid @RequestBody BulkCommentDTO comments,
      @Valid @PathVariable ProcurementType procurementType,
      Authentication authentication) {
    try {
      Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
      EmployeeRole role = roleService.getEmployeeRole(authentication);

      switch (procurementType) {
        case LPO:
          List<RequestItemComment> commentResult =
              comments.getComments().stream()
                  .map(
                      c -> {
                        if (c.getCancelled() != null && c.getCancelled() == true) {
                          requestItemService.cancelRequestItem(c.getProcurementTypeId(), role);
                        }
                        return requestItemCommentService.saveRequestItemComment(
                            c.getComment(), c.getProcurementTypeId(), employee);
                      })
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());

          if (commentResult.isEmpty()) return failedResponse("COMMENT NOT SAVED");
          ResponseDTO response = new ResponseDTO("COMMENTS SAVED", SUCCESS, commentResult);
          return ResponseEntity.ok(response);
        case FLOAT:
          List<FloatComment> floatComments =
              comments.getComments().stream()
                  .map(
                      c -> {
                        if (c.getCancelled() != null && c.getCancelled()) {
                          floatOrderService.cancel(c.getProcurementTypeId(), role);
                        }
                        return floatCommentService.saveFloatComment(c.getComment(), c.getProcurementTypeId(), employee);
                      })
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());
          if (floatComments.isEmpty()) return failedResponse("COMMENT NOT SAVED");
          ResponseDTO responseFloat = new ResponseDTO("COMMENTS SAVED", SUCCESS, floatComments);
          return ResponseEntity.ok(responseFloat);
        case PETTY_CASH:
          List<PettyCashComment> pettyCashComments =
              comments.getComments().stream()
                  .map(
                      c -> {
                        if (c.getCancelled() != null && c.getCancelled()) {
                          pettyCashService.cancelPettyCash(c.getProcurementTypeId(), role);
                        }
                        return pettyCashCommentService.savePettyCashComment(
                            c.getComment(), c.getProcurementTypeId(), employee);
                      })
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());

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
          List<RequestItemComment> comments =
              requestItemCommentService.findUnReadComment(employee.getId());
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
