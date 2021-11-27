package com.logistics.supply.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.tomcat.jni.Local;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
public class FloatPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    private Floats floats;

    @ManyToOne
    private Employee paidBy;

    @Column(nullable = false)
    private BigDecimal amount;

    @CreationTimestamp
    private LocalDateTime createdDate;

    public FloatPayment(Floats floats, Employee paidBy, BigDecimal amount) {
        this.floats = floats;
        this.paidBy = paidBy;
        this.amount = amount;
    }
}
