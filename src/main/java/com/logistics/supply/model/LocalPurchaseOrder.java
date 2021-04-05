package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Data
public class LocalPurchaseOrder {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String comment;

  @ManyToOne(fetch = FetchType.EAGER)
  private Employee procurementOfficer;

  @OneToMany private Set<RequestItem> requestItem;

  @JsonIgnore @CreationTimestamp private Date createdDate;

  @JsonIgnore private Date updatedDate;

  @PostUpdate
  public void logAfterUpdate() {
    updatedDate = new Date();
  }
}
