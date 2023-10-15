package com.logistics.supply.event.listener;

import com.logistics.supply.enums.EmailType;
import com.logistics.supply.enums.RequestProcess;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.Quotation;
import com.logistics.supply.model.QuotationComment;
import com.logistics.supply.service.CacheService;
import com.logistics.supply.util.EmailSenderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.persistence.PostPersist;
import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class QuotationCommentListener {

    private final EmailSenderUtil emailSenderUtil;

    private final CacheService cacheService;

    public QuotationCommentListener(EmailSenderUtil emailSenderUtil,
                                    CacheService cacheService) {
        this.emailSenderUtil = emailSenderUtil;
        this.cacheService = cacheService;
    }

    @Value("${config.templateMail}")
    String newCommentEmail;

    @PostPersist
    public void sendEmailOnComment(QuotationComment comment) {

        log.info("Send email to notify comment on quotation");
        String title = "QUOTATION COMMENT";
        Quotation quotation = comment.getQuotation();
        String message =
                MessageFormat.format(
                        "{0} has commented on quotation with reference: {1}",
                        comment.getEmployee().getFullName(), quotation.getQuotationRef());

        String auditor = cacheService.getValueFromCache("managerByRoleName", EmployeeRole.ROLE_HOD.name());
        log.info("Auditor {} to receive message: {}", auditor, message);
        String to = RequestProcess.PROCUREMENT_RESPONSE_TO_QUOTATION_REVIEW == comment.getProcessWithComment()
                ? auditor
                : quotation.getCreatedBy().getEmail();

        CompletableFuture.runAsync(() -> emailSenderUtil.sendComposeAndSendEmail(
                title,
                message,
                newCommentEmail,
                EmailType.QUOTATION_COMMENT_EMAIL,
                to));
    }

}
