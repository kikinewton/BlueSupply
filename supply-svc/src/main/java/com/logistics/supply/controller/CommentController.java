package com.logistics.supply.controller;

import com.logistics.supply.dto.*;
import com.logistics.supply.enums.CommentType;
import com.logistics.supply.enums.ProcurementType;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.*;
import com.logistics.supply.service.*;
import com.logistics.supply.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
      Authentication authentication) {
    try {
      EmployeeRole role = roleService.getEmployeeRole(authentication);

      switch (procurementType) {
        case LPO:
          RequestItem commentResult = requestItemCommentService.cancelRequestItem(itemId, role);
          return ResponseDTO.wrapSuccessResult(commentResult, Constants.REQUEST_CANCELLED);
        case FLOAT:
          FloatOrder floatOrders = floatCommentService.cancel(itemId, role);
          return ResponseDTO.wrapSuccessResult(floatOrders, Constants.REQUEST_CANCELLED);
        case PETTY_CASH:
          PettyCash pettyCash = pettyCashCommentService.cancelPettyCash(itemId, role);
          return ResponseDTO.wrapSuccessResult(pettyCash, Constants.REQUEST_CANCELLED);
        default:
          throw new IllegalArgumentException("UNSUPPORTED VALUE: " + procurementType);
      }

    } catch (Exception e) {
      log.error(e.toString());
    }
    return ResponseDTO.wrapErrorResult("CANCEL COMMENT FAILED");
  }

  @PostMapping("/api/comments/{commentType}/{itemId}")
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
          return ResponseDTO.wrapSuccessResult(requestItemComment, Constants.COMMENT_SAVED);
        case FLOAT_COMMENT:
          CommentResponse<FloatOrder.FloatOrderDTO> floatComment =
              floatCommentService.saveFloatComment(comments, itemId, employee);
          return ResponseDTO.wrapSuccessResult(floatComment, Constants.COMMENT_SAVED);
        case PETTY_CASH_COMMENT:
          CommentResponse<PettyCash.PettyCashMinorDTO> pettyCashComment =
              pettyCashCommentService.savePettyCashComment(comments, itemId, employee);
          return ResponseDTO.wrapSuccessResult(pettyCashComment, Constants.COMMENT_SAVED);
        case QUOTATION_COMMENT:
          CommentResponse<QuotationMinorDTO> quotationComment =
              quotationCommentService.saveComment(comments, itemId, employee);
          return ResponseDTO.wrapSuccessResult(quotationComment, Constants.COMMENT_SAVED);
        case GRN_COMMENT:
          CommentResponse<GrnMinorDTO> goodsReceivedNoteComment =
              goodsReceivedNoteCommentService.saveGRNComment(comments, itemId, employee);
          return ResponseDTO.wrapSuccessResult(goodsReceivedNoteComment, Constants.COMMENT_SAVED);
        case PAYMENT_COMMENT:
          CommentResponse<PaymentDraftMinorDTO> paymentDraftComment =
              paymentDraftCommentService.savePaymentDraftComment(comments, itemId, employee);
          return ResponseDTO.wrapSuccessResult(paymentDraftComment, Constants.COMMENT_SAVED);
        case FLOAT_GRN_COMMENT:
          CommentResponse<FloatGrnDTO> floatGRNComment =
              floatGRNCommentService.saveFloatGRNComment(comments, itemId, employee);
          return ResponseDTO.wrapSuccessResult(floatGRNComment, Constants.COMMENT_SAVED);
        default:
          throw new IllegalArgumentException("UNSUPPORTED VALUE: " + commentType);
      }

    } catch (Exception e) {
      log.error(e.toString());
    }
    return ResponseDTO.wrapErrorResult(Constants.COMMENT_NOT_SAVED);
  }

  @GetMapping(value = "/api/comments/{itemId}/unread")
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
          return ResponseDTO.wrapSuccessResult(floatOrderComments, Constants.FETCH_SUCCESSFUL);
        case PETTY_CASH_COMMENT:
          List<CommentResponse<PettyCash.PettyCashMinorDTO>> pettyCashComments =
              pettyCashCommentService.findByCommentTypeId(itemId);
          return ResponseDTO.wrapSuccessResult(pettyCashComments, Constants.FETCH_SUCCESSFUL);
        case QUOTATION_COMMENT:
          List<CommentResponse<QuotationMinorDTO>> unReadQuotationComment =
              quotationCommentService.findByCommentTypeId(itemId);
          return ResponseDTO.wrapSuccessResult(unReadQuotationComment, Constants.FETCH_SUCCESSFUL);
        case GRN_COMMENT:
          List<CommentResponse<GrnMinorDTO>> unReadGRNComment =
              goodsReceivedNoteCommentService.findByCommentTypeId(itemId);
          return ResponseDTO.wrapSuccessResult(unReadGRNComment, Constants.FETCH_SUCCESSFUL);
        case PAYMENT_COMMENT:
          List<CommentResponse<PaymentDraftMinorDTO>> unPaymentComment =
              paymentDraftCommentService.findByCommentTypeId(itemId);
          return ResponseDTO.wrapSuccessResult(unPaymentComment, Constants.FETCH_SUCCESSFUL);
        case FLOAT_GRN_COMMENT:
          List<CommentResponse<FloatGrnDTO>> floatGrnComments =
              floatGRNCommentService.findByCommentTypeId(itemId);
          return ResponseDTO.wrapSuccessResult(floatGrnComments, Constants.FETCH_SUCCESSFUL);
        default:
          throw new IllegalStateException(String.format("UNEXPECTED VALUE: %s", commentType));
      }
    } catch (Exception e) {
      log.error(e.toString());
    }
    return ResponseDTO.wrapErrorResult("NO COMMENT FOUND");
  }

  @GetMapping(value = "/res/comments/{itemId}/export")
  public ResponseEntity<Resource> check(
      @PathVariable("itemId") int itemId, @RequestParam CommentType commentType)
      throws GeneralException, IOException {
    String fileName =
        MessageFormat.format(
                "{0}_data_{1}_{2}", commentType, itemId, RandomStringUtils.randomAlphanumeric(5))
            .toLowerCase()
            .concat(".csv");
    switch (commentType) {
      case LPO_COMMENT:
        ByteArrayInputStream commentDataSheet =
            requestItemCommentService.getCommentDataSheet(itemId);
        InputStreamResource outPutResource = new InputStreamResource(commentDataSheet);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
            .contentType(MediaType.parseMediaType("application/csv"))
            .body(outPutResource);
      case QUOTATION_COMMENT:
        ByteArrayInputStream quotationCommentDataSheet =
            quotationCommentService.getCommentDataSheet(itemId);
        InputStreamResource quotationOutPutResource =
            new InputStreamResource(quotationCommentDataSheet);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
            .contentType(MediaType.parseMediaType("application/csv"))
            .body(quotationOutPutResource);
      case GRN_COMMENT:
        ByteArrayInputStream grnCommentDataSheet =
            goodsReceivedNoteCommentService.getCommentDataSheet(itemId);
        InputStreamResource grnInputStreamResource = new InputStreamResource(grnCommentDataSheet);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
            .contentType(MediaType.parseMediaType("application/csv"))
            .body(grnInputStreamResource);
      case FLOAT_COMMENT:
        ByteArrayInputStream flCommentDataSheet = floatCommentService.getCommentDataSheet(itemId);
        InputStreamResource floatInputStreamResource = new InputStreamResource(flCommentDataSheet);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
            .contentType(MediaType.parseMediaType("application/csv"))
            .body(floatInputStreamResource);
      case PAYMENT_COMMENT:
        ByteArrayInputStream payCommentDataSheet =
            paymentDraftCommentService.getCommentDataSheet(itemId);
        InputStreamResource payInputStreamResource = new InputStreamResource(payCommentDataSheet);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
            .contentType(MediaType.parseMediaType("application/csv"))
            .body(payInputStreamResource);
      case PETTY_CASH_COMMENT:
        ByteArrayInputStream pettyCashCommentDataSheet =
            pettyCashCommentService.getCommentDataSheet(itemId);
        InputStreamResource pettyCashInputStreamResource =
            new InputStreamResource(pettyCashCommentDataSheet);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
            .contentType(MediaType.parseMediaType("application/csv"))
            .body(pettyCashInputStreamResource);
      case FLOAT_GRN_COMMENT:
        ByteArrayInputStream floatGRNCommentDataSheet = floatGRNCommentService.getCommentDataSheet(itemId);
        InputStreamResource floatGRNCommentResource = new InputStreamResource(floatGRNCommentDataSheet);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(floatGRNCommentResource);

    }
    throw new GeneralException("Failed to generate comment export", HttpStatus.BAD_REQUEST);
  }
}
