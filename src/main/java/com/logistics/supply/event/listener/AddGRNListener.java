package com.logistics.supply.event.listener;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.*;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.util.CommonHelper;
import com.logistics.supply.util.Constants;
import com.logistics.supply.util.EmailComposer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AddGRNListener {

  final EmployeeService employeeService;
  private final EmailSender emailSender;
  @Value("${stores.defaultEmail}")
  String storesDefaultMail;

  public AddGRNListener(EmailSender emailSender, @Lazy EmployeeService employeeService) {
    this.emailSender = emailSender;
    this.employeeService = employeeService;
  }

  private List<String> receivedGoodsTableTitleList() {
    List<String> title = new ArrayList<>();
    title.add("Description");
    title.add("Quantity");
    title.add("Reason");
    title.add("purpose");
    return title;
  }

  @EventListener
  public void handleEvent(GRNEvent grnEvent) {
    GoodsReceivedNote goodsReceivedNote = grnEvent.goodsReceivedNote;
    log.info("============ SEND MAIL ON GOODS RECEIVED ==========");

    Employee procurementManager =
        employeeService.getManagerByRoleName(EmployeeRole.ROLE_PROCUREMENT_MANAGER.name());
    Employee generalManager = employeeService.getGeneralManager();

    Set<String> emails = Sets.newHashSet(procurementManager.getEmail(), generalManager.getEmail());

    // send email to procurement and general manager
    CompletableFuture.runAsync(
        () -> {
          try {
            sendReceivedGoodsEmail(goodsReceivedNote, emails);
          } catch (Exception e) {
            log.error(e.toString());
          }
        });

    Map<Department, List<RequestItem>> result =
        goodsReceivedNote.getReceivedItems().stream()
            .collect(Collectors.groupingBy(RequestItem::getUserDepartment));
    result.keySet().stream()
        .forEach(
            x -> {
              // send email to hod
              String goodsHtmlTable =
                  CommonHelper.buildHtmlTableForRequestItems(
                      receivedGoodsTableTitleList(), result.get(x));
              String emailContent =
                  EmailComposer.buildEmailWithTable(
                      "STORES RECEIVED GOODS", Constants.GOODS_RECEIVED_MESSAGE, goodsHtmlTable);
              String hodEmail = employeeService.getDepartmentHOD(x).getEmail();
              emailSender.sendMail(
                  hodEmail, EmailType.STORES_RECEIVED_GOODS_EMAIL_TO_STAKEHOLDERS, emailContent);
            });
  }

  private void sendReceivedGoodsEmail(GoodsReceivedNote goodsReceivedNote, Set<String> emails) {
    String goodsHtmlTable =
        CommonHelper.buildHtmlTableForRequestItems(
            receivedGoodsTableTitleList(),
            Lists.newArrayList(goodsReceivedNote.getReceivedItems()));
    String emailContent =
        EmailComposer.buildEmailWithTable(
            "STORES RECEIVED GOODS", Constants.GOODS_RECEIVED_MESSAGE, goodsHtmlTable);
    emails.stream()
        .forEach(
            x ->
                emailSender.sendMail(
                    x, EmailType.STORES_RECEIVED_GOODS_EMAIL_TO_STAKEHOLDERS, emailContent));
  }

  public static class GRNEvent extends ApplicationEvent {

    private GoodsReceivedNote goodsReceivedNote;

    public GRNEvent(Object source, GoodsReceivedNote goodsReceivedNote) {
      super(source);
      this.goodsReceivedNote = goodsReceivedNote;
    }
  }
}
