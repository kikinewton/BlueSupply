package com.logistics.supply.event.listener;

import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.Quotation;
import com.logistics.supply.model.QuotationComment;
import com.logistics.supply.util.EmailSenderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.persistence.PostPersist;
import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuotationCommentListener {

  private final EmailSenderUtil emailSenderUtil;

  @Value("${config.templateMail}")
  String newCommentEmail;

  @PostPersist
  public void sendEmailOnComment(QuotationComment comment) {
    log.info("======= EMAIL 0N QUOTATION COMMENT ==========");
    String title = "QUOTATION COMMENT";
    Quotation quotation = comment.getQuotation();
    String message =
        MessageFormat.format(
            "{0} has commented on quotation with reference: {1}",
            comment.getEmployee().getFullName(), quotation.getQuotationRef());
    CompletableFuture.runAsync(() -> {
      emailSenderUtil.sendComposeAndSendEmail(
              title,
              message,
              newCommentEmail,
              EmailType.QUOTATION_COMMENT_EMAIL,
              quotation.getCreatedBy().getEmail());
    });
  }
}
