package com.logistics.supply.event.listener;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.FloatComment;
import com.logistics.supply.model.FloatOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.persistence.PostPersist;
import java.text.MessageFormat;

@Slf4j
public class FloatCommentListener {

  final SpringTemplateEngine templateEngine;
  private final EmailSender emailSender;

  @Value("${config.templateMail}")
  String newCommentEmail;

  public FloatCommentListener(SpringTemplateEngine templateEngine, EmailSender emailSender) {
    this.templateEngine = templateEngine;
    this.emailSender = emailSender;
  }

  @PostPersist
  public void sendFloatComment(FloatComment comment) {
    log.info("======= EMAIL 0N FLOAT COMMENT ==========");
    String title = "FLOATS COMMENT";
    FloatOrder floats = comment.getFloats();
    String message =
        MessageFormat.format(
            "{0} has commented on your floats request: {1}",
            comment.getEmployee().getFullName(), floats.getDescription());
    String emailContent = composeEmail(title, message, newCommentEmail);
    emailSender.sendMail(
        floats.getCreatedBy().getEmail(), EmailType.FLOAT_COMMENT_EMAIL_TO_EMPLOYEE, emailContent);
  }

  private String composeEmail(String title, String message, String template) {
    Context context = new Context();
    context.setVariable("title", title);
    context.setVariable("message", message);
    return templateEngine.process(template, context);
  }
}
