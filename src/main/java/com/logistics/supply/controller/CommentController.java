package com.logistics.supply.controller;

import com.logistics.supply.dto.BulkCommentDTO;
import com.logistics.supply.dto.CommentDTO;
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

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

  final RequestItemCommentService requestItemCommentService;
  final FloatCommentService floatCommentService;
  final FloatService floatService;
  final PettyCashCommentService pettyCashCommentService;
  final PettyCashService pettyCashService;
  final RequestItemService requestItemService;
  final EmployeeService employeeService;

  @PostMapping("/comment/{procurementType}")
  @PreAuthorize("hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_HOD')")
  public ResponseEntity<?> addRequestComment(
      @Valid @RequestBody BulkCommentDTO comments,
      @Valid @PathVariable ProcurementType procurementType,
      Authentication authentication) {
    try {
      Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
      switch (procurementType) {
        case LPO:
          List<RequestItemComment> commentResult =
              comments.getComments().stream()
                  .map(
                      c ->
                          saveRequestItemComment(
                              c.getComment(), c.getProcurementTypeId(), employee))
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());

          if (commentResult.isEmpty()) return failedResponse("COMMENT_NOT_SAVED");
          ResponseDTO response = new ResponseDTO("COMMENTS_SAVED", SUCCESS, commentResult);
          return ResponseEntity.ok(response);
        case FLOAT:
          List<FloatComment> floatComments =
              comments.getComments().stream()
                  .map(c -> saveFloatComment(c.getComment(), c.getProcurementTypeId(), employee))
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());
          if (floatComments.isEmpty()) return failedResponse("COMMENT_NOT_SAVED");
          ResponseDTO responseFloat = new ResponseDTO("COMMENTS_SAVED", SUCCESS, floatComments);
          return ResponseEntity.ok(responseFloat);
        case PETTY_CASH:
          List<PettyCashComment> pettyCashComments =
              comments.getComments().stream()
                  .map(
                      c -> savePettyCashComment(c.getComment(), c.getProcurementTypeId(), employee))
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());

          if (pettyCashComments.isEmpty()) return failedResponse("COMMENTS_NOT_SAVED");
          ResponseDTO responsePettyComment =
              new ResponseDTO("COMMENT_SAVED", SUCCESS, pettyCashComments);
          return ResponseEntity.ok(responsePettyComment);
      }

    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("ADD_COMMENT_FAILED");
  }

  private RequestItemComment saveRequestItemComment(
      CommentDTO comment, int requestItemId, Employee employee) {
    Optional<RequestItem> requestItem = requestItemService.findById(requestItemId);
    if (!requestItem.isPresent()) return null;
    if (hodNotRelatedToRequestItem(employee, requestItem)) return null;
    RequestItemComment requestItemComment =
        RequestItemComment.builder()
            .requestItem(requestItem.get())
            .processWithComment(comment.getProcess())
            .description(comment.getDescription())
            .employee(employee)
            .build();

    RequestItemComment saved = requestItemCommentService.addComment(requestItemComment);
    return saved;
  }

  private boolean hodNotRelatedToRequestItem(Employee employee, Optional<RequestItem> requestItem) {
    return employee.getRoles().get(0).getName().equalsIgnoreCase(EmployeeRole.ROLE_HOD.name())
        && employee.getDepartment() != requestItem.get().getUserDepartment();
  }

  private boolean hodNotRelatedToFloats(Employee employee, Floats floats) {
    return employee.getRoles().get(0).getName().equalsIgnoreCase(EmployeeRole.ROLE_HOD.name())
        && employee.getDepartment() != floats.getDepartment();
  }

  private boolean hodNotRelatedToPettyCash(Employee employee, PettyCash pettyCash) {
    return employee.getRoles().get(0).getName().equalsIgnoreCase(EmployeeRole.ROLE_HOD.name())
        && employee.getDepartment() != pettyCash.getDepartment();
  }

  private FloatComment saveFloatComment(CommentDTO comment, int floatId, Employee employee) {
    Floats floats = floatService.findById(floatId);
    if (Objects.isNull(floats)) return null;
    if (hodNotRelatedToFloats(employee, floats)) return null;

    FloatComment floatComment =
        FloatComment.builder()
            .floats(floats)
            .processWithComment(comment.getProcess())
            .description(comment.getDescription())
            .employee(employee)
            .build();

    FloatComment saved = floatCommentService.addComment(floatComment);
    return saved;
  }

  private PettyCashComment savePettyCashComment(
      CommentDTO comment, int pettyCashId, Employee employee) {
    PettyCash pettyCash = pettyCashService.findById(pettyCashId);
    if (Objects.isNull(pettyCash)) return null;
    if (hodNotRelatedToPettyCash(employee, pettyCash)) return null;
    PettyCashComment pettyCashComment =
        PettyCashComment.builder()
            .processWithComment(comment.getProcess())
            .description(comment.getDescription())
            .pettyCash(pettyCash)
            .employee(employee)
            .build();

    PettyCashComment saved = pettyCashCommentService.addComment(pettyCashComment);
    return saved;
  }

  private ResponseEntity<ResponseDTO> failedResponse(String message) {
    ResponseDTO failed = new ResponseDTO(message, ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
