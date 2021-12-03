package com.logistics.supply.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
public class FloatGRN {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private boolean approvedByHod;
    private Date dateOfApprovalByHod;
    private Integer employeeHod;

    private Integer employeeGm;
    private Date dateOfApprovalByGm;
    private boolean approvedByGm;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private Employee createdBy;

    @CreationTimestamp
    private Date createdDate;

    @OneToOne
    private Floats floats;

}
