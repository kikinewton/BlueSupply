package com.logistics.supply.email;

import com.logistics.supply.enums.EmailType;
import com.logistics.supply.util.CommonHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.constraints.Email;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class EmployeeEmailService implements EmailSender {

  @Value("${config.defaultSendingEmail}")
  String DEFAULT_SENDING_EMAIL;
  @Autowired private JavaMailSender mailSender;

  @Override
  @Async
  public void sendMail(@Email String to, EmailType type, @Email String email) {
    if (!CommonHelper.isValidEmailAddress(to)) {
      log.error("Invalid email format");
      return;
    }
    String from = DEFAULT_SENDING_EMAIL;
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
      String html = email;
      switch (type) {
        case NEW_REQUEST_MAIL:
          helper.setTo(to);
          helper.setText(html, Boolean.TRUE);
          helper.setSubject("NEW REQUEST");
          helper.setFrom(from);
          break;

        case PROCUREMENT_REVIEW_MAIL:
          helper.setTo(to);
          helper.setText(html, Boolean.TRUE);
          helper.setSubject("PROCUREMENT DETAILS FOR REQUEST");
          helper.setFrom(from);
          break;

        case REQUEST_ENDORSEMENT_MAIL:
          helper.setTo(to);
          helper.setText(html, Boolean.TRUE);
          helper.setSubject("ENDORSEMENT OF REQUEST");
          //          helper.setCc(""); -- to do
          helper.setFrom(from);
          break;
        case CANCELLED_REQUEST_MAIL:
          helper.setTo(to);
          helper.setText(html, Boolean.TRUE);
          helper.setSubject("CANCELLED REQUEST");
          helper.setFrom(from);
          break;

        case APPROVED_REQUEST_MAIL:
          helper.setTo(to);
          helper.setText(html, Boolean.TRUE);
          helper.setSubject("APPROVED REQUEST");
          helper.setFrom(from);
          break;

        case GENERAL_MANAGER_APPROVAL_MAIL:
          helper.setTo(to);
          helper.setText(html, Boolean.TRUE);
          helper.setSubject("REQUEST APPROVAL");
          helper.setFrom(from);
          break;

        case NEW_USER_CONFIRMATION_MAIL:
          helper.setTo(to);
          helper.setText(html, Boolean.TRUE);
          helper.setSubject("CONFIRMATION EMAIL");
          helper.setFrom(from);
          break;
          //          helper.setFrom("info@adminuser.com");

        case NEW_USER_PASSWORD_MAIL:
          helper.setTo(to);
          helper.setText(html, Boolean.TRUE);
          helper.setSubject("USER CREDENTIALS");
          helper.setFrom(from);
          break;

        case NOTIFY_EMPLOYEE_OF_ENDORSEMENT_MAIL:
          helper.setTo(to);
          helper.setText(html, Boolean.TRUE);
          helper.setSubject("HOD ENDORSEMENT");
          helper.setFrom(from);
          break;

        case QUOTATION_TO_GM_AND_HOD_MAIL:
          helper.setTo(to);
          helper.setText(html, Boolean.TRUE);
          helper.setSubject("QUOTATIONS FROM SUPPLIERS");
          helper.setFrom(from);
          break;
        case LPO_TO_STORES_EMAIL:
          helper.setTo(to);
          helper.setText(html, Boolean.TRUE);
          helper.setSubject("LPO TO STORES");
          helper.setFrom(from);
          break;
        case PAYMENT_DUE_EMAIL:
          helper.setTo(to);
          helper.setText(html, Boolean.TRUE);
          helper.setSubject("PAYMENT DUE");
          helper.setFrom(from);
          break;
        case FLOAT_ENDORSEMENT_EMAIL:
          helper.setTo(to);
          helper.setText(html, Boolean.TRUE);
          helper.setSubject("FLOAT ENDORSEMENT BY HOD");
          helper.setFrom(from);
          break;

        case PETTY_CASH_ENDORSEMENT_EMAIL:
          helper.setTo(to);
          helper.setText(html, Boolean.TRUE);
          helper.setSubject("PETTY CASH ENDORSEMENT BY HOD");
          helper.setFrom(from);
          break;

        case PETTY_CASH_COMMENT_EMAIL_TO_EMPLOYEE:
          helper.setTo(to);
          helper.setText(html, Boolean.TRUE);
          helper.setSubject("COMMENT ON PETTY CASH REQUEST");
          helper.setFrom(from);
          break;

        case FLOAT_COMMENT_EMAIL_TO_EMPLOYEE:
          helper.setTo(to);
          helper.setText(html, Boolean.TRUE);
          helper.setSubject("COMMENT ON FLOATS REQUEST");
          helper.setFrom(from);
          break;

        case REQUEST_ITEM_COMMENT_EMAIL_TO_EMPLOYEE:
          helper.setTo(to);
          helper.setText(html, Boolean.TRUE);
          helper.setSubject("COMMENT ON REQUEST ITEM");
          helper.setFrom(from);
          break;

        case EMPLOYEE_ROLE_CHANGE:
          helper.setTo(to);
          helper.setText(html, Boolean.TRUE);
          helper.setSubject("EMPLOYEE ROLE CHANGED");
          helper.setFrom(from);
          break;
      }
      mailSender.send(message);

    } catch (MessagingException e) {
      log.error(e.getMessage());
    }
  }
}
