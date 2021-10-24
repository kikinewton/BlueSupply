package com.logistics.supply.event;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.PettyCash;
import com.logistics.supply.model.PettyCashComment;
import com.logistics.supply.repository.PettyCashRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.persistence.PostPersist;
import java.text.MessageFormat;

@Slf4j
public class PettyCashCommentListener {

  final PettyCashRepository pettyCashRepository;
  final SpringTemplateEngine templateEngine;
  private final EmailSender emailSender;

  @Value("${config.templateMail}")
  String newCommentEmail;

  public PettyCashCommentListener(
      @Lazy PettyCashRepository pettyCashRepository,
      SpringTemplateEngine templateEngine,
      EmailSender emailSender) {
    this.pettyCashRepository = pettyCashRepository;
    this.templateEngine = templateEngine;
    this.emailSender = emailSender;
  }

  @PostPersist
  public void sendPettyCashComment(PettyCashComment comment) {
    log.info("======= EMAIL 0N PETTY CASH COMMENT ==========");
    String title = "PETTY CASH COMMENT";
    PettyCash pettyCash = comment.getPettyCash();
    String message =
        MessageFormat.format(
            "{0} has commented on your petty cash request: {1}",
            comment.getEmployee().getFullName(), pettyCash.getName());
    String emailContent = composeEmail(title, message, newCommentEmail);
    emailSender.sendMail(
        pettyCash.getCreatedBy().getEmail(),
        EmailType.PETTY_CASH_COMMENT_EMAIL_TO_EMPLOYEE,
        emailContent);
  }

  private String composeEmail(String title, String message, String template) {
    Context context = new Context();
    context.setVariable("title", title);
    context.setVariable("message", message);
    return templateEngine.process(template, context);
  }
}
