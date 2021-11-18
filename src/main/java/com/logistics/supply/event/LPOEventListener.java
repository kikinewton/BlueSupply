package com.logistics.supply.event;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import static com.logistics.supply.util.CommonHelper.buildNewHtmlEmail;
import static com.logistics.supply.util.Constants.LPO_ADDED_NOTIFICATION;
import static com.logistics.supply.util.Constants.LPO_LINK;

@Slf4j
@Component
@RequiredArgsConstructor
public class LPOEventListener {

  private final EmailSender emailSender;

  @Value("${stores.defaultEmail}")
  String DEFAULT_STORES_EMAIL;

  @Async
  @EventListener
  public void handleAddLPOEventListener(AddLPOEvent lpoEvent) {

      log.info("=============== SEND EMAIL TO STORES ================");
    String emailContent = buildNewHtmlEmail(LPO_LINK, "STORES", LPO_ADDED_NOTIFICATION);
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
