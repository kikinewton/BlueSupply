package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logistics.supply.enums.RequestProcess;
import com.logistics.supply.event.listener.PettyCashCommentListener;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(PettyCashCommentListener.class)
public class PettyCashComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(length = 1000)
    private String description;

    boolean read;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    Employee employee;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "petty_cash_id")
    PettyCash pettyCash;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    RequestProcess processWithComment;

    @CreationTimestamp
    Date createdDate;

    @UpdateTimestamp
    Date updatedDate;

}
