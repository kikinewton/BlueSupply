package com.logistics.supply.model;

import com.logistics.supply.event.listener.GRNListener;
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
@EntityListeners(GRNListener.class)
public class FloatGRN {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private boolean approvedByStoreManager;
  private Date dateOfApprovalByStoreManager;
  private Integer employeeStoreManager;
  private int floatOrderId;

  @ManyToOne
  @JoinColumn(name = "created_by_id")
  private Employee createdBy;

  @CreationTimestamp private Date createdDate;

  @UpdateTimestamp private Date updateDate;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Floats> floats = new HashSet<>();
}
