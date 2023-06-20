package com.logistics.supply.event.listener;

import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.GoodsReceivedNoteComment;
import com.logistics.supply.model.Role;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.RoleService;
import com.logistics.supply.util.EmailSenderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.persistence.PostPersist;
import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class GRNCommentListener {
  private final EmailSenderUtil emailSenderUtil;
  private final EmployeeService employeeService;
  private final RoleService roleService;

  public GRNCommentListener(
      EmailSenderUtil emailSenderUtil,
      @Lazy EmployeeService employeeService,
      @Lazy RoleService roleService) {
    this.emailSenderUtil = emailSenderUtil;
    this.employeeService = employeeService;
    this.roleService = roleService;
  }

  @Value("${config.templateMail}")
  private String newCommentEmail;

  @PostPersist
  public void sendCommentAlert(GoodsReceivedNoteComment comment) {
    log.info("======= EMAIL 0N GRN COMMENT ==========");
    String title = "GRN COMMENT";
    String message =
        MessageFormat.format(
            "{0} has commented on GRN with reference: {1}",
            comment.getEmployee().getFullName(), comment.getGoodsReceivedNote().getGrnRef());
    CompletableFuture.runAsync(
        () -> {
          /**
           * send mail to PROCUREMENT MANAGER if comment by account manager send mail to response to
           * Account personnel that raised the comment
           */
          String mailTo = "";
          switch (comment.getProcessWithComment()) {
            case REVIEW_GRN_ACCOUNTS:
              mailTo =
                  employeeService
                      .getManagerByRoleName(EmployeeRole.ROLE_PROCUREMENT_MANAGER.name())
                      .getEmail();
              break;
            case PROCUREMENT_RESPONSE_TO_ACCOUNT_GRN_COMMENT:
              Role role = roleService.findByName(EmployeeRole.ROLE_ACCOUNT_OFFICER.name());
              mailTo = employeeService.findRecentEmployeeWithRoleId(role.getId()).getEmail();
              break;
            default:
              throw new UnsupportedOperationException();
          }

          emailSenderUtil.sendComposeAndSendEmail(
              title, message, newCommentEmail, EmailType.PAYMENT_DRAFT_COMMENT, mailTo);
        });
  }
}
