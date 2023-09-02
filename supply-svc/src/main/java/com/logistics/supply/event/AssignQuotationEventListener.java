package com.logistics.supply.event;

import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestForQuotation;
import com.logistics.supply.repository.RequestForQuotationRepository;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.util.EmailSenderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.logistics.supply.util.Constants.REQUEST_QUOTATION_FROM_PROCUREMENT_MAIL;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssignQuotationEventListener {

    private final EmailSenderUtil emailSenderUtil;
    private final EmployeeService employeeService;
    private final RequestForQuotationRepository requestForQuotationRepository;

    @Value("${config.templateMail}")
    private String quotationMailTemplate;


    @Async
    @Transactional
    @EventListener(condition = "#requestItemEvent.getHasQuotation() > 0")
    public void handleQuotationRequestItemEvent(AssignQuotationRequestItemEvent requestItemEvent) {

        log.info("QUOTATION ASSIGNED: Sending emails to General Manager and HOD");

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
                                                        });
                                            }
                                        }));

        Department department = requestItemEvent.getRequestItems()
                .stream()
                .findFirst()
                .get()
                .getEmployee()
                .getDepartment();

        Employee hod = employeeService.getDepartmentHOD(department);
        Employee gm = employeeService.getGeneralManager();

        try {
            emailSenderUtil.sendComposeAndSendEmail(
                    "QUOTATIONS ASSIGNED",
                    REQUEST_QUOTATION_FROM_PROCUREMENT_MAIL,
                    quotationMailTemplate,
                    EmailType.QUOTATION_TO_GM_AND_HOD_MAIL,
                    gm.getEmail());
            log.info("Sent email to General Manager: {}", gm.getEmail());
        } catch (Exception e) {
            log.error("Error sending email to GM: {}", e);
        }


        try {
            emailSenderUtil.sendComposeAndSendEmail(
                    "QUOTATIONS ASSIGNED",
                    REQUEST_QUOTATION_FROM_PROCUREMENT_MAIL,
                    quotationMailTemplate,
                    EmailType.QUOTATION_TO_GM_AND_HOD_MAIL,
                    hod.getEmail());

            log.info("Sent email to HOD: {}", hod.getEmail());
        } catch (Exception e) {
            log.error("Error sending email to HOD: {}", e);
        }

        log.info("Emails sent to Auditor, General Manager, and HOD");

    }
}
