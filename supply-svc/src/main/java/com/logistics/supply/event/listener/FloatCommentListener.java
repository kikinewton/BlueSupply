package com.logistics.supply.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.persistence.PostPersist;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.FloatComment;
import com.logistics.supply.model.FloatOrder;
import com.logistics.supply.util.EmailSenderUtil;
import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class FloatCommentListener {

  private final EmailSenderUtil emailSenderUtil;

  @Value("${config.templateMail}")
  private String newCommentEmail;

  @PostPersist
  public void sendFloatComment(FloatComment comment) {
    log.info("======= EMAIL 0N FLOAT COMMENT ==========");
    String title = "FLOATS COMMENT";
    FloatOrder floats = comment.getFloats();
    String message =
        MessageFormat.format(
            "{0} has commented on your floats request: {1}",
            comment.getEmployee().getFullName(), floats.getDescription());
    CompletableFuture.runAsync(
        () -> {
          emailSenderUtil.sendComposeAndSendEmail(
              title,
              message,
              newCommentEmail,
              EmailType.FLOAT_COMMENT_EMAIL_TO_EMPLOYEE,
              floats.getCreatedBy().getEmail());
        });
  }
}
