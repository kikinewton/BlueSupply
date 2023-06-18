package com.logistics.supply.service;

import com.logistics.supply.dto.*;
import com.logistics.supply.model.Employee;
import com.logistics.supply.util.Constants;
import com.logistics.supply.util.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api")
public class PaymentCommentController {
  private final PaymentDraftCommentService paymentDraftCommentService;
  private final GoodsReceivedNoteCommentService goodsReceivedNoteCommentService;
  private final EmployeeService employeeService;

  @PostMapping("/comment/goodsReceivedNote/{goodsReceivedNoteId}")
  @PreAuthorize(
      "hasRole('ROLE_HOD') or hasRole('ROLE_PROCUREMENT_MANAGER') or hasRole('ROLE_ACCOUNT_OFFICER')")
  public ResponseEntity<?> postGRNComment(
      @PathVariable("goodsReceivedNoteId") int goodsReceivedNoteId,
      @RequestBody BulkCommentDTO comments,
      Authentication authentication) {
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());

    List<CommentResponse<GrnMinorDto>> savedComments =
        comments.getComments().stream()
            .map(
                c ->
                    goodsReceivedNoteCommentService.saveGRNComment(
                        c.getComment(), goodsReceivedNoteId, employee))
            .collect(Collectors.toList());
    if (savedComments.isEmpty()) return Helper.failedResponse("ADD COMMENT FAILED");
    ResponseDto responseGRNComment = new ResponseDto("COMMENT SAVED", Constants.SUCCESS, savedComments);
    return ResponseEntity.ok(responseGRNComment);
  }

  @PostMapping("/comment/paymentDraft/{paymentDraftId}")
  @PreAuthorize("hasRole('ROLE_AUDITOR')")
  public ResponseEntity<?> postAuditorComment(
      Authentication authentication,
      @PathVariable("paymentDraftId") int paymentDraftId,
      @RequestBody CommentDTO comment) {
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    CommentResponse<PaymentDraftMinorDto> draftComment =
        paymentDraftCommentService.savePaymentDraftComment(comment, paymentDraftId, employee);
    return ResponseDto.wrapSuccessResult(draftComment, "SAVED SUCCESSFULLY");
  }
}
