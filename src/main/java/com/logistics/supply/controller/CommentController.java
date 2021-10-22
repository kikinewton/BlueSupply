package com.logistics.supply.controller;

import com.logistics.supply.dto.CommentDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestItemComment;
import com.logistics.supply.service.EmployeeService;
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
  final EmployeeService employeeService;

  @PostMapping("/comment/{requestItemId}")
  public ResponseEntity<?> addRequestComment(
          @Valid CommentDTO comment, @PathVariable("requestItemId") int requestItemId, Authentication authentication) {
    try {
      RequestItemComment requestItemComment = new RequestItemComment();
      BeanUtils.copyProperties(comment, requestItemComment);
      requestItemComment.setRequestItemId(requestItemId);
      Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
      requestItemComment.setEmployee(employee);
      RequestItemComment saved = requestItemCommentService.addComment(requestItemComment);
      if (Objects.isNull(saved)) return failedResponse("COMMENT_NOT_SAVED");
      ResponseDTO response = new ResponseDTO("COMMENT_SAVED", SUCCESS, saved);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("ADD_COMMENT_FAILED");
  }

  private ResponseEntity<ResponseDTO> failedResponse(String message) {
    ResponseDTO failed = new ResponseDTO(message, ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
