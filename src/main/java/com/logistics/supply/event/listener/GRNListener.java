package com.logistics.supply.event.listener;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.*;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.util.CommonHelper;
import com.logistics.supply.util.Constants;
import com.logistics.supply.util.EmailComposer;
import com.logistics.supply.util.EmailSenderUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.persistence.PostPersist;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GRNListener {

  private final EmployeeService employeeService;
  private final EmailSender emailSender;
  private final EmailSenderUtil emailSenderUtil;

  @Value("${stores.defaultEmail}")
  String storesDefaultMail;

  public GRNListener(
      EmailSender emailSender,
      @Lazy EmployeeService employeeService,
      EmailSenderUtil emailSenderUtil) {
    this.emailSender = emailSender;
    this.employeeService = employeeService;
    this.emailSenderUtil = emailSenderUtil;
  }

  private List<String> receivedGoodsTableTitleList() {
    List<String> title = new ArrayList<>();
    title.add("Description");
    title.add("Quantity");
    title.add("Reason");
    title.add("purpose");
    return title;
  }

  @EventListener(condition = "#event.getGoodsReceivedNote().isApprovedByHod() eq true")
  public void handleHodEndorseGRN(GRNEvent event) {
    GoodsReceivedNote grn = event.getGoodsReceivedNote();
    log.info("============ SEND MAIL TO PROCUREMENT MANAGER GRN APPROVAL BY HOD ==========");
    Employee procurementManager =
        employeeService.getManagerByRoleName(EmployeeRole.ROLE_PROCUREMENT_MANAGER.name());
    String message =
        MessageFormat.format(
            "Dear {0}, the GRN from supplier {1} with reference {2} has been approved",
            procurementManager.getFullName(), grn.getFinalSupplier().getName(), grn.getGrnRef());
    emailSenderUtil.sendComposeAndSendEmail(
        "GRN APPROVAL BY HOD",
        message,
        storesDefaultMail,
        EmailType.STORES_RECEIVED_GOODS_EMAIL_TO_STAKEHOLDERS,
        procurementManager.getEmail());
  }

  @PostPersist
  public void handleCreateFloatGRN(FloatGRN floatGRN) {
    log.info("============ SEND MAIL TO STORES MANAGER FLOAT GRN APPROVAL ==========");
    Employee storeManager =
        employeeService.getManagerByRoleName(EmployeeRole.ROLE_STORE_MANAGER.name());
    String message =
        MessageFormat.format(
            "Dear {0}, kindly approve Float GRN issued by {1}",
            storeManager.getFullName(), floatGRN.getCreatedBy().getFullName());
    emailSenderUtil.sendComposeAndSendEmail(
        "FLOAT GRN APPROVAL",
        message,
        storesDefaultMail,
        EmailType.STORES_MANAGER_APPROVE_GRN,
        storeManager.getEmail());
  }

  @EventListener(
      condition =
          "#event.getFloatGRN() != null && #event.getFloatGRN().isApprovedByStoreManager() == true")
  public void handleProcurementAdvise(FloatGRNEvent event) {
    Employee auditor = employeeService.getManagerByRoleName(EmployeeRole.ROLE_AUDITOR.name());
    String message =
        MessageFormat.format(
            "Dear {0}, kindly check Float GRN for request being retired", auditor.getFullName());
    emailSenderUtil.sendComposeAndSendEmail(
        "FLOAT AUDITOR CHECK",
        message,
        storesDefaultMail,
        EmailType.AUDITOR_FLOAT_RETIREMENT,
        auditor.getEmail());
  }

  @EventListener
  public void handleEvent(GRNEvent event) {
    GoodsReceivedNote goodsReceivedNote = event.getGoodsReceivedNote();
    log.info("============ SEND MAIL ON GOODS RECEIVED ==========");
    Employee procurementManager =
        employeeService.getManagerByRoleName(EmployeeRole.ROLE_PROCUREMENT_MANAGER.name());

    Set<String> emails = Sets.newHashSet(procurementManager.getEmail());

    // send email to procurement and general manager
    CompletableFuture.runAsync(
        () -> {
          try {
            if (!emails.isEmpty()) sendReceivedGoodsEmail(goodsReceivedNote, emails);
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
              String hodEmail = null;
              try {
                hodEmail = employeeService.getDepartmentHOD(x).getEmail();
              } catch (GeneralException e) {
                throw new RuntimeException(e);
              }
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

  @Getter
  @Setter
  public static class GRNEvent extends ApplicationEvent {

    private GoodsReceivedNote goodsReceivedNote;

    public GRNEvent(Object source, GoodsReceivedNote goodsReceivedNote) {
      super(source);
      this.goodsReceivedNote = goodsReceivedNote;
    }
  }

  @Getter
  @Setter
  public static class FloatGRNEvent extends ApplicationEvent {
    private FloatGRN floatGRN;

    public FloatGRNEvent(Object source, FloatGRN floatGRN) {
      super(source);
      this.floatGRN = floatGRN;
    }
  }
}
