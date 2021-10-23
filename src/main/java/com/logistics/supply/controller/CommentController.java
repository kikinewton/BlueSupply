package com.logistics.supply.controller;

import com.logistics.supply.dto.CommentDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.FloatComment;
import com.logistics.supply.model.PettyCashComment;
import com.logistics.supply.model.RequestItemComment;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.FloatCommentService;
import com.logistics.supply.service.PettyCashCommentService;
import com.logistics.supply.service.RequestItemCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Objects;

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
  final EmployeeService employeeService;

  @PostMapping("/comment/{procurementType}/{procurementTypeId}")
  public ResponseEntity<?> addRequestComment(
          @Valid CommentDTO comment, @PathVariable("requestItemId") int requestItemId, Authentication authentication) {
    try {
      Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
      RequestItemComment saved = saveRequestItemComment(comment, requestItemId, employee);
      if (Objects.isNull(saved)) return failedResponse("COMMENT_NOT_SAVED");
      ResponseDTO response = new ResponseDTO("COMMENT_SAVED", SUCCESS, saved);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("ADD_COMMENT_FAILED");
  }

  private RequestItemComment saveRequestItemComment(CommentDTO comment, int requestItemId, Employee employee) {
    RequestItemComment requestItemComment = new RequestItemComment();
    BeanUtils.copyProperties(comment, requestItemComment);
    requestItemComment.setRequestItemId(requestItemId);
    requestItemComment.setEmployee(employee);
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

  private PettyCashComment savePettyCashComment(CommentDTO comment, int pettyCashId, Employee employee) {
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
