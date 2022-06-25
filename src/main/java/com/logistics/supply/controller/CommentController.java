package com.logistics.supply.controller;

import com.logistics.supply.dto.*;
import com.logistics.supply.enums.CommentType;
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

import static com.logistics.supply.util.Constants.COMMENT_NOT_SAVED;
import static com.logistics.supply.util.Constants.COMMENT_SAVED;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {
  private final RequestItemCommentService requestItemCommentService;
  private final FloatCommentService floatCommentService;
  private final PettyCashCommentService pettyCashCommentService;
  private final EmployeeService employeeService;
  private final QuotationCommentService quotationCommentService;
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
          List<RequestItemComment> commentResult =
              requestItemCommentService.saveBulkRequestItemComments(comments, employee, role);
          if (commentResult.isEmpty()) return ResponseDTO.wrapErrorResult("COMMENT NOT SAVED");
          return ResponseDTO.wrapSuccessResult(commentResult, "COMMENTS SAVED");
        case FLOAT:
          List<FloatComment> floatComments =
              floatCommentService.saveFloatComments(comments, employee, role);
          if (floatComments.isEmpty()) return ResponseDTO.wrapErrorResult("COMMENT NOT SAVED");
          return ResponseDTO.wrapSuccessResult(floatComments, "COMMENTS SAVED");
        case PETTY_CASH:
          List<PettyCashComment> pettyCashComments =
              pettyCashCommentService.savePettyCashComments(comments, employee, role);
          if (pettyCashComments.isEmpty()) return ResponseDTO.wrapErrorResult("COMMENTS NOT SAVED");
          return ResponseDTO.wrapSuccessResult(pettyCashComments, "COMMENT SAVED");
        default:
          throw new IllegalArgumentException("Unsupported value: " + procurementType);
      }

    } catch (Exception e) {
      log.error(e.toString());
    }
    return ResponseDTO.wrapErrorResult("ADD COMMENT FAILED");
  }

//  @GetMapping(value = "/comment/unread")
//  public ResponseEntity<?> findUnReadRequestComment(
//      @RequestParam ProcurementType procurementType, Authentication authentication) {
//    try {
//      switch (procurementType) {
//        case LPO:
//          Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
//          List<CommentResponse<RequestItemDTO>> comments =
//              requestItemCommentService.findCommentsNotRead(employee.getId());
//          return ResponseDTO.wrapSuccessResult(comments, "FETCH UNREAD LPO COMMENT");
//        default:
//          throw new IllegalStateException("Unexpected value: " + procurementType);
//      }
//    } catch (Exception e) {
//      log.error(e.toString());
//    }
//    return ResponseDTO.wrapErrorResult("NO COMMENT FOUND");
//  }

    @GetMapping(value = "/comment/unread")
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
            break;
          case PETTY_CASH_COMMENT:
            break;
          case QUOTATION_COMMENT:
            List<CommentResponse<QuotationMinorDTO>> unReadQuotationComment =
                quotationCommentService.findUnReadComment(employee.getId());
            return ResponseDTO.wrapSuccessResult(unReadQuotationComment, "FETCH UNREAD QUOTATION COMMENT");
          default:
            throw new IllegalStateException(String.format("Unexpected value: %s", commentType));
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
          Authentication authentication)  {
    try {
      Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
      switch (commentType) {
        case QUOTATION_COMMENT:
          QuotationComment quotationComment = quotationCommentService.saveComment(comments, itemId, employee);
          return ResponseDTO.wrapSuccessResult(quotationComment, COMMENT_SAVED);
        case STORES_COMMENT:
          break;
        case GRN_COMMENT:
          break;
        case PAYMENT_COMMENT:
          break;
        default:
          throw new IllegalArgumentException("Unsupported value: " + commentType);
      }

    } catch (Exception e) {
      log.error(e.toString());
    }
    return ResponseDTO.wrapErrorResult(COMMENT_NOT_SAVED);
  }
}
