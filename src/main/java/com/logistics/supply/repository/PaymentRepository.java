package com.logistics.supply.repository;

import com.logistics.supply.enums.PaymentStatus;
import com.logistics.supply.model.Payment;
import org.checkerframework.checker.nullness.Opt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

  @Query(
      value =
          "SELECT * from payment p where p.invoice_id in (SELECT i.id from invoice i where supplier_id =:supplierId) order by created_date desc",
      nativeQuery = true)
  List<Payment> findAllPaymentToSupplier(@Param("supplierId") int supplierId);

  List<Payment> findByPurchaseNumber(String purchaseNumber);

  Optional<Payment> findByChequeNumber(String chequeNumber);

  List<Payment> findByPaymentStatusOrderByCreatedDate(PaymentStatus paymentStatus);

  @Query(
      value =
          "SELECT sum(p.payment_amount) from payment p where p.purchase_number =:purchaseNumber",
      nativeQuery = true)
  Optional<BigDecimal> findTotalPaidAmountByPurchaseNumber(
      @Param("purchaseNumber") String purchaseNumber);

  @Query(
      value = "Select * from payment p where DATE(created_date) = CURRENT_DATE()",
      nativeQuery = true)
  List<Payment> findAllPaymentMadeToday();

  @Query(
      value = "Select * from payment p where MONTH(created_date) = MONTH (NOW())",
      nativeQuery = true)
  List<Payment> findAllPaymentMadeThisMonth();

  List<Payment> findPaymentByCreatedDate(Date date);

  @Query(
      value =
          "select * from payment p where p.invoice_id in ( select i.id from invoice i where i.invoice_number =:invoiceNumber) order by created_date desc",
      nativeQuery = true)
  List<Payment> findByInvoiceNumber(String invoiceNumber);

  List<Payment> findByBankOrderByCreatedDate(String bank);

//  @Query("UPDATE Payment p SET p.payment_status=:paymentStatus WHERE p.purchase_number =:purchaseNumber")
//  @Modifying
//  @Transactional
//  public void updatePaymentStatus(@Param("paymentStatus") String paymentStatus, @Param("purchaseNumber") String purchaseNumber);
}
