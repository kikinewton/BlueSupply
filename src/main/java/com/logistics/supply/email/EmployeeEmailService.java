package com.logistics.supply.email;

import com.logistics.supply.enums.EmailType;
import com.logistics.supply.util.CommonHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class EmployeeEmailService implements EmailSender {

  @Autowired
  private JavaMailSender mailSender;

  @Override
  @Async
  public void sendMail(String from, String to, EmailType type, String email) {
    if (!CommonHelper.isValidEmailAddress(to)) {
      log.error("Invalid email format");
      return;
    }
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
          helper.setFrom(from);
          break;
        case CANCEL_REQUEST_MAIL:
          helper.setTo(to);
          helper.setText(html, Boolean.TRUE);
          helper.setSubject("CANCEL REQUEST");
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
          helper.setFrom("info@adminuser.com");

        case NEW_USER_PASSWORD_MAIL:
          helper.setTo(to);
          helper.setText(html, Boolean.TRUE);
          helper.setSubject("USER CREDENTIALS");
          helper.setFrom("info@adminuser.com");

      }
      mailSender.send(message);

    } catch (MessagingException e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }
}
