package com.logistics.supply.event.listener;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.service.EmployeeService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.text.MessageFormat;
import java.util.List;

import static com.logistics.supply.util.Constants.REQUEST_PENDING_APPROVAL_TITLE;

@Slf4j
@Component
@RequiredArgsConstructor
public class HodReviewListener {
  private final EmailSender emailSender;
  private final EmployeeService employeeService;
  private final SpringTemplateEngine templateEngine;

  @Value("${procurement.defaultMail}")
  String emailTemplate;

  @Async
  @EventListener(condition ="#hodReviewEvent.isHodReview() == 'HOD_REVIEW'")
  public void sendMailToHod(HodReviewEvent hodReviewEvent) {
    log.debug("===== SEND MAIL TO GM =====");
    Employee gm = employeeService.getGeneralManager();
    String message =
            MessageFormat.format(
                    "Dear {0}, You have received requests pending approval", gm.getFullName());
    String content = composeEmail(REQUEST_PENDING_APPROVAL_TITLE, message, emailTemplate);
    System.out.println("Send mail to GM to approve request");
    emailSender.sendMail(gm.getEmail(), EmailType.REQUEST_ITEM_APPROVAL_GM, content);
    System.out.println("Email sent");
  }

  private String composeEmail(String title, String message, String template) {
    Context context = new Context();
    context.setVariable("title", title);
    context.setVariable("message", message);
    return templateEngine.process(template, context);
  }

  @Getter
  public static class HodReviewEvent {
    private List<RequestItem> requestItems;
    private String isHodReview;

    public HodReviewEvent(List<RequestItem> requestItems) {
      this.requestItems = requestItems;
      this.isHodReview =
              requestItems.stream().map(RequestItem::getRequestReview).findFirst().get().toString();
    }
  }
}
