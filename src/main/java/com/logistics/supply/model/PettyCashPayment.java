package com.logistics.supply.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
public class PettyCashPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private PettyCash pettyCash;

    @ManyToOne
    private Employee paidBy;

    @Column(nullable = false)
    private BigDecimal amount;

    @CreationTimestamp
    private LocalDateTime createdDate;

    public PettyCashPayment(PettyCash pettyCash, Employee paidBy, BigDecimal amount) {
        this.pettyCash = pettyCash;
        this.paidBy = paidBy;
        this.amount = amount;
    }
}
