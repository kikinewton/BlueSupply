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
      helper.setTo(to);
      helper.setText(html, Boolean.TRUE);
      helper.setFrom(from);
      helper.setSubject(type.getEmailType());

      mailSender.send(message);

    } catch (MessagingException e) {
      log.error(e.getMessage());
    }
  }
}
