package com.logistics.supply.controller;

import com.logistics.supply.dto.*;
import com.logistics.supply.enums.CommentType;
import com.logistics.supply.enums.ProcurementType;
import com.logistics.supply.model.*;
import com.logistics.supply.service.*;
import com.logistics.supply.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CommentController {

  private final RequestItemCommentService requestItemCommentService;
  private final FloatCommentService floatCommentService;
  private final PettyCashCommentService pettyCashCommentService;
  private final PaymentDraftCommentService paymentDraftCommentService;
  private final EmployeeService employeeService;
  private final QuotationCommentService quotationCommentService;
  private final GoodsReceivedNoteCommentService goodsReceivedNoteCommentService;
  private final FloatGRNCommentService floatGRNCommentService;
  private final RoleService roleService;

  @PutMapping("/api/comments/{procurementType}/cancel")
  @PreAuthorize("hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_HOD')")
  public ResponseEntity<?> cancelRequestsWithComment(
      @RequestParam("itemId") int itemId,
      @Valid @PathVariable ProcurementType procurementType,
      Authentication authentication)  {

      EmployeeRole role = roleService.getEmployeeRole(authentication);

      switch (procurementType) {
        case LPO -> {
          RequestItem commentResult = requestItemCommentService.cancelRequestItem(itemId, role);
          return ResponseDto.wrapSuccessResult(commentResult, Constants.REQUEST_CANCELLED);
        }
        case FLOAT -> {
          FloatOrder floatOrders = floatCommentService.cancel(itemId, role);
          return ResponseDto.wrapSuccessResult(floatOrders, Constants.REQUEST_CANCELLED);
        }
        case PETTY_CASH -> {
          PettyCash pettyCash = pettyCashCommentService.cancelPettyCash(itemId, role);
          return ResponseDto.wrapSuccessResult(pettyCash, Constants.REQUEST_CANCELLED);
        }
        default -> throw new IllegalArgumentException("UNSUPPORTED VALUE: " + procurementType);
      }
  }

  @PostMapping("/api/comments/{commentType}/{itemId}")
  public ResponseEntity<?> addComment(
      @Valid @RequestBody CommentDto comments,
      @Valid @PathVariable("commentType") CommentType commentType,
      @PathVariable("itemId") int itemId,
      Authentication authentication) {
    try {
      Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
      switch (commentType) {
        case LPO_COMMENT -> {
          CommentResponse<RequestItemDto> requestItemComment =
                  requestItemCommentService.saveRequestItemComment(comments, itemId, employee);
          return ResponseDto.wrapSuccessResult(requestItemComment, Constants.COMMENT_SAVED);
        }
        case FLOAT_COMMENT -> {
          CommentResponse<FloatOrder.FloatOrderDto> floatComment =
                  floatCommentService.saveFloatComment(comments, itemId, employee);
          return ResponseDto.wrapSuccessResult(floatComment, Constants.COMMENT_SAVED);
        }
        case PETTY_CASH_COMMENT -> {
          CommentResponse<PettyCash.PettyCashMinorDto> pettyCashComment =
                  pettyCashCommentService.savePettyCashComment(comments, itemId, employee);
          return ResponseDto.wrapSuccessResult(pettyCashComment, Constants.COMMENT_SAVED);
        }
        case QUOTATION_COMMENT -> {
          CommentResponse<QuotationMinorDto> quotationComment =
                  quotationCommentService.saveComment(comments, itemId, employee);
          return ResponseDto.wrapSuccessResult(quotationComment, Constants.COMMENT_SAVED);
        }
        case GRN_COMMENT -> {
          CommentResponse<GrnMinorDto> goodsReceivedNoteComment =
                  goodsReceivedNoteCommentService.saveGRNComment(comments, itemId, employee);
          return ResponseDto.wrapSuccessResult(goodsReceivedNoteComment, Constants.COMMENT_SAVED);
        }
        case PAYMENT_COMMENT -> {
          CommentResponse<PaymentDraftMinorDto> paymentDraftComment =
                  paymentDraftCommentService.savePaymentDraftComment(comments, itemId, employee);
          return ResponseDto.wrapSuccessResult(paymentDraftComment, Constants.COMMENT_SAVED);
        }
        case FLOAT_GRN_COMMENT -> {
          CommentResponse<FloatGrnDto> floatGRNComment =
                  floatGRNCommentService.saveFloatGRNComment(comments, itemId, employee);
          return ResponseDto.wrapSuccessResult(floatGRNComment, Constants.COMMENT_SAVED);
        }
        default -> throw new IllegalArgumentException("UNSUPPORTED VALUE: " + commentType);
      }

    } catch (Exception e) {
      log.error(e.toString());
    }
    return ResponseDto.wrapErrorResult(Constants.COMMENT_NOT_SAVED);
  }

  @GetMapping(value = "/api/comments/{itemId}/unread")
  public ResponseEntity<?> findUnReadRequestComment(
      @PathVariable("itemId") int itemId, @RequestParam CommentType commentType) {
    try {
      switch (commentType) {
        case LPO_COMMENT -> {
          List<CommentResponse<RequestItemDto>> comments =
                  requestItemCommentService.findByCommentTypeId(itemId);
          return ResponseDto.wrapSuccessResult(comments, "FETCH UNREAD LPO COMMENT");
        }
        case FLOAT_COMMENT -> {
          List<CommentResponse<FloatOrder.FloatOrderDto>> floatOrderComments =
                  floatCommentService.findByCommentTypeId(itemId);
          return ResponseDto.wrapSuccessResult(floatOrderComments, Constants.FETCH_SUCCESSFUL);
        }
        case PETTY_CASH_COMMENT -> {
          List<CommentResponse<PettyCash.PettyCashMinorDto>> pettyCashComments =
                  pettyCashCommentService.findByCommentTypeId(itemId);
          return ResponseDto.wrapSuccessResult(pettyCashComments, Constants.FETCH_SUCCESSFUL);
        }
        case QUOTATION_COMMENT -> {
          List<CommentResponse<QuotationMinorDto>> unReadQuotationComment =
                  quotationCommentService.findByCommentTypeId(itemId);
          return ResponseDto.wrapSuccessResult(unReadQuotationComment, Constants.FETCH_SUCCESSFUL);
        }
        case GRN_COMMENT -> {
          List<CommentResponse<GrnMinorDto>> unReadGRNComment =
                  goodsReceivedNoteCommentService.findByCommentTypeId(itemId);
          return ResponseDto.wrapSuccessResult(unReadGRNComment, Constants.FETCH_SUCCESSFUL);
        }
        case PAYMENT_COMMENT -> {
          List<CommentResponse<PaymentDraftMinorDto>> unPaymentComment =
                  paymentDraftCommentService.findByCommentTypeId(itemId);
          return ResponseDto.wrapSuccessResult(unPaymentComment, Constants.FETCH_SUCCESSFUL);
        }
        case FLOAT_GRN_COMMENT -> {
          List<CommentResponse<FloatGrnDto>> floatGrnComments =
                  floatGRNCommentService.findByCommentTypeId(itemId);
          return ResponseDto.wrapSuccessResult(floatGrnComments, Constants.FETCH_SUCCESSFUL);
        }
        default -> throw new IllegalStateException(String.format("UNEXPECTED VALUE: %s", commentType));
      }
    } catch (Exception e) {
      log.error(e.toString());
    }
    return ResponseDto.wrapErrorResult("NO COMMENT FOUND");
  }

  @GetMapping(value = "/res/comments/{itemId}/export")
  public ResponseEntity<Resource> exportCommentAsCsv(
      @PathVariable("itemId") int itemId, @RequestParam CommentType commentType) throws IOException {
    String fileName =
        MessageFormat.format(
                "{0}_data_{1}_{2}", commentType, itemId, RandomStringUtils.randomAlphanumeric(5))
            .toLowerCase()
            .concat(".csv");
    switch (commentType) {
      case LPO_COMMENT -> {
        ByteArrayInputStream commentDataSheet =
                requestItemCommentService.getCommentDataSheet(itemId);
        return getResourceResponseEntity(fileName, commentDataSheet);
      }
      case QUOTATION_COMMENT -> {
        ByteArrayInputStream quotationCommentDataSheet =
                quotationCommentService.getCommentDataSheet(itemId);
        return getResourceResponseEntity(fileName, quotationCommentDataSheet);
      }
      case GRN_COMMENT -> {
        ByteArrayInputStream grnCommentDataSheet =
                goodsReceivedNoteCommentService.getCommentDataSheet(itemId);
        return getResourceResponseEntity(fileName, grnCommentDataSheet);
      }
      case FLOAT_COMMENT -> {
        ByteArrayInputStream flCommentDataSheet = floatCommentService.getCommentDataSheet(itemId);
        return getResourceResponseEntity(fileName, flCommentDataSheet);
      }
      case PAYMENT_COMMENT -> {
        ByteArrayInputStream payCommentDataSheet =
                paymentDraftCommentService.getCommentDataSheet(itemId);
        return getResourceResponseEntity(fileName, payCommentDataSheet);
      }
      case PETTY_CASH_COMMENT -> {
        ByteArrayInputStream pettyCashCommentDataSheet =
                pettyCashCommentService.getCommentDataSheet(itemId);
        return getResourceResponseEntity(fileName, pettyCashCommentDataSheet);
      }
      case FLOAT_GRN_COMMENT -> {
        ByteArrayInputStream floatGRNCommentDataSheet = floatGRNCommentService.getCommentDataSheet(itemId);
        return getResourceResponseEntity(fileName, floatGRNCommentDataSheet);
      }
      default -> throw new IllegalStateException("Unexpected value: " + commentType);
    }
  }

  private static ResponseEntity<Resource> getResourceResponseEntity(String fileName, ByteArrayInputStream byteArrayInputStream) {
    InputStreamResource inputStreamResource = new InputStreamResource(byteArrayInputStream);
    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
            .contentType(MediaType.parseMediaType("application/csv"))
            .body(inputStreamResource);
  }
}
