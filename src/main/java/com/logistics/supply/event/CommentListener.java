package com.logistics.supply.event;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.FloatsRepository;
import com.logistics.supply.repository.PettyCashRepository;
import com.logistics.supply.repository.RequestItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.persistence.PostPersist;
import java.text.MessageFormat;

@Slf4j
public class CommentListener {

  final RequestItemRepository requestItemRepository;
  final FloatsRepository floatsRepository;
  final PettyCashRepository pettyCashRepository;
  final SpringTemplateEngine templateEngine;
  private final EmailSender emailSender;

  @Value("${config.templateMail}")
  String newCommentEmail;

  public CommentListener(
      @Lazy RequestItemRepository requestItemRepository,
      @Lazy FloatsRepository floatsRepository,
      @Lazy PettyCashRepository pettyCashRepository,
      SpringTemplateEngine templateEngine,
      EmailSender emailSender) {
    this.requestItemRepository = requestItemRepository;
    this.floatsRepository = floatsRepository;
    this.pettyCashRepository = pettyCashRepository;
    this.templateEngine = templateEngine;
    this.emailSender = emailSender;
  }

  @PostPersist
  public void sendRequestItemComment(RequestItemComment comment) {
    log.info("======= EMAIL 0N REQUEST ITEM COMMENT ==========");
    String title = "Request Comment";
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

  public void sendFloatComment(FloatComment comment) {
    log.info("======= EMAIL 0N FLOAT COMMENT ==========");
    String title = "FLOATS COMMENT";
    Floats floats = floatsRepository.findById(comment.getFloatId()).get();
    String message =
        MessageFormat.format(
            "{0} has commented on your floats request {1}",
            comment.getEmployee().getFullName(), floats.getItemDescription());
    String emailContent = composeEmail(title, message, newCommentEmail);
    emailSender.sendMail(
        floats.getCreatedBy().getEmail(), EmailType.FLOAT_COMMENT_EMAIL_TO_EMPLOYEE, emailContent);
  }

  public void sendPettyCashComment(PettyCashComment comment) {
    log.info("======= EMAIL 0N PETTY CASH COMMENT ==========");
    String title = "PETTY CASH COMMENT";
    PettyCash pettyCash = pettyCashRepository.findById(comment.getPettyCashId()).get();
    String message =
        MessageFormat.format(
            "{0} has commented on your petty cash request {1}",
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
