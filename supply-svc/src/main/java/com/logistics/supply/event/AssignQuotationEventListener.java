package com.logistics.supply.event;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestForQuotation;
import com.logistics.supply.repository.RequestForQuotationRepository;
import com.logistics.supply.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.logistics.supply.util.CommonHelper.buildNewHtmlEmail;
import static com.logistics.supply.util.Constants.REQUEST_QUOTATION_FROM_PROCUREMENT_MAIL;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssignQuotationEventListener {
  private final EmailSender emailSender;
  private final RequestForQuotationRepository requestForQuotationRepository;
  private final EmployeeService employeeService;

  @Value("${default.login:http://localhost:4000}")
  private String link;


  @Async
  @Transactional
  @EventListener(condition = "#requestItemEvent.getHasQuotation() > 0")
  public void handleQuotationRequestItemEvent(AssignQuotationRequestItemEvent requestItemEvent)
      throws Exception {
    log.info("=============== QUOTATION ASSIGNED ================");

    requestItemEvent
        .getRequestItems()
        .forEach(
            r -> r.getQuotations()
                .forEach(
                    quotation -> {
                      Optional<RequestForQuotation> rfq =
                          requestForQuotationRepository.findByQuotationReceivedFalseAndSupplier(
                              quotation.getSupplier());
                      if (rfq.isPresent()) {
                        rfq.map(
                                x -> {
                                  x.setQuotationReceived(true);
                                  return requestForQuotationRepository.save(x);
                                })
                            .get();
                      }
                    }));

    Employee hod =
        requestItemEvent.getRequestItems().stream()
            .map(x -> x.getEmployee().getDepartment())
            .limit(1)
            .map(employeeService::getDepartmentHOD)
            .findFirst()
            .orElseThrow(Exception::new);

    Employee gm = employeeService.getGeneralManager();

    // todo suppliers will be piped into table in the email
    //    String listOfSuppliers =
    //        requestItemEvent.getRequestItems().stream()
    //            .map(
    //                r ->
    //                    r.getSuppliers().stream().map(s -> s.getName())
    //                        .reduce(" ", (init, element) -> init + " " + element);

    String emailContentGM =
        buildNewHtmlEmail(
            link,
            gm.getLastName(),
            String.format(REQUEST_QUOTATION_FROM_PROCUREMENT_MAIL, "hi"));
    String emailContentHOD =
        buildNewHtmlEmail(
            link,
            gm.getLastName(),
            REQUEST_QUOTATION_FROM_PROCUREMENT_MAIL);

    CompletableFuture<String> sendToGM =
        CompletableFuture.supplyAsync(
            () -> {
              try {
                emailSender.sendMail(
                    gm.getEmail(), EmailType.QUOTATION_TO_GM_AND_HOD_MAIL, emailContentGM);
                log.info("Sent email to General Manager: {}", gm.getEmail());
              } catch (Exception e) {
                e.printStackTrace();
              }
              return "Email sent to GM";
            });

    CompletableFuture<String> sendToHOD =
        CompletableFuture.supplyAsync(
            () -> {
              try {
                emailSender.sendMail(
                    hod.getEmail(), EmailType.QUOTATION_TO_GM_AND_HOD_MAIL, emailContentHOD);
                log.info("Sent email to HOD: {}", hod.getEmail());
              } catch (Exception e) {
                e.printStackTrace();
              }
              return "Email sent to HOD";
            });

    String combined =
        Stream.of(sendToGM, sendToHOD)
            .map(CompletableFuture::join)
            .collect(Collectors.joining(" \n"));

    log.info(combined);
  }
}
