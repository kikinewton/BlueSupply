package com.logistics.supply.controller;

import com.logistics.supply.dto.CommentDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.enums.ProcurementType;
import com.logistics.supply.model.*;
import com.logistics.supply.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Objects;
import java.util.Optional;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

  final RequestItemCommentService requestItemCommentService;
  final FloatCommentService floatCommentService;
  final PettyCashCommentService pettyCashCommentService;
  final RequestItemService requestItemService;
  final EmployeeService employeeService;

  @PostMapping("/comment/{procurementType}/{procurementTypeId}")
  @PreAuthorize("hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_HOD')")
  public ResponseEntity<?> addRequestComment(
      @Valid @RequestBody CommentDTO comment,
      @Valid @PathVariable("procurementTypeId") int procurementTypeId,
      @Valid @PathVariable ProcurementType procurementType,
      Authentication authentication) {
    try {
      Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
      switch (procurementType) {
        case LPO:
          RequestItemComment saved = saveRequestItemComment(comment, procurementTypeId, employee);
          if (Objects.isNull(saved)) return failedResponse("COMMENT_NOT_SAVED");
          ResponseDTO response = new ResponseDTO("COMMENT_SAVED", SUCCESS, saved);
          return ResponseEntity.ok(response);
        case FLOAT:
          FloatComment savedFloatComment = saveFloatComment(comment, procurementTypeId, employee);
          if (Objects.isNull(savedFloatComment)) return failedResponse("COMMENT_NOT_SAVED");
          ResponseDTO responseFloat = new ResponseDTO("COMMENT_SAVED", SUCCESS, savedFloatComment);
          return ResponseEntity.ok(responseFloat);
        case PETTY_CASH:
          PettyCashComment savedPettyCashComment =
              savePettyCashComment(comment, procurementTypeId, employee);
          if (Objects.isNull(savedPettyCashComment)) return failedResponse("COMMENT_NOT_SAVED");
          ResponseDTO responsePettyComment =
              new ResponseDTO("COMMENT_SAVED", SUCCESS, savedPettyCashComment);
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
    if (!requestItem.isPresent()
        || requestItem.get().getUserDepartment() != employee.getDepartment()) return null;
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

  private FloatComment saveFloatComment(CommentDTO comment, int floatId, Employee employee) {
    FloatComment floatComment = new FloatComment();
    BeanUtils.copyProperties(comment, floatComment);
    floatComment.setEmployee(employee);
    floatComment.setFloatId(floatId);
    FloatComment saved = floatCommentService.addComment(floatComment);
    return saved;
  }

  private PettyCashComment savePettyCashComment(
      CommentDTO comment, int pettyCashId, Employee employee) {
    PettyCashComment pettyCashComment = new PettyCashComment();
    BeanUtils.copyProperties(comment, pettyCashComment);
    pettyCashComment.setEmployee(employee);
    pettyCashComment.setPettyCashId(pettyCashId);
    PettyCashComment saved = pettyCashCommentService.addComment(pettyCashComment);
    return saved;
  }

  private ResponseEntity<ResponseDTO> failedResponse(String message) {
    ResponseDTO failed = new ResponseDTO(message, ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
