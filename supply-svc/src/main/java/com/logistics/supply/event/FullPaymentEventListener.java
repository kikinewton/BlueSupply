package com.logistics.supply.event;

import org.springframework.stereotype.Component;

@Component
public class FullPaymentEventListener {

//  @Autowired private PaymentService paymentService;
//
//  @EventListener
//  public void handleFullPaymentEventListener(FullPaymentEvent fullPaymentEvent) {
//    System.out.println("========CHECK IF FULL PAYMENT TO SUPPLIER IS MADE==========");
//    var amountPayable =
//        fullPaymentEvent.getPayment().getGoodsReceivedNote().getInvoiceAmountPayable();
//    var totalAmountPaid =
//        paymentService.findTotalPaymentMadeByPurchaseNumber(
//            fullPaymentEvent.getPayment().getPurchaseNumber());
//    if (Objects.nonNull(totalAmountPaid)) {
//      double payable = Double.parseDouble(String.valueOf(amountPayable));
//      double totalPaid = Double.parseDouble(String.valueOf(totalAmountPaid));
//      if (totalPaid >= payable * 0.97) {
//        paymentService.updatePaymentStatus(
//            PaymentStatus.COMPLETED, fullPaymentEvent.getPayment().getPurchaseNumber());
//      }
//    }
//  }
}
