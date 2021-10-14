package com.logistics.supply.model;

import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class RequestItemComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(length = 1000)
    String description;

    Boolean read;

    @Enumerated(EnumType.STRING)
    Process processWithComment;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    Employee employee;

    int requestItemId;

    @CreationTimestamp
    Date createdDate;

    @UpdateTimestamp
    Date updatedDate;
}
