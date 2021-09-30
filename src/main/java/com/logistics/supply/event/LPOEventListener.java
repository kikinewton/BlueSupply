package com.logistics.supply.event;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import static com.logistics.supply.util.Constants.*;

import static com.logistics.supply.util.CommonHelper.buildNewHtmlEmail;

@Slf4j
@Component
public class LPOEventListener {

    @Value("${stores.defaultEmail}")
    String DEFAULT_STORES_EMAIL;

  private final EmailSender emailSender;
  @Autowired private EmployeeService employeeService;

  public LPOEventListener(EmailSender emailSender) {
    this.emailSender = emailSender;
  }

  @Async
  @EventListener
  public void handleAddLPOEventListener(AddLPOEvent addLPOEvent) {
    String emailContent = buildNewHtmlEmail(LPO_LINK, "STORES", LPO_ADDED_NOTIFICATION);
    System.out.println("=============== SEND EMAIL TO STORES ================");
    CompletableFuture.runAsync(
        () -> {
          try {
            emailSender.sendMail(DEFAULT_STORES_EMAIL, EmailType.LPO_TO_STORES_EMAIL, emailContent);
          } catch (Exception e) {
            log.error(e.getMessage());
          }
        });
  }
}
