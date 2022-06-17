package com.logistics.supply.service;

import com.logistics.supply.dto.BulkCommentDTO;
import com.logistics.supply.dto.CommentDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.GoodsReceivedNoteComment;
import com.logistics.supply.model.PaymentDraftComment;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.SUCCESS;
import static com.logistics.supply.util.Helper.failedResponse;

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
        List<GoodsReceivedNoteComment> savedComments =
                comments.getComments().stream()
                        .map(
                                c ->
                                        goodsReceivedNoteCommentService.saveGRNComment(
                                                c.getComment(), goodsReceivedNoteId, employee))
                        .collect(Collectors.toList());
        if (savedComments.isEmpty()) return failedResponse("ADD COMMENT FAILED");
        ResponseDTO responseGRNComment = new ResponseDTO("COMMENT SAVED", SUCCESS, savedComments);
        return ResponseEntity.ok(responseGRNComment);
    }

    @PostMapping("/comment/paymentDraft/{paymentDraftId}")
    @PreAuthorize("hasRole('ROLE_AUDITOR')")
    public ResponseEntity<?> postAuditorComment(
            Authentication authentication,
            @PathVariable("paymentDraftId") int paymentDraftId,
            @RequestBody CommentDTO comment) {
        Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
        PaymentDraftComment draftComment = paymentDraftCommentService.savePaymentDraftComment(comment, paymentDraftId, employee);
        ResponseDTO response = new ResponseDTO("COMMENT SAVED", SUCCESS, draftComment);
        return ResponseEntity.ok(response);
    }

}
