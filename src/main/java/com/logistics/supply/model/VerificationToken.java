package com.logistics.supply.model;

import lombok.Data;


import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

@Data
@Entity
public class VerificationToken {

    public VerificationToken() {}

    public VerificationToken(String token, Employee employee) {
        this.token = token;
        this.employee = employee;
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }


    private static final int EXPIRATION = 60 * 24;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;


    @NotNull
    @Column
    String token;

    @OneToOne
    @JoinColumn(nullable = false, name = "employee_id")
    private Employee employee;

    @NotNull
    @Column
    private Date expiryDate;


    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }
}
