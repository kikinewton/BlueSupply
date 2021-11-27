package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.logistics.supply.enums.PaymentMethod;
import com.logistics.supply.enums.PaymentStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

@Data
@Slf4j
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(
        value = {"createdDate", "lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class PaymentDraft extends AbstractAuditable<Employee, Integer> {

    @NonNull
    @Column(unique = true)
    private String purchaseNumber;

    @OneToOne private GoodsReceivedNote goodsReceivedNote;

    @PositiveOrZero
    private BigDecimal paymentAmount;

    @Column(updatable = false)
    private BigDecimal withHoldingTaxAmount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(updatable = false)
    private String chequeNumber;

    @Column(updatable = false)
    private String bank;

    @Column(updatable = false)
    private String auditorComment;

    private Boolean approvalFromAuditor;

    private Date approvalByAuditorDate;

    Boolean approvalFromGM;

    Boolean approvalFromFM;

    Date approvalByGMDate;

    Date approvalByFMDate;

    @PrePersist
    private void calculateWithholdingTax() {
    if (paymentMethod.getPaymentMethod().equals(PaymentMethod.CASH.getPaymentMethod())) return;
      withHoldingTaxAmount = paymentAmount.multiply(BigDecimal.valueOf(0.03));
        log.info("Attempting to add withholding tax: " + withHoldingTaxAmount);
    }
}
