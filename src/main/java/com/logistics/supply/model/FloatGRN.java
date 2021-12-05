package com.logistics.supply.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
@Table(name = "float_grn")
public class FloatGRN {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private boolean approvedByHod;
  private Date dateOfApprovalByHod;
  private Integer employeeHod;

  @ManyToOne
  @JoinColumn(name = "created_by_id")
  private Employee createdBy;

  @CreationTimestamp private Date createdDate;

  @UpdateTimestamp private Date updateDate;

  @OneToMany
  @JoinColumn(name = "float_id", referencedColumnName = "id")
  private Set<Floats> floats = new HashSet<>();
}
