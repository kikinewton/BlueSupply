package com.logistics.supply.model;

import com.logistics.supply.enums.RequestProcess;
import com.logistics.supply.event.CommentListener;
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
@EntityListeners(CommentListener.class)
public class RequestItemComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(length = 1000)
    String description;

    Boolean read;

    @Enumerated(EnumType.STRING)
    RequestProcess processWithComment;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    Employee employee;

    int requestItemId;

    @CreationTimestamp
    Date createdDate;

    @UpdateTimestamp
    Date updatedDate;
}
