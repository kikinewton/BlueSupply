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
@Table(uniqueConstraints = { @UniqueConstraint(name = "uniqueFloatAndAmount", columnNames = { "floats_id", "amount" }) })
public class FloatPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    private FloatOrder floats;

    @ManyToOne
    private Employee paidBy;

    @Column(nullable = false)
    private BigDecimal amount;

    @CreationTimestamp
    private LocalDateTime createdDate;

    public FloatPayment(FloatOrder floats, Employee paidBy, BigDecimal amount) {
        this.floats = floats;
        this.paidBy = paidBy;
        this.amount = amount;
    }
}
