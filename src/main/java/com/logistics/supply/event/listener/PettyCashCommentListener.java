package com.logistics.supply.event.listener;

import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.PettyCash;
import com.logistics.supply.model.PettyCashComment;
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
public class PettyCashCommentListener {
  private final EmailSenderUtil emailSenderUtil;
  @Value("${config.templateMail}")
  String newCommentEmail;

  @PostPersist
  public void sendPettyCashComment(PettyCashComment comment) {
    log.info("======= EMAIL 0N PETTY CASH COMMENT ==========");
    String title = "PETTY CASH COMMENT";
    PettyCash pettyCash = comment.getPettyCash();
    String message =
        MessageFormat.format(
            "{0} has commented on your petty cash request: {1}",
            comment.getEmployee().getFullName(), pettyCash.getName());
    CompletableFuture.runAsync(
        () -> {
          emailSenderUtil.sendComposeAndSendEmail(
              title,
              message,
              newCommentEmail,
              EmailType.PETTY_CASH_COMMENT_EMAIL_TO_EMPLOYEE,
              pettyCash.getCreatedBy().getEmail());
        });
  }
}
