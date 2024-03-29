package com.logistics.supply.repository;

import com.logistics.supply.enums.PaymentStatus;
import com.logistics.supply.interfaces.projections.PaymentMade;
import com.logistics.supply.model.GoodsReceivedNote;
import com.logistics.supply.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
public interface PaymentRepository extends JpaRepository<Payment, Integer>, JpaSpecificationExecutor<Payment> {

  @Query(
      value =
          "SELECT * from payment p join goods_received_note grn on p.goods_received_note_id = grn.id and grn.supplier =:supplierId order by p.created_date desc",
      nativeQuery = true)
  List<Payment> findAllPaymentToSupplier(@Param("supplierId") int supplierId);

  @Query(
          value =
                  "SELECT * from payment p join goods_received_note grn on p.goods_received_note_id = grn.id and grn.supplier =:supplierId order by p.created_date desc",
          nativeQuery = true)
  Page<Payment> findAllPaymentToSupplier(@Param("supplierId") int supplierId, Pageable pageable);

  List<Payment> findByPurchaseNumber(String purchaseNumber);

  @Query(value = "Select * from payment p where p.cheque_number =:chequeNumber", nativeQuery = true)
  Optional<Payment> findByChequeNumberIncludeDeleted(@Param("chequeNumber") String chequeNumber);
  Optional<Payment> findByChequeNumber(String chequeNumber);


  @Query(
      value =
          "SELECT sum(p.payment_amount) from payment p where p.purchase_number =:purchaseNumber",
      nativeQuery = true)
  Optional<BigDecimal> findTotalPaidAmountByPurchaseNumber(
      @Param("purchaseNumber") String purchaseNumber);

  @Query(
      value =
          "Select p.purchase_number as purchaseNumber, p.payment_amount as paymentAmount, p.created_date as createdDate, p.payment_status as paymentStatus, p.cheque_number as chequeNumber, p.bank, (select s.name from supplier s where id in (select grn.supplier from goods_received_note grn where grn.id = p.goods_received_note_id)) as supplier  from payment p where DATE(created_date) = CURRENT_DATE",
      nativeQuery = true)
  List<PaymentMade> findAllPaymentMadeToday();

  @Query(
      value =
          "Select * from payment p where EXTRACT(MONTH FROM p.created_date) = EXTRACT(MONTH FROM CURRENT_DATE)",
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
          "UPDATE Payment p SET p.payment_status=:paymentStatus, p.goods_received_note_id = null WHERE p.purchase_number =:purchaseNumber",
      nativeQuery = true)
  @Modifying
  @Transactional
  public void updatePaymentStatus(
      @Param("paymentStatus") String paymentStatus, @Param("purchaseNumber") String purchaseNumber);


  @Query(
          value =
                  "UPDATE payment p SET payment_status=:paymentStatus, deleted = true WHERE cheque_number =:chequeNumber",
          nativeQuery = true)
  @Modifying
  @Transactional
  public void cancelPayment(
          @Param("paymentStatus") String paymentStatus, @Param("chequeNumber") String chequeNumber);

  @Query(
      value =
          "Select * from payment p where p.goods_received_note_id in "
              + "( select grn.id from goods_received_note grn where grn.invoice_id in"
              + " ( select id from invoice i2 where i2.payment_date <= current_date + interval '7 day'))"
              + " and UPPER(p.payment_status) != 'COMPLETED'",
      nativeQuery = true)
  Set<Payment> findPaymentsDueWithinOneWeek();

  @Query(
      value =
          "Select count(*) from payment p where p.goods_received_note_id in "
              + "              ( select grn.id from goods_received_note grn where grn.invoice_id in"
              + "               ( select id from invoice i2 where i2.payment_date <= current_date +  interval '7 day'))\n"
              + "               and UPPER(p.payment_status) != 'COMPLETED'",
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
              + "\tDATE(p.created_date) as payment_date,\n"
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
      value = "SELECT count(id) from payment p where DATE(p.created_date) = CURRENT_DATE",
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
              + "\t\tand CURRENT_DATE > DATE(i.payment_date));",
      nativeQuery = true)
  int findCountOfPaymentPastDueDate();

  List<Payment> findAllByCreatedDateBetween(Date periodStart, Date periodEnd);


  @Query(value = "Select * from payment p where p.goods_received_note_id =:grnId", nativeQuery = true)
  List<Payment> findByGoodsReceivedNote(@Param("grnId") long grnId);

  Boolean existsByGoodsReceivedNote(GoodsReceivedNote goodsReceivedNote);

  @Query(value = "SELECT * FROM payment p WHERE p.deleted = false", nativeQuery = true)
  List<Payment> findAllPayments();

  @Query(value = "SELECT * FROM payment p WHERE p.deleted = false", nativeQuery = true)
  Page<Payment> findAllPayment(Pageable pageable);

  @Query(value = "select count(id) from payment", nativeQuery = true)
  long countAll();

  String sql = "SELECT p.id, ( SELECT name from supplier s where s.id = grn.supplier) as supplier" +
          ", ( SELECT i.invoice_number from invoice i where i.id = grn.invoice_id) as invoice_no, " +
          "( SELECT s.account_number from supplier s where s.id = grn.supplier) as account_number" +
          ", p.cheque_number, p.purchase_number, " +
          "( SELECT DATE(i.payment_date) from invoice i where i.id = grn.invoice_id) as payment_due_date, " +
          "p.payment_amount as paid_amount, p.payment_status, DATE(p.created_date) as created_date, " +
          "p.with_holding_tax_amount as wht_amount from payment p " +
          "join goods_received_note grn on p.goods_received_note_id = grn.id where p.created_date" +
          " BETWEEN CAST(? AS DATE) and CAST(? AS DATE)";

  List<Payment> findByPaymentStatusOrderByCreatedDate(PaymentStatus paymentStatus);
}

