package com.logistics.supply.scheduler;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.Payment;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.service.AbstractDataService;
import com.logistics.supply.service.ExcelService;
import com.logistics.supply.util.EmailComposer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.*;

@Service
@Slf4j
public class DuePaymentReminder extends AbstractDataService {

  private final EmailSender emailSender;
  @Autowired ExcelService excelService;

  public DuePaymentReminder(EmailSender emailSender) {
    this.emailSender = emailSender;
  }

  private Set<Payment> paymentsWithOneWeekDueDate() {
    Set<Payment> payments = new HashSet<>();
    try {
      System.out.println("Check for payments due in one week ");
      payments.addAll(paymentRepository.findPaymentsDueWithinOneWeek());
      return payments;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return payments;
  }

  private Set<Employee> paymentRelatedEmployees() {
    Set<Employee> employees = new HashSet<>();
    try {
      employees.addAll(employeeRepository.findEmployeeRelatingToFinance());
      return employees;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return employees;
  }

  private List<String> tableTitleList() {
    List<String> title = new ArrayList<>();
    title.add("Name");
    title.add("Phone No");
    title.add("Email");
    return title;
  }

  private List<String> emailList(Set<Employee> employees) {
    return employees.stream().map(x -> x.getEmail()).collect(Collectors.toList());
  }

  private List<Supplier> suppliersToBePaid(Set<Payment> payments) {
    return payments.stream()
        .map(x -> x.getGoodsReceivedNote().getInvoice().getSupplier())
        .collect(Collectors.toList());
  }

  private static String buildHtmlTableForSuppliers(List<String> title, List<Supplier> suppliers) {
    StringBuilder header = new StringBuilder();
    for (String t : title) header.append(String.format(tableHeader, t));

    header = new StringBuilder(String.format(tableRow, header.toString()));
    String sb =
        suppliers.stream()
            .map(
                s ->
                    String.format(tableData, s.getName())
                        + String.format(tableData, s.getPhone_no())
                        + String.format(tableData, s.getEmail()))
            .map(t -> String.format(tableRow, t))
            .collect(Collectors.joining("", "", ""));

    return header.toString().concat(sb);
  }

  @Async
//  @Scheduled(cron = "0 0 8  * * *")
    @Scheduled(fixedRate = 1000000, initialDelay = 5000)
  public void sendReminder() {
    System.out.println("Send reminder");
    Set<Payment> payments = paymentsWithOneWeekDueDate();
    List<Supplier> suppliers = suppliersToBePaid(payments);
    String suppliersHtmlTable = buildHtmlTableForSuppliers(tableTitleList(), suppliers);
    String emailContent =
        EmailComposer.buildEmailWithTable(
            "PAYMENT DUE", payment_due_reminder_message, suppliersHtmlTable);
    if (suppliers.size() > 0)
      paymentRelatedEmployees().stream()
          .map(
              e -> {
                emailSender.sendMail(e.getEmail(), EmailType.PAYMENT_DUE_EMAIL, emailContent);
                return "Email sent to employee:  " + e.getEmail();
              })
          .forEach(System.out::println);
  }
}
