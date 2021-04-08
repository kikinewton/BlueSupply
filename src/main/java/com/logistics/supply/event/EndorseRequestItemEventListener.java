//package com.logistics.supply.event;
//
//import com.logistics.supply.email.EmailSender;
//import com.logistics.supply.enums.EmailType;
//import org.springframework.context.event.EventListener;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Component;
//
//import java.util.concurrent.CompletableFuture;
//
//import static com.logistics.supply.util.CommonHelper.buildEmail;
//import static com.logistics.supply.util.Constants.*;
//
//@Component
//public class EndorseRequestItemEventListener {
//
//  private final EmailSender emailSender;
//
//  public EndorseRequestItemEventListener(EmailSender emailSender) {
//    this.emailSender = emailSender;
//  }
//
//  @Async
//  @EventListener
//  public void handleRequestItemEndorsedEvent(BulkRequestItemEvent requestItemEvent) {
//
//    String emailContent =
//        buildEmail(
//            "PROCUREMENT",
//            REQUEST_PENDING_PROCUREMENT_DETAILS_LINK,
//            REQUEST_PENDING_PROCUREMENT_DETAILS_TITLE,
//            PROCUREMENT_DETAILS_MAIL);
//
//    CompletableFuture.runAsync(
//        () -> {
//          try {
//            emailSender.sendMail(
//                DEFAULT_PROCUREMENT_MAIL, EmailType.PROCUREMENT_REVIEW_MAIL, emailContent);
//          } catch (Exception e) {
//            e.printStackTrace();
//          }
//        });
//  }
//}
