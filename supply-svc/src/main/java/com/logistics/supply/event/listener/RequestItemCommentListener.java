package com.logistics.supply.event.listener;

import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.RequestItemComment;
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
public class RequestItemCommentListener {
  private final EmailSenderUtil emailSenderUtil;
  @Value("${config.templateMail}")
  String newCommentEmail;

  @PostPersist
  public void sendRequestItemComment(RequestItemComment comment) {

    log.info("======= EMAIL 0N REQUEST ITEM COMMENT ==========");
    String title = "REQUEST COMMENT";
    RequestItem requestItem = comment.getRequestItem();

    String message =
        MessageFormat.format(
            "{0} has commented on your request: {1}",
            comment.getEmployee().getFullName(), requestItem.getName());

    CompletableFuture.runAsync(
        () ->
            emailSenderUtil.sendComposeAndSendEmail(
                title,
                message,
                newCommentEmail,
                EmailType.REQUEST_ITEM_COMMENT_EMAIL_TO_EMPLOYEE,
                requestItem.getEmployee().getEmail()));
  }
}
