package com.logistics.supply.model;

import com.logistics.supply.enums.PaymentMethod;
import com.logistics.supply.enums.PaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Slf4j
@Entity
@NoArgsConstructor
public class PaymentDraft {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NonNull
    @Column(unique = true)
    private String purchaseNumber;

    @OneToOne private GoodsReceivedNote goodsReceivedNote;

    @PositiveOrZero
    private BigDecimal paymentAmount;

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

    @CreationTimestamp Date createdDate;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    Employee createdBy;


}
