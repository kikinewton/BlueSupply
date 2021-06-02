package com.logistics.supply.repository;

import com.logistics.supply.dto.PaymentReportDTO;
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
import java.util.Set;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

  @Query(
      value =
          "SELECT * from payment p join goods_received_note grn on p.goods_received_note_id = grn.id and grn.supplier =:supplierId order by p.created_date desc",
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

  @Query(
      value =
          "UPDATE Payment p SET p.payment_status=:paymentStatus WHERE p.purchase_number =:purchaseNumber",
      nativeQuery = true)
  @Modifying
  @Transactional
  public void updatePaymentStatus(
      @Param("paymentStatus") String paymentStatus, @Param("purchaseNumber") String purchaseNumber);

  @Query(
      value =
          "Select * from payment p where p.goods_received_note_id in "
              + "( select grn.id from goods_received_note grn where grn.invoice_id in"
              + " ( select id from invoice i2 where i2.payment_date <= date_add(current_date() , interval 7 day)))"
              + " and p.payment_status != 'completed'",
      nativeQuery = true)
  Set<Payment> findPaymentsDueWithinOneWeek();

  @Query(
      value =
          "Select count(*) from payment p where p.goods_received_note_id in "
              + "( select grn.id from goods_received_note grn where grn.invoice_id in"
              + " ( select id from invoice i2 where i2.payment_date <= date_add(current_date() , interval 7 day)))"
              + " and p.payment_status != 'completed'",
      nativeQuery = true)
  int findCountOfPaymentsDueWithinOneWeek();

  @Query(
      value =
          "SELECT\n"
              + "\tp.id,\n"
              + "\t(\n"
              + "\tSELECT\n"
              + "\t\tname\n"
              + "\tfrom\n"
              + "\t\tsupplier s\n"
              + "\twhere\n"
              + "\t\ts.id = grn.supplier) as supplier,\n"
              + "\t(\n"
              + "\tSELECT\n"
              + "\t\ti.invoice_number\n"
              + "\tfrom\n"
              + "\t\tinvoice i\n"
              + "\twhere\n"
              + "\t\ti.id = grn.invoice_id) as invoice_no,\n"
              + "\t(\n"
              + "\tSELECT\n"
              + "\t\ts.account_number\n"
              + "\tfrom\n"
              + "\t\tsupplier s\n"
              + "\twhere\n"
              + "\t\ts.id = grn.supplier) as account_number,\n"
              + "\tp.cheque_number,\n"
              + "\tp.purchase_number,\n"
              + "\t(\n"
              + "\tSELECT\n"
              + "\t\tDATE(i.payment_date)\n"
              + "\tfrom\n"
              + "\t\tinvoice i\n"
              + "\twhere\n"
              + "\t\ti.id = grn.invoice_id) as \"payment_due_date\",\n"
              + "\tp.payment_amount as \"paid_amount\",\n"
              + "\tp.payment_status,\n"
              + "\tDATE(p.created_date) as \"created_date\",\n"
              + "\tp.with_holding_tax_amount as wht_amount\n"
              + "from\n"
              + "\tpayment p\n"
              + "join goods_received_note grn on\n"
              + "\tp.goods_received_note_id = grn.id\n"
              + "where\n"
              + "\tp.created_date BETWEEN CAST(:startDate AS DATE) and CAST(:endDate AS DATE)",
      nativeQuery = true)
  List<Object[]> getPaymentReport(
      @Param("startDate") Date startDate, @Param("endDate") Date endDate);

  @Query(
      value = "SELECT count(id) from payment p where p.created_date = CURRENT_DATE()",
      nativeQuery = true)
  int findCountOfPaymentMadeToday();

  @Query(
      value =
          "SELECT\n"
              + "\tcount(*) as num\n"
              + "from\n"
              + "\tpayment p\n"
              + "join goods_received_note grn on\n"
              + "\tp.goods_received_note_id = grn.id\n"
              + "where\n"
              + "\tgrn.invoice_id in (\n"
              + "\tSELECT\n"
              + "\t\ti.id\n"
              + "\tfrom\n"
              + "\t\tinvoice i\n"
              + "\twhere\n"
              + "\t\ti.id = grn.invoice_id\n"
              + "\t\tand CURRENT_DATE() > DATE(i.payment_date));",
      nativeQuery = true)
  int findCountOfPaymentPastDueDate();


}
