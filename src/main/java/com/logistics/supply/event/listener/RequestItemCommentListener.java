package com.logistics.supply.event.listener;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.RequestItemComment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.persistence.PostPersist;
import java.text.MessageFormat;

@Slf4j
public class RequestItemCommentListener {

  final SpringTemplateEngine templateEngine;
  private final EmailSender emailSender;

  @Value("${config.templateMail}")
  String newCommentEmail;

  public RequestItemCommentListener(
      SpringTemplateEngine templateEngine,
      EmailSender emailSender) {
    this.templateEngine = templateEngine;
    this.emailSender = emailSender;
  }

  @PostPersist
  public void sendRequestItemComment(RequestItemComment comment) {
    log.info("======= EMAIL 0N REQUEST ITEM COMMENT ==========");
    String title = "REQUEST COMMENT";
    RequestItem requestItem = comment.getRequestItem();
    String message =
        MessageFormat.format(
            "{0} has commented on your request: {1}",
            comment.getEmployee().getFullName(), requestItem.getName());
    String emailContent = composeEmail(title, message, newCommentEmail);
    emailSender.sendMail(
        requestItem.getEmployee().getEmail(),
        EmailType.REQUEST_ITEM_COMMENT_EMAIL_TO_EMPLOYEE,
        emailContent);
  }

  private String composeEmail(String title, String message, String template) {
    Context context = new Context();
    context.setVariable("title", title);
    context.setVariable("message", message);
    return templateEngine.process(template, context);
  }
}
