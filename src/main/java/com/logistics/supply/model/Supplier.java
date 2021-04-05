package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Data
@ToString
public class Supplier {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, unique = false)
  private String name;

  private String phone_no;

  private String location;

  private String description;

  private String email;

  String accountNumber;

  String bank;

  @ManyToMany(mappedBy = "suppliers")
  private Set<RequestItem> requestItems;

  @JsonIgnore @CreationTimestamp Date createdDate;

  @JsonIgnore Date updatedDate;

  @PostUpdate
  public void logAfterUpdate() {
    updatedDate = new Date();
  }
}
