package com.logistics.supply.event.listener;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.persistence.PostPersist;
import java.text.MessageFormat;

@Slf4j
@Component
public class PaymentDraftCommentListener {

    private final SpringTemplateEngine templateEngine;
    private final EmailSender emailSender;

    @Value("${config.templateMail}")
    String newCommentEmail;

    public PaymentDraftCommentListener(
            SpringTemplateEngine templateEngine,
            EmailSender emailSender) {
        this.templateEngine = templateEngine;
        this.emailSender = emailSender;
    }

    @PostPersist
    public void sendPaymentDraftComment(PaymentDraftComment comment) {
        log.info("======= EMAIL 0N PAYMENT DRAFT COMMENT ==========");
        String title = "PAYMENT DRAFT COMMENT";
        PaymentDraft draft = comment.getPaymentDraft();
    String message =
        MessageFormat.format(
            "{0} has commented on your payment with PN: {1}",
            comment.getEmployee().getFullName(), comment.getPaymentDraft().getPurchaseNumber());
        String emailContent = composeEmail(title, message, newCommentEmail);
        emailSender.sendMail(
                draft.getCreatedBy().getEmail(),
                EmailType.PAYMENT_DRAFT_COMMENT,
                emailContent);
    }

    private String composeEmail(String title, String message, String template) {
        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("message", message);
        return templateEngine.process(template, context);
    }

}
