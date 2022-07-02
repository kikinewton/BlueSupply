package com.logistics.supply.controller;

import com.logistics.supply.dto.*;
import com.logistics.supply.enums.CommentType;
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

import static com.logistics.supply.util.Constants.*;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {
  private final RequestItemCommentService requestItemCommentService;
  private final FloatCommentService floatCommentService;
  private final PettyCashCommentService pettyCashCommentService;
  private final PaymentDraftCommentService paymentDraftCommentService;
  private final EmployeeService employeeService;
  private final QuotationCommentService quotationCommentService;
  private final GoodsReceivedNoteCommentService goodsReceivedNoteCommentService;
  private final RoleService roleService;

  @PutMapping("/comments/{procurementType}/cancel")
  @PreAuthorize("hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_HOD')")
  public ResponseEntity<?> cancelRequestsWithComment(
      @RequestParam("itemId") int itemId,
      @Valid @PathVariable ProcurementType procurementType,
      Authentication authentication) {
    try {
      EmployeeRole role = roleService.getEmployeeRole(authentication);

      switch (procurementType) {
        case LPO:
          RequestItem commentResult =
              requestItemCommentService.cancelRequestItem(itemId, role);
          return ResponseDTO.wrapSuccessResult(commentResult, REQUEST_CANCELLED);
        case FLOAT:
          FloatOrder floatOrders =
              floatCommentService.cancel(itemId,  role);
          return ResponseDTO.wrapSuccessResult(floatOrders, REQUEST_CANCELLED);
        case PETTY_CASH:
          PettyCash pettyCash =
              pettyCashCommentService.cancelPettyCash(itemId, role);
          return ResponseDTO.wrapSuccessResult(pettyCash, REQUEST_CANCELLED);
        default:
          throw new IllegalArgumentException("UNSUPPORTED VALUE: " + procurementType);
      }

    } catch (Exception e) {
      log.error(e.toString());
    }
    return ResponseDTO.wrapErrorResult("CANCEL COMMENT FAILED");
  }

  @GetMapping(value = "/comments/unread")
  public ResponseEntity<?> findUnReadRequestComment(
      @RequestParam CommentType commentType, Authentication authentication) {
    try {
      Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
      switch (commentType) {
        case LPO_COMMENT:
          List<CommentResponse<RequestItemDTO>> comments =
              requestItemCommentService.findUnReadComment(employee.getId());
          return ResponseDTO.wrapSuccessResult(comments, "FETCH UNREAD LPO COMMENT");
        case FLOAT_COMMENT:
        case PETTY_CASH_COMMENT:
          break;
        case QUOTATION_COMMENT:
          List<CommentResponse<QuotationMinorDTO>> unReadQuotationComment =
              quotationCommentService.findUnReadComment(employee.getId());
          return ResponseDTO.wrapSuccessResult(
              unReadQuotationComment, "FETCH UNREAD QUOTATION COMMENT");
        case GRN_COMMENT:
          List<CommentResponse<GrnMinorDTO>> unReadGRNComment =
              goodsReceivedNoteCommentService.findUnReadComment(employee.getId());
          return ResponseDTO.wrapSuccessResult(unReadGRNComment, FETCH_SUCCESSFUL);
        default:
          throw new IllegalStateException(String.format("UNEXPECTED VALUE: %s", commentType));
      }
    } catch (Exception e) {
      log.error(e.toString());
    }
    return ResponseDTO.wrapErrorResult("NO COMMENT FOUND");
  }

  @PostMapping("/comments/{commentType}/{itemId}")
  public ResponseEntity<?> addComment(
      @Valid @RequestBody CommentDTO comments,
      @Valid @PathVariable("commentType") CommentType commentType,
      @PathVariable("itemId") int itemId,
      Authentication authentication) {
    try {
      Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
      switch (commentType) {
        case LPO_COMMENT:
          CommentResponse<RequestItemDTO> requestItemComment =
              requestItemCommentService.saveRequestItemComment(comments, itemId, employee);
          return ResponseDTO.wrapSuccessResult(requestItemComment, COMMENT_SAVED);
        case FLOAT_COMMENT:
          CommentResponse<FloatOrder.FloatOrderDTO> floatComment =
              floatCommentService.saveFloatComment(comments, itemId, employee);
          return ResponseDTO.wrapSuccessResult(floatComment, COMMENT_SAVED);
        case PETTY_CASH_COMMENT:
          PettyCashComment pettyCashComment =
              pettyCashCommentService.savePettyCashComment(comments, itemId, employee);
          return ResponseDTO.wrapSuccessResult(pettyCashComment, COMMENT_SAVED);
        case QUOTATION_COMMENT:
          CommentResponse<QuotationMinorDTO> quotationComment =
              quotationCommentService.saveComment(comments, itemId, employee);
          return ResponseDTO.wrapSuccessResult(quotationComment, COMMENT_SAVED);
        case GRN_COMMENT:
          CommentResponse<GrnMinorDTO> goodsReceivedNoteComment =
              goodsReceivedNoteCommentService.saveGRNComment(comments, itemId, employee);
          return ResponseDTO.wrapSuccessResult(goodsReceivedNoteComment, COMMENT_SAVED);
        case PAYMENT_COMMENT:
          CommentResponse<PaymentDraftMinorDTO> paymentDraftComment =
              paymentDraftCommentService.savePaymentDraftComment(comments, itemId, employee);
          return ResponseDTO.wrapSuccessResult(paymentDraftComment, COMMENT_SAVED);
        default:
          throw new IllegalArgumentException("UNSUPPORTED VALUE: " + commentType);
      }

    } catch (Exception e) {
      log.error(e.toString());
    }
    return ResponseDTO.wrapErrorResult(COMMENT_NOT_SAVED);
  }

  @GetMapping(value = "/comments/{itemId}/unread")
  public ResponseEntity<?> findUnReadRequestComment(
      @PathVariable("itemId") int itemId, @RequestParam CommentType commentType) {
    try {
      switch (commentType) {
        case LPO_COMMENT:
          List<CommentResponse<RequestItemDTO>> comments =
              requestItemCommentService.findByCommentTypeId(itemId);
          return ResponseDTO.wrapSuccessResult(comments, "FETCH UNREAD LPO COMMENT");
        case FLOAT_COMMENT:
          List<CommentResponse<FloatOrder.FloatOrderDTO>> floatOrderComments =
              floatCommentService.findByCommentTypeId(itemId);
          return ResponseDTO.wrapSuccessResult(floatOrderComments, FETCH_SUCCESSFUL);
        case PETTY_CASH_COMMENT:
          break;
        case QUOTATION_COMMENT:
          List<CommentResponse<QuotationMinorDTO>> unReadQuotationComment =
              quotationCommentService.findByCommentTypeId(itemId);
          return ResponseDTO.wrapSuccessResult(
              unReadQuotationComment, "FETCH UNREAD QUOTATION COMMENT");
        case GRN_COMMENT:
          List<CommentResponse<GrnMinorDTO>> unReadGRNComment =
              goodsReceivedNoteCommentService.findByCommentTypeId(itemId);
          return ResponseDTO.wrapSuccessResult(unReadGRNComment, FETCH_SUCCESSFUL);
        case PAYMENT_COMMENT:
          List<CommentResponse<PaymentDraftMinorDTO>> unPaymentComment = paymentDraftCommentService.findByCommentTypeId(itemId);
          return ResponseDTO.wrapSuccessResult(unPaymentComment, FETCH_SUCCESSFUL);
        default:
          throw new IllegalStateException(String.format("UNEXPECTED VALUE: %s", commentType));
      }
    } catch (Exception e) {
      log.error(e.toString());
    }
    return ResponseDTO.wrapErrorResult("NO COMMENT FOUND");
  }
}
